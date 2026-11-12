package com.gateway.payment.configuration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.payment.audit.service.AuditService;
import com.gateway.payment.common.crypto.SecretEncryptionService;
import com.gateway.payment.common.domain.AuthType;
import com.gateway.payment.common.exception.BusinessException;
import com.gateway.payment.configuration.api.dto.OmnicanalBulkImportResponse;
import com.gateway.payment.configuration.api.dto.OmnicanalConnectivityImportRequest;
import com.gateway.payment.configuration.api.dto.OmnicanalConnectivityImportRequest.OmnicanalApiSpec;
import com.gateway.payment.configuration.api.dto.OmnicanalConnectivityImportRequest.OmnicanalMethodSpec;
import com.gateway.payment.configuration.api.dto.OmnicanalConnectivityImportResponse;
import com.gateway.payment.configuration.domain.FseParticipantKind;
import com.gateway.payment.configuration.domain.FseStatut;
import com.gateway.payment.configuration.domain.PaymentProvider;
import com.gateway.payment.common.domain.ExecutionMode;
import com.gateway.payment.common.domain.PaymentMethodType;
import com.gateway.payment.configuration.domain.ProviderApi;
import com.gateway.payment.configuration.domain.ProviderEnvironment;
import com.gateway.payment.configuration.domain.ProviderEnvironmentCode;
import com.gateway.payment.configuration.domain.ProviderFseApi;
import com.gateway.payment.configuration.domain.ProviderOperation;
import com.gateway.payment.configuration.repository.PaymentProviderRepository;
import com.gateway.payment.configuration.repository.ProviderApiRepository;
import com.gateway.payment.configuration.repository.ProviderEnvironmentRepository;
import com.gateway.payment.configuration.repository.ProviderFseApiRepository;
import com.gateway.payment.configuration.repository.ProviderOperationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * Migre la connectivité Omnicanal (Api + Methodes + ParametreMethode) vers
 * ProviderFseApi (Api) + Operation/ProviderApi (Méthodes) — sans toucher au SANDBOX actif.
 */
@Service
public class OmnicanalConnectivityMigrationService {

    private static final Logger log = LoggerFactory.getLogger(OmnicanalConnectivityMigrationService.class);

    public static final String FIXTURE_CORIS_MONEY = "omnicanal-migration/coris-money-0011.json";
    public static final String FIXTURE_CORIS_BANK = "omnicanal-migration/coris-bank-0004.json";
    public static final String EXPORTED_MANIFEST = "omnicanal-migration/exported/manifest.json";
    public static final String EXPORTED_DIR = "omnicanal-migration/exported/";

    private record ManifestEntry(String file, String codeFse, String providerCodeHint, Integer methods, String libelle) {}

    private record Manifest(Integer version, String source, Integer count, List<ManifestEntry> fixtures) {}

    private final PaymentProviderRepository providerRepository;
    private final ProviderEnvironmentRepository environmentRepository;
    private final ProviderOperationRepository operationRepository;
    private final ProviderApiRepository apiRepository;
    private final ProviderFseApiRepository fseApiRepository;
    private final EnvironmentService environmentService;
    private final SecretEncryptionService encryptionService;
    private final AuditService auditService;
    private final ObjectMapper objectMapper;
    private final com.gateway.payment.orchestration.service.OrchestrationSeedService orchestrationSeedService;

    public OmnicanalConnectivityMigrationService(
            PaymentProviderRepository providerRepository,
            ProviderEnvironmentRepository environmentRepository,
            ProviderOperationRepository operationRepository,
            ProviderApiRepository apiRepository,
            ProviderFseApiRepository fseApiRepository,
            EnvironmentService environmentService,
            SecretEncryptionService encryptionService,
            AuditService auditService,
            ObjectMapper objectMapper,
            com.gateway.payment.orchestration.service.OrchestrationSeedService orchestrationSeedService
    ) {
        this.providerRepository = providerRepository;
        this.environmentRepository = environmentRepository;
        this.operationRepository = operationRepository;
        this.apiRepository = apiRepository;
        this.fseApiRepository = fseApiRepository;
        this.environmentService = environmentService;
        this.encryptionService = encryptionService;
        this.auditService = auditService;
        this.objectMapper = objectMapper;
        this.orchestrationSeedService = orchestrationSeedService;
    }

    /** Seed idempotent au bootstrap — DEVELOPPEMENT seulement. */
    @Transactional
    public void seedBundledFixtures() {
        applyFixtureIfProviderExists(FIXTURE_CORIS_MONEY, "CORIS_MONEY");
        applyFixtureIfProviderExists(FIXTURE_CORIS_BANK, "CORIS_BANK");
    }

    /**
     * Importe toutes les fixtures exportées depuis la DB Omnicanal
     * ({@code omnicanal-migration/exported/manifest.json}).
     */
    @Transactional
    public OmnicanalBulkImportResponse importAllExportedFixtures(String actor) {
        Manifest manifest = loadManifest();
        if (manifest.fixtures() == null || manifest.fixtures().isEmpty()) {
            return new OmnicanalBulkImportResponse(0, 0, 0, 0, List.of());
        }

        int created = 0;
        int imports = 0;
        int failures = 0;
        List<OmnicanalBulkImportResponse.Item> items = new ArrayList<>();

        for (ManifestEntry entry : manifest.fixtures()) {
            if (entry == null || !StringUtils.hasText(entry.file())) {
                continue;
            }
            String codeFse = entry.codeFse();
            try {
                OmnicanalConnectivityImportRequest request = loadClasspath(EXPORTED_DIR + entry.file());
                ProviderEnsureResult ensured = ensureProvider(request, actor);
                if (ensured.created()) {
                    created++;
                }
                OmnicanalConnectivityImportResponse res = importConnectivity(
                        ensured.provider().getId(), request, actor);
                imports++;
                items.add(new OmnicanalBulkImportResponse.Item(
                        codeFse,
                        res.providerCode(),
                        res.providerId(),
                        ensured.created(),
                        res.operationsUpserted(),
                        res.apisUpserted(),
                        null
                ));
            } catch (Exception ex) {
                failures++;
                log.warn("Import Omnicanal FSE {} ignoré: {}", codeFse, ex.getMessage());
                items.add(new OmnicanalBulkImportResponse.Item(
                        codeFse,
                        entry.providerCodeHint(),
                        null,
                        false,
                        0,
                        0,
                        ex.getMessage()
                ));
            }
        }

        return new OmnicanalBulkImportResponse(
                items.size(),
                created,
                imports,
                failures,
                items
        );
    }

    /** Résout le classpath d'une fixture (exported/ puis legacy). */
    public Optional<String> resolveFixtureClasspath(UUID providerId) {
        PaymentProvider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new BusinessException("PROVIDER_NOT_FOUND", "Fournisseur introuvable",
                        HttpStatus.NOT_FOUND));
        return resolveFixtureClasspath(provider);
    }

    /** Résout le classpath d'une fixture (exported/ puis legacy). */
    public Optional<String> resolveFixtureClasspath(PaymentProvider provider) {
        if (provider == null) {
            return Optional.empty();
        }
        String codeFse = provider.getCodeFse();
        if (StringUtils.hasText(codeFse)) {
            try {
                Manifest manifest = loadManifest();
                for (ManifestEntry entry : manifest.fixtures()) {
                    if (codeFse.equals(entry.codeFse())) {
                        return Optional.of(EXPORTED_DIR + entry.file());
                    }
                }
            } catch (Exception ignored) {
                // manifest absent
            }
            return switch (codeFse.trim()) {
                case "0011" -> Optional.of(FIXTURE_CORIS_MONEY);
                case "0004" -> Optional.of(FIXTURE_CORIS_BANK);
                default -> Optional.empty();
            };
        }
        return switch (provider.getCode()) {
            case "CORIS_MONEY" -> Optional.of(FIXTURE_CORIS_MONEY);
            case "CORIS_BANK" -> Optional.of(FIXTURE_CORIS_BANK);
            default -> Optional.empty();
        };
    }

    public Manifest loadManifest() {
        try (InputStream in = new ClassPathResource(EXPORTED_MANIFEST).getInputStream()) {
            return objectMapper.readValue(in, Manifest.class);
        } catch (Exception ex) {
            throw new BusinessException("OMNI_MANIFEST_READ",
                    "Impossible de lire " + EXPORTED_MANIFEST + ": " + ex.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private record ProviderEnsureResult(PaymentProvider provider, boolean created) {}

    private ProviderEnsureResult ensureProvider(OmnicanalConnectivityImportRequest request, String actor) {
        String codeFse = blankToNull(request.codeFse());
        if (codeFse != null) {
            Optional<PaymentProvider> byFse = providerRepository.findByCodeFse(codeFse);
            if (byFse.isPresent()) {
                PaymentProvider existing = byFse.get();
                enrichFseMetadata(existing, request);
                return new ProviderEnsureResult(providerRepository.save(existing), false);
            }
        }
        String hint = blankToNull(request.providerCodeHint());
        if (hint != null) {
            Optional<PaymentProvider> byCode = providerRepository.findByCode(hint);
            if (byCode.isPresent()) {
                PaymentProvider existing = byCode.get();
                enrichFseMetadata(existing, request);
                if (codeFse != null && !codeFse.equals(blankToNull(existing.getCodeFse()))) {
                    existing.setCodeFse(codeFse);
                }
                return new ProviderEnsureResult(providerRepository.save(existing), false);
            }
        }
        PaymentProvider created = createProviderFromFixture(request, actor);
        return new ProviderEnsureResult(created, true);
    }

    private PaymentProvider createProviderFromFixture(OmnicanalConnectivityImportRequest request, String actor) {
        String code = resolveProviderCode(request);
        String libelle = StringUtils.hasText(request.providerLibelle())
                ? request.providerLibelle().trim()
                : code;
        PaymentProvider provider = PaymentProvider.builder()
                .code(code)
                .libelle(libelle)
                .description("Fournisseur importé Omnicanal FSE " + request.codeFse())
                .actif(false)
                .ordreAffichage(parseOrdre(request.codeFse()))
                .paymentMethodType(mapPaymentMethodType(request.participantKindHint()))
                .modeExecution(ExecutionMode.DIRECT)
                .build();
        enrichFseMetadata(provider, request);
        PaymentProvider saved = providerRepository.save(provider);
        auditService.record(actor, "PROVIDER_CREATE_OMNI", "PROVIDER", saved.getId().toString(),
                "Création depuis export Omnicanal " + saved.getCodeFse());
        return saved;
    }

    private void enrichFseMetadata(PaymentProvider provider, OmnicanalConnectivityImportRequest request) {
        if (blankToNull(provider.getCodeFse()) == null && StringUtils.hasText(request.codeFse())) {
            provider.setCodeFse(request.codeFse().trim());
        }
        if (blankToNull(provider.getCodeExterne()) == null && StringUtils.hasText(request.codeExterne())) {
            provider.setCodeExterne(request.codeExterne().trim());
        }
        if (blankToNull(provider.getGroupCode()) == null && StringUtils.hasText(request.groupCode())) {
            provider.setGroupCode(request.groupCode().trim().toUpperCase(Locale.ROOT));
        }
        if (provider.getParticipantKind() == null && StringUtils.hasText(request.participantKindHint())) {
            provider.setParticipantKind(parseParticipantKind(request.participantKindHint()));
        }
        if (provider.getFseStatut() == null && StringUtils.hasText(request.fseStatut())) {
            provider.setFseStatut(parseFseStatut(request.fseStatut()));
        }
        if (StringUtils.hasText(request.providerLibelle()) && provider.getLibelle().equals(provider.getCode())) {
            provider.setLibelle(request.providerLibelle().trim());
        }
    }

    private static String resolveProviderCode(OmnicanalConnectivityImportRequest request) {
        String hint = blankToNull(request.providerCodeHint());
        if (hint != null && !hint.startsWith("FSE_")) {
            return hint;
        }
        String codeFse = blankToNull(request.codeFse());
        if (codeFse != null) {
            return "FSE_" + codeFse;
        }
        return hint != null ? hint : "FSE_UNKNOWN";
    }

    private static Integer parseOrdre(String codeFse) {
        if (codeFse == null || codeFse.isBlank()) {
            return 999;
        }
        try {
            return Integer.parseInt(codeFse.trim());
        } catch (NumberFormatException ex) {
            return 999;
        }
    }

    private static PaymentMethodType mapPaymentMethodType(String hint) {
        return switch (parseParticipantKind(hint)) {
            case WALLET -> PaymentMethodType.MOBILE_MONEY;
            case BANQUE -> PaymentMethodType.BANK_TRANSFER;
            default -> PaymentMethodType.OTHER;
        };
    }

    private static FseParticipantKind parseParticipantKind(String raw) {
        if (raw == null || raw.isBlank()) {
            return FseParticipantKind.FACTURIER;
        }
        try {
            return FseParticipantKind.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return FseParticipantKind.FACTURIER;
        }
    }

    private static FseStatut parseFseStatut(String raw) {
        if (raw == null || raw.isBlank()) {
            return FseStatut.AUTRE;
        }
        try {
            return FseStatut.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return FseStatut.AUTRE;
        }
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private void applyFixtureIfProviderExists(String classpath, String providerCode) {
        providerRepository.findByCode(providerCode).ifPresent(provider -> {
            try {
                OmnicanalConnectivityImportRequest request = loadClasspath(classpath);
                // Upsert idempotent : crée/maj l'Api Omnicanal + rattache les méthodes
                // (même si les opérations existent déjà — cas migration V26)
                OmnicanalConnectivityImportResponse res =
                        importConnectivity(provider.getId(), request, "bootstrap");
                log.info("Connectivité Omnicanal {} → Api + {} méthode(s) sur {}",
                        providerCode, res.apisUpserted(), res.environmentCode());
            } catch (Exception ex) {
                log.warn("Import Omnicanal {} ignoré: {}", providerCode, ex.getMessage());
            }
        });
    }

    public OmnicanalConnectivityImportRequest loadClasspath(String path) {
        try (InputStream in = new ClassPathResource(path).getInputStream()) {
            return objectMapper.readValue(in, OmnicanalConnectivityImportRequest.class);
        } catch (Exception ex) {
            throw new BusinessException("OMNI_FIXTURE_READ", "Impossible de lire " + path + ": " + ex.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public OmnicanalConnectivityImportResponse importConnectivity(
            UUID providerId,
            OmnicanalConnectivityImportRequest request,
            String actor
    ) {
        PaymentProvider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new BusinessException("PROVIDER_NOT_FOUND", "Fournisseur introuvable",
                        HttpStatus.NOT_FOUND));

        String envCode = ProviderEnvironmentCode.parse(
                StringUtils.hasText(request.targetEnvironmentCode())
                        ? request.targetEnvironmentCode()
                        : ProviderEnvironmentCode.DEVELOPPEMENT.name()
        ).name();

        if (ProviderEnvironmentCode.SANDBOX.name().equals(envCode)) {
            throw new BusinessException("OMNI_SANDBOX_PROTECTED",
                    "Import Omnicanal interdit sur SANDBOX (protéger le checkout mock)");
        }

        environmentService.ensureStandardEnvironments(providerId, request.api().baseUrl());

        ProviderEnvironment env = environmentRepository.findByProviderIdAndCode(providerId, envCode)
                .orElseThrow(() -> new BusinessException("ENVIRONMENT_NOT_FOUND",
                        "Environnement " + envCode + " introuvable", HttpStatus.NOT_FOUND));

        // Met à jour l'URL DEVELOPPEMENT/PRODUCTION — sans activer
        env.setBaseUrl(request.api().baseUrl());
        if (env.getTimeoutSeconds() == null) {
            env.setTimeoutSeconds(30);
        }
        env.setActif(false);
        environmentRepository.save(env);

        ProviderFseApi fseApi = upsertFseApi(provider, env, request.api());
        boolean authUpserted = upsertFseApiAuth(fseApi, request.api());

        int ops = 0;
        int apis = 0;
        List<String> codes = new ArrayList<>();
        for (OmnicanalMethodSpec method : request.methods()) {
            if (method == null || !StringUtils.hasText(method.operationCode())) {
                continue;
            }
            String opCode = method.operationCode().trim().toUpperCase(Locale.ROOT);
            ProviderOperation operation = operationRepository.findByProviderIdAndCode(providerId, opCode)
                    .orElseGet(() -> ProviderOperation.builder()
                            .provider(provider)
                            .code(opCode)
                            .build());
            operation.setNom(StringUtils.hasText(method.nom()) ? method.nom() : opCode);
            operation.setDescription(buildDescription(method, request));
            operation.setIdempotent(false);
            operation.setActif(true);
            operation = operationRepository.save(operation);
            ops++;

            String apiCode = opCode;
            ProviderApi endpoint = apiRepository.findByProviderIdAndCode(providerId, apiCode)
                    .orElseGet(() -> ProviderApi.builder()
                            .provider(provider)
                            .code(apiCode)
                            .build());
            endpoint.setFseApi(fseApi);
            endpoint.setOperation(operation);
            endpoint.setEnvironment(null);
            endpoint.setNom(StringUtils.hasText(method.nom()) ? method.nom() : opCode);
            endpoint.setHttpMethod(normalizeHttp(method.httpMethod()));
            endpoint.setPath(normalizePath(method.path()));
            // Connecteurs Omnicanal (Coris / CBI /api/v1/soap/…) : @RequestParam form/query, pas JSON body
            endpoint.setContentType("application/x-www-form-urlencoded");
            endpoint.setTimeoutSeconds(30);
            endpoint.setRequestMappingJson(method.requestMappingJson());
            endpoint.setResponseMappingJson(null);
            endpoint.setRetryEnabled(false);
            endpoint.setMaxAttempts(1);
            endpoint.setRetryDelayMs(2000L);
            endpoint.setBackoffMultiplier(1.0);
            endpoint.setActif(true);
            apiRepository.save(endpoint);
            apis++;
            codes.add(opCode);
        }

        auditService.record(actor, "OMNI_CONNECTIVITY_IMPORT", "PROVIDER", providerId.toString(),
                "Import Omnicanal Api " + fseApi.getCode() + " → " + envCode + " (" + apis + " méthodes)");

        if (orchestrationSeedService != null) {
            try {
                orchestrationSeedService.seedInterfacesForProvider(provider);
            } catch (Exception ex) {
                log.warn("Seed interfaces utilisateur ignore pour {}: {}", provider.getCode(), ex.getMessage());
            }
        }

        return new OmnicanalConnectivityImportResponse(
                providerId,
                provider.getCode(),
                envCode,
                env.getId(),
                ops,
                apis,
                authUpserted,
                codes
        );
    }

    private ProviderFseApi upsertFseApi(PaymentProvider provider, ProviderEnvironment env, OmnicanalApiSpec api) {
        String code = StringUtils.hasText(api.codeApi())
                ? api.codeApi().trim().toUpperCase(Locale.ROOT)
                : "API_" + provider.getCode();
        String nom = StringUtils.hasText(api.nomApi()) ? api.nomApi().trim() : code;
        ProviderFseApi fseApi = fseApiRepository.findByProviderIdAndCode(provider.getId(), code)
                .orElseGet(() -> ProviderFseApi.builder()
                        .provider(provider)
                        .code(code)
                        .build());
        fseApi.setNom(nom);
        fseApi.setTypeApi(StringUtils.hasText(api.type()) ? api.type().trim().toUpperCase(Locale.ROOT) : "REST");
        fseApi.setBaseUrl(api.baseUrl());
        fseApi.setHost(api.host());
        fseApi.setPort(api.port());
        fseApi.setUseSsl(Boolean.TRUE.equals(api.useSsl()));
        fseApi.setUseProxy(Boolean.TRUE.equals(api.useProxy()));
        fseApi.setEnvironment(env);
        fseApi.setActif(true);
        return fseApiRepository.save(fseApi);
    }

    private boolean upsertFseApiAuth(ProviderFseApi fseApi, OmnicanalApiSpec api) {
        AuthType authType = mapAuthType(api.authType());
        fseApi.setAuthType(authType);
        fseApi.setClientId(api.clientId());
        fseApi.setTokenUrl(api.tokenUrl());
        if (StringUtils.hasText(api.grantType())) {
            fseApi.setExtraConfigJson("{\"grantType\":\"" + api.grantType().replace("\"", "")
                    + "\",\"source\":\"omnicanal\"}");
        } else {
            fseApi.setExtraConfigJson("{\"source\":\"omnicanal\"}");
        }
        if (StringUtils.hasText(api.clientSecret())) {
            fseApi.setClientSecretEnc(encryptionService.encrypt(api.clientSecret()));
        }
        if (StringUtils.hasText(api.username())) {
            fseApi.setUsernameEnc(encryptionService.encrypt(api.username()));
        }
        if (StringUtils.hasText(api.password())) {
            fseApi.setPasswordEnc(encryptionService.encrypt(api.password()));
        }
        fseApiRepository.save(fseApi);
        return authType != AuthType.NONE;
    }

    private static AuthType mapAuthType(String raw) {
        if (raw == null || raw.isBlank()) {
            return AuthType.NONE;
        }
        String n = raw.trim().toUpperCase(Locale.ROOT);
        return switch (n) {
            case "NONE", "NOAUTH" -> AuthType.NONE;
            case "BASIC" -> AuthType.BASIC;
            case "API_KEY" -> AuthType.API_KEY;
            case "BEARER", "BEARER_TOKEN" -> AuthType.BEARER_TOKEN;
            case "OAUTH", "OAUTH2", "OAUTH2_CLIENT_CREDENTIALS" -> AuthType.OAUTH2_CLIENT_CREDENTIALS;
            case "CUSTOM_HEADER" -> AuthType.CUSTOM_HEADER;
            case "MTLS" -> AuthType.MTLS;
            default -> {
                try {
                    yield AuthType.valueOf(n);
                } catch (Exception ex) {
                    yield AuthType.NONE;
                }
            }
        };
    }

    private static String normalizeHttp(String method) {
        if (method == null || method.isBlank() || "NONE".equalsIgnoreCase(method)) {
            return "POST";
        }
        return method.trim().toUpperCase(Locale.ROOT);
    }

    private static String normalizePath(String path) {
        if (path == null || path.isBlank()) {
            return "/";
        }
        return path.startsWith("/") ? path.substring(1) : path;
    }

    private static String buildDescription(OmnicanalMethodSpec method, OmnicanalConnectivityImportRequest request) {
        StringBuilder sb = new StringBuilder("Omnicanal");
        if (StringUtils.hasText(request.codeFse())) {
            sb.append(" FSE ").append(request.codeFse());
        }
        if (StringUtils.hasText(method.typeMethode())) {
            sb.append(" · ").append(method.typeMethode());
        }
        if (StringUtils.hasText(method.codeMethode())) {
            sb.append(" (").append(method.codeMethode()).append(")");
        }
        return sb.toString();
    }
}
