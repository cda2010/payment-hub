package com.gateway.payment.catalog.service;

import com.gateway.payment.catalog.api.dto.CatalogDeServiceRequest;
import com.gateway.payment.catalog.api.dto.CatalogDeServiceResponse;
import com.gateway.payment.catalog.api.dto.CatalogDeServiceVersionConfigRequest;
import com.gateway.payment.catalog.api.dto.CatalogDeServiceVersionRequest;
import com.gateway.payment.catalog.api.dto.CatalogDeServiceVersionResponse;
import com.gateway.payment.catalog.api.dto.CatalogProductCastingResponse;
import com.gateway.payment.catalog.api.dto.CatalogProductPublishRequest;
import com.gateway.payment.catalog.api.dto.CatalogProductPublishResponse;
import com.gateway.payment.catalog.api.dto.CatalogWorkflowBindingResponse;
import com.gateway.payment.catalog.api.dto.CatalogWorkflowStepReport;
import com.gateway.payment.common.domain.ProductAudience;
import com.gateway.payment.common.domain.ServiceTypeCode;
import com.gateway.payment.catalog.bpmn.CatalogWorkflowBindingService;
import com.gateway.payment.catalog.bpmn.CatalogWorkflowStepValidator;
import com.gateway.payment.catalog.casting.CatalogProductCastingService;
import com.gateway.payment.catalog.journey.CatalogJourneySettings;
import com.gateway.payment.catalog.domain.CatalogDeService;
import com.gateway.payment.catalog.domain.CatalogDeServiceStatus;
import com.gateway.payment.catalog.domain.CatalogDeServiceVersion;
import com.gateway.payment.catalog.domain.CategorieService;
import com.gateway.payment.catalog.i18n.CatalogConfigurationLocalizer;
import com.gateway.payment.catalog.repository.CatalogDeServiceRepository;
import com.gateway.payment.catalog.repository.CatalogDeServiceVersionRepository;
import com.gateway.payment.common.exception.BusinessException;
import com.gateway.payment.common.i18n.I18nScope;
import com.gateway.payment.common.i18n.TranslationService;
import org.flowable.engine.repository.ProcessDefinition;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class CatalogDeServiceService {

    private static final Pattern VERSION_PATTERN = Pattern.compile("^\\d+\\.\\d+$");

    private final CatalogDeServiceRepository repository;
    private final CatalogDeServiceVersionRepository versionRepository;
    private final CategorieServiceService categorieServiceService;
    private final CatalogWorkflowBindingService workflowBindingService;
    private final CatalogProductCastingService castingService;
    private final CatalogProductResolver catalogProductResolver;
    private final CatalogHostFlowService hostFlowService;
    private final TranslationService translations;
    private final CatalogConfigurationLocalizer configurationLocalizer;
    private final CatalogWorkflowStepValidator workflowStepValidator;

    public CatalogDeServiceService(
            CatalogDeServiceRepository repository,
            CatalogDeServiceVersionRepository versionRepository,
            CategorieServiceService categorieServiceService,
            CatalogWorkflowBindingService workflowBindingService,
            CatalogProductCastingService castingService,
            CatalogProductResolver catalogProductResolver,
            CatalogHostFlowService hostFlowService,
            TranslationService translations,
            CatalogConfigurationLocalizer configurationLocalizer,
            CatalogWorkflowStepValidator workflowStepValidator
    ) {
        this.repository = repository;
        this.versionRepository = versionRepository;
        this.categorieServiceService = categorieServiceService;
        this.workflowBindingService = workflowBindingService;
        this.castingService = castingService;
        this.catalogProductResolver = catalogProductResolver;
        this.hostFlowService = hostFlowService;
        this.translations = translations;
        this.configurationLocalizer = configurationLocalizer;
        this.workflowStepValidator = workflowStepValidator;
    }

    @Transactional(readOnly = true)
    public List<CatalogDeServiceResponse> listAll() {
        return toServiceResponses(repository.findAllOrderedByCategorieAndName());
    }

    @Transactional(readOnly = true)
    public List<CatalogDeServiceResponse> listPublished() {
        return listPublished(null);
    }

    /**
     * Liste les produits PUBLISHED, optionnellement filtrés par {@link ProductAudience}.
     * Sans politique {@code supportedAudiences} sur le produit → visible pour toutes les audiences.
     */
    @Transactional(readOnly = true)
    public List<CatalogDeServiceResponse> listPublished(String audienceRaw) {
        ProductAudience audience = ProductAudience.parse(audienceRaw);
        List<CatalogDeService> all =
                repository.findByStatusOrderedByCategorieAndName(CatalogDeServiceStatus.PUBLISHED);
        if (audience == null) {
            return toServiceResponses(all);
        }
        return toServiceResponses(all.stream()
                .filter(s -> supportsAudience(s, audience))
                .toList());
    }

    private static boolean supportsAudience(CatalogDeService service, ProductAudience audience) {
        CatalogDeServiceVersion version = service.getCurrentVersion();
        if (version == null) {
            return true;
        }
        return CatalogJourneySettings.from(version.getConfiguration()).supportsAudience(audience);
    }

    @Transactional(readOnly = true)
    public CatalogDeServiceResponse get(UUID id) {
        return toServiceResponse(requireService(id));
    }

    @Transactional
    public CatalogDeServiceResponse create(CatalogDeServiceRequest request) {
        String code = normalizeCode(request.code());
        if (repository.existsByCodeIgnoreCase(code)) {
            throw new BusinessException("CATALOG_SERVICE_EXISTS", "Service catalogue déjà existant: " + code);
        }
        CategorieService categorie = categorieServiceService.requireByCodeOrId(
                request.categoryCode(), request.categoryId());
        CatalogDeService entity = CatalogDeService.builder()
                .code(code)
                .name(request.name().trim())
                .description(request.description())
                .categorie(categorie)
                .status(request.status() != null ? request.status() : CatalogDeServiceStatus.DRAFT)
                .build();
        entity = repository.save(entity);

        String initialVersion = request.initialVersion() != null && !request.initialVersion().isBlank()
                ? normalizeVersion(request.initialVersion())
                : "1.0";
        CatalogDeServiceVersion version = CatalogDeServiceVersion.builder()
                .catalogDeService(entity)
                .version(initialVersion)
                .status(entity.getStatus())
                .effectiveFrom(Instant.now())
                .workflowKey(blankToNull(request.workflowKey()))
                .configuration(normalizeConfig(request.configuration()))
                .build();
        if (entity.getStatus() == CatalogDeServiceStatus.PUBLISHED) {
            workflowBindingService.requireDeployedProcess(version.getWorkflowKey());
        }
        version = versionRepository.save(version);
        entity.addVersion(version);
        entity.setCurrentVersion(version);
        saveServiceTranslations(entity, request.nameI18n(), request.descriptionI18n());
        saveConfigurationTranslations(version, request.translations());
        return toServiceResponse(repository.save(entity));
    }

    /**
     * Mise à jour des métadonnées produit uniquement (code, name, catégorie…).
     * Les changements workflow/config passent par {@link #createVersion}.
     */
    @Transactional
    public CatalogDeServiceResponse update(UUID id, CatalogDeServiceRequest request) {
        CatalogDeService entity = requireService(id);
        String code = normalizeCode(request.code());
        if (!entity.getCode().equalsIgnoreCase(code) && repository.existsByCodeIgnoreCase(code)) {
            throw new BusinessException("CATALOG_SERVICE_EXISTS", "Service catalogue déjà existant: " + code);
        }
        CategorieService categorie = categorieServiceService.requireByCodeOrId(
                request.categoryCode(), request.categoryId());
        entity.setCode(code);
        entity.setName(request.name().trim());
        entity.setDescription(request.description());
        entity.setCategorie(categorie);
        if (request.status() != null) {
            entity.setStatus(request.status());
        }
        saveServiceTranslations(entity, request.nameI18n(), request.descriptionI18n());
        return toServiceResponse(repository.save(entity));
    }

    @Transactional(readOnly = true)
    public List<CatalogDeServiceVersionResponse> listVersions(UUID serviceId) {
        requireService(serviceId);
        return versionRepository.findByCatalogDeServiceIdOrderByCreatedAtAsc(serviceId).stream()
                .map(this::toVersionResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CatalogDeServiceVersionResponse getVersion(UUID versionId) {
        return toVersionResponse(requireVersion(versionId));
    }

    /**
     * Crée une nouvelle version (ex. 1.0 → 1.1 / 2.0).
     * Si la version est PUBLISHED, elle devient la version courante et
     * la précédente PUBLISHED reçoit {@code effectiveTo = now}.
     */
    @Transactional
    public CatalogDeServiceVersionResponse createVersion(UUID serviceId, CatalogDeServiceVersionRequest request) {
        CatalogDeService service = requireService(serviceId);
        String versionLabel = normalizeVersion(request.version());
        if (versionRepository.existsByCatalogDeServiceIdAndVersion(serviceId, versionLabel)) {
            throw new BusinessException(
                    "CATALOG_SERVICE_VERSION_EXISTS",
                    "Version déjà existante: " + service.getCode() + " v" + versionLabel);
        }

        CatalogDeServiceStatus status = request.status() != null
                ? request.status()
                : CatalogDeServiceStatus.DRAFT;
        Instant effectiveFrom = request.effectiveFrom() != null ? request.effectiveFrom() : Instant.now();

        if (status == CatalogDeServiceStatus.PUBLISHED) {
            workflowBindingService.requireDeployedProcess(blankToNull(request.workflowKey()));
            closePreviousPublished(serviceId, effectiveFrom);
        }

        CatalogDeServiceVersion version = CatalogDeServiceVersion.builder()
                .catalogDeService(service)
                .version(versionLabel)
                .status(status)
                .effectiveFrom(effectiveFrom)
                .effectiveTo(request.effectiveTo())
                .workflowKey(blankToNull(request.workflowKey()))
                .configuration(normalizeConfig(request.configuration()))
                .build();
        version = versionRepository.save(version);
        service.addVersion(version);
        saveConfigurationTranslations(version, request.translations());

        if (status == CatalogDeServiceStatus.PUBLISHED) {
            service.setCurrentVersion(version);
            service.setStatus(CatalogDeServiceStatus.PUBLISHED);
            repository.save(service);
        }
        return toVersionResponse(version);
    }

    /**
     * Met à jour la configuration d'une version non publiée (roles, participants, steps…).
     */
    @Transactional
    public CatalogDeServiceVersionResponse updateDraftConfiguration(
            UUID versionId,
            CatalogDeServiceVersionConfigRequest request
    ) {
        CatalogDeServiceVersion version = requireVersion(versionId);
        if (version.getStatus() == CatalogDeServiceStatus.PUBLISHED
                || version.getStatus() == CatalogDeServiceStatus.ARCHIVED) {
            throw new BusinessException(
                    "CATALOG_SERVICE_VERSION_IMMUTABLE",
                    "Configuration figée pour une version " + version.getStatus()
                            + " — créez une nouvelle version");
        }
        version.setConfiguration(normalizeConfig(request != null ? request.configuration() : null));
        version = versionRepository.save(version);
        saveConfigurationTranslations(version, request != null ? request.translations() : null);
        return toVersionResponse(version);
    }

    /**
     * Publie une version : exige un {@code workflowKey} RapidBPMN déployé,
     * devient courante, archive la précédente.
     */
    @Transactional
    public CatalogDeServiceVersionResponse publishVersion(UUID versionId) {
        CatalogDeServiceVersion version = requireVersion(versionId);
        if (version.getStatus() == CatalogDeServiceStatus.ARCHIVED) {
            throw new BusinessException("CATALOG_SERVICE_VERSION_ARCHIVED", "Version archivée non publiable");
        }
        castingService.validate(castingService.parse(version.getConfiguration()));
        assertWorkflowBound(version);
        Instant now = Instant.now();
        closePreviousPublished(version.getCatalogDeService().getId(), now);
        version.setStatus(CatalogDeServiceStatus.PUBLISHED);
        if (version.getEffectiveFrom() == null) {
            version.setEffectiveFrom(now);
        }
        version.setEffectiveTo(null);
        version = versionRepository.save(version);

        CatalogDeService service = version.getCatalogDeService();
        service.setCurrentVersion(version);
        service.setStatus(CatalogDeServiceStatus.PUBLISHED);
        repository.save(service);
        ensureHostFlowIfConfigured(service.getCode(), version);
        catalogProductResolver.invalidate(service.getCode());
        catalogProductResolver.invalidateAll();
        return toVersionResponse(version);
    }

    /**
     * One-shot : crée (ou met à jour) un produit, publie la version, assure l'hôte technique.
     */
    @Transactional
    public CatalogProductPublishResponse publishProduct(CatalogProductPublishRequest request) {
        String code = normalizeCode(request.code());
        String versionLabel = request.version() != null && !request.version().isBlank()
                ? normalizeVersion(request.version())
                : "1.0";
        Map<String, Object> configuration = normalizeConfig(request.configuration());
        mergeGatewayHostHints(configuration, request);

        CatalogDeService service = repository.findByCodeIgnoreCase(code).orElse(null);
        if (service == null) {
            CatalogDeServiceResponse created = create(new CatalogDeServiceRequest(
                    code,
                    request.name(),
                    request.description(),
                    request.nameI18n(),
                    request.descriptionI18n(),
                    request.categoryCode(),
                    null,
                    CatalogDeServiceStatus.DRAFT,
                    versionLabel,
                    request.workflowKey(),
                    configuration,
                    request.translations()
            ));
            service = requireService(created.id());
        } else {
            CatalogDeServiceVersion existing = versionRepository
                    .findByCatalogDeServiceIdAndVersion(service.getId(), versionLabel)
                    .orElse(null);
            if (existing == null) {
                createVersion(service.getId(), new CatalogDeServiceVersionRequest(
                        versionLabel,
                        CatalogDeServiceStatus.DRAFT,
                        Instant.now(),
                        null,
                        request.workflowKey(),
                        configuration,
                        request.translations()
                ));
            } else if (existing.getStatus() != CatalogDeServiceStatus.PUBLISHED
                    && existing.getStatus() != CatalogDeServiceStatus.ARCHIVED) {
                existing.setWorkflowKey(blankToNull(request.workflowKey()));
                existing.setConfiguration(configuration);
                versionRepository.save(existing);
                saveConfigurationTranslations(existing, request.translations());
            } else {
                createVersion(service.getId(), new CatalogDeServiceVersionRequest(
                        bumpPatch(versionLabel),
                        CatalogDeServiceStatus.DRAFT,
                        Instant.now(),
                        null,
                        request.workflowKey(),
                        configuration,
                        request.translations()
                ));
            }
        }

        final UUID serviceId = service.getId();
        CatalogDeServiceVersion toPublish = versionRepository
                .findFirstByCatalogDeServiceIdAndStatusOrderByEffectiveFromDesc(
                        serviceId, CatalogDeServiceStatus.DRAFT)
                .or(() -> versionRepository.findByCatalogDeServiceIdAndVersion(serviceId, versionLabel))
                .orElseThrow(() -> new BusinessException(
                        "CATALOG_SERVICE_VERSION_NOT_FOUND",
                        "Aucune version à publier pour " + code));

        if (toPublish.getStatus() != CatalogDeServiceStatus.PUBLISHED) {
            toPublish.setWorkflowKey(blankToNull(request.workflowKey()));
            if (configuration != null && !configuration.isEmpty()) {
                toPublish.setConfiguration(configuration);
            }
            versionRepository.save(toPublish);
            saveConfigurationTranslations(toPublish, request.translations());
            publishVersion(toPublish.getId());
            toPublish = requireVersion(toPublish.getId());
        }

        boolean ensureHost = request.ensureHostFlow() == null || Boolean.TRUE.equals(request.ensureHostFlow());
        CatalogHostFlowService.HostFlowResult host = null;
        if (ensureHost) {
            host = ensureHostFlowIfConfigured(code, toPublish);
            if (host == null) {
                ServiceTypeCode st = CatalogProductResolver.mapToServiceType(code, toPublish.getConfiguration());
                Map<String, Object> hostCfg = new LinkedHashMap<>(
                        CatalogHostFlowService.extractHostFlowConfig(toPublish.getConfiguration()));
                if (request.hostProviderCode() != null) {
                    hostCfg.put("providerCode", request.hostProviderCode());
                }
                if (request.hostParticipantKind() != null) {
                    hostCfg.put("participantKind", request.hostParticipantKind());
                }
                hostCfg.putIfAbsent("autoCreate", true);
                host = hostFlowService.ensureHostFlow(
                        code, toPublish.getWorkflowKey(), st, hostCfg);
            }
        }

        CatalogProductPublishResponse.CatalogHostFlowServiceView hostView = host == null ? null
                : new CatalogProductPublishResponse.CatalogHostFlowServiceView(
                        host.providerCode(),
                        host.workflowKey(),
                        host.serviceType() != null ? host.serviceType().name() : null,
                        host.created());
        return new CatalogProductPublishResponse(
                toServiceResponse(requireService(service.getId())),
                toVersionResponse(requireVersion(toPublish.getId())),
                hostView
        );
    }

    private CatalogHostFlowService.HostFlowResult ensureHostFlowIfConfigured(
            String productCode,
            CatalogDeServiceVersion version
    ) {
        Map<String, Object> hostCfg = CatalogHostFlowService.extractHostFlowConfig(version.getConfiguration());
        Object auto = hostCfg.get("autoCreate");
        if (auto == null) {
            auto = hostCfg.get("auto_create");
        }
        // Défaut : créer l'hôte si workflow_key présent et pas de refus explicite
        boolean create = auto == null || Boolean.parseBoolean(String.valueOf(auto));
        if (!create || version.getWorkflowKey() == null || version.getWorkflowKey().isBlank()) {
            return null;
        }
        ServiceTypeCode st = CatalogProductResolver.mapToServiceType(productCode, version.getConfiguration());
        return hostFlowService.ensureHostFlow(productCode, version.getWorkflowKey(), st, hostCfg);
    }

    private static void mergeGatewayHostHints(
            Map<String, Object> configuration,
            CatalogProductPublishRequest request
    ) {
        if (configuration == null) {
            return;
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> gateway = configuration.get("gateway") instanceof Map<?, ?> m
                ? new LinkedHashMap<>((Map<String, Object>) m)
                : new LinkedHashMap<>();
        @SuppressWarnings("unchecked")
        Map<String, Object> hostFlow = gateway.get("hostFlow") instanceof Map<?, ?> m
                ? new LinkedHashMap<>((Map<String, Object>) m)
                : new LinkedHashMap<>();
        if (request.hostProviderCode() != null && !request.hostProviderCode().isBlank()) {
            hostFlow.put("providerCode", request.hostProviderCode().trim());
        }
        if (request.hostParticipantKind() != null && !request.hostParticipantKind().isBlank()) {
            hostFlow.put("participantKind", request.hostParticipantKind().trim());
        }
        if (request.ensureHostFlow() == null || Boolean.TRUE.equals(request.ensureHostFlow())) {
            hostFlow.putIfAbsent("autoCreate", true);
        }
        if (!hostFlow.isEmpty()) {
            gateway.put("hostFlow", hostFlow);
            configuration.put("gateway", gateway);
        }
    }

    private static String bumpPatch(String version) {
        String[] parts = version.split("\\.");
        int major = Integer.parseInt(parts[0]);
        int minor = Integer.parseInt(parts[1]) + 1;
        return major + "." + minor;
    }

    /**
     * Vérifie le binding version → process RapidBPMN déployé (sans générer de XML).
     */
    @Transactional(readOnly = true)
    public CatalogWorkflowBindingResponse resolveWorkflow(UUID versionId) {
        CatalogDeServiceVersion version = requireVersion(versionId);
        ProcessDefinition def = workflowBindingService.requireDeployedProcess(version.getWorkflowKey());
        return toBinding(version, def);
    }

    /**
     * Casting PUBLISHED d'un produit (roles / participants / bindings) pour l'UI runtime.
     */
    @Transactional(readOnly = true)
    public CatalogProductCastingResponse getPublishedCasting(String productCode) {
        String code = normalizeCode(productCode);
        CatalogDeService service = repository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new BusinessException(
                        "CATALOG_SERVICE_NOT_FOUND",
                        "Service catalogue introuvable: " + code,
                        HttpStatus.NOT_FOUND));
        CatalogDeServiceVersion version = service.getCurrentVersion();
        if (version == null || version.getStatus() != CatalogDeServiceStatus.PUBLISHED) {
            version = versionRepository
                    .findFirstByCatalogDeServiceIdAndStatusOrderByEffectiveFromDesc(
                            service.getId(), CatalogDeServiceStatus.PUBLISHED)
                    .orElseThrow(() -> new BusinessException(
                            "CATALOG_SERVICE_NOT_PUBLISHED",
                            "Aucune version PUBLISHED pour: " + code));
        }
        Map<String, Object> configuration = version.getConfiguration() != null
                ? new LinkedHashMap<>(version.getConfiguration())
                : new LinkedHashMap<>();
        Map<String, Object> normalized = configurationLocalizer.localize(
                version.getId(), castingService.normalizeConfiguration(configuration));
        CatalogJourneySettings journey = CatalogJourneySettings.from(normalized);
        return new CatalogProductCastingResponse(
                service.getCode(),
                version.getId(),
                version.getVersion(),
                version.getWorkflowKey(),
                castList(normalized.get("roles")),
                castList(normalized.get("participants")),
                castList(normalized.get("bindings")),
                journey.journeyCode(),
                journey.supportedChannels().stream().map(Enum::name).toList(),
                journey.defaultChannel() != null ? journey.defaultChannel().name() : null,
                journey.supportedAudiences().stream().map(Enum::name).toList(),
                journey.defaultAudience() != null ? journey.defaultAudience().name() : null
        );
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> castList(Object raw) {
        if (raw instanceof List<?> list) {
            return (List<Map<String, Object>>) list;
        }
        return List.of();
    }

    private void assertWorkflowBound(CatalogDeServiceVersion version) {
        workflowBindingService.requireDeployedProcess(version.getWorkflowKey());
        workflowStepValidator.requireValid(version.getWorkflowKey(), version.getConfiguration());
    }

    /**
     * Contrôle des serviceTasks du process lié face à la configuration de la version (sans publier).
     */
    @Transactional(readOnly = true)
    public CatalogWorkflowStepReport checkWorkflowSteps(UUID versionId) {
        CatalogDeServiceVersion version = requireVersion(versionId);
        return workflowStepValidator.check(version.getWorkflowKey(), version.getConfiguration());
    }

    private static CatalogWorkflowBindingResponse toBinding(
            CatalogDeServiceVersion version,
            ProcessDefinition def
    ) {
        return new CatalogWorkflowBindingResponse(
                def.getKey(),
                def.getName(),
                def.getVersion(),
                def.getDeploymentId(),
                version.getCatalogDeService().getCode(),
                version.getVersion(),
                true
        );
    }

    private void closePreviousPublished(UUID serviceId, Instant effectiveTo) {
        versionRepository.findFirstByCatalogDeServiceIdAndStatusOrderByEffectiveFromDesc(
                        serviceId, CatalogDeServiceStatus.PUBLISHED)
                .ifPresent(previous -> {
                    previous.setEffectiveTo(effectiveTo);
                    previous.setStatus(CatalogDeServiceStatus.ARCHIVED);
                    versionRepository.save(previous);
                });
    }

    private CatalogDeService requireService(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "CATALOG_SERVICE_NOT_FOUND",
                        "Service catalogue introuvable: " + id,
                        HttpStatus.NOT_FOUND));
    }

    private CatalogDeServiceVersion requireVersion(UUID id) {
        return versionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "CATALOG_SERVICE_VERSION_NOT_FOUND",
                        "Version catalogue introuvable: " + id,
                        HttpStatus.NOT_FOUND));
    }

    private void saveServiceTranslations(
            CatalogDeService entity,
            Map<String, String> nameI18n,
            Map<String, String> descriptionI18n
    ) {
        Map<String, Map<String, String>> byPath = new LinkedHashMap<>();
        byPath.put(I18nScope.FIELD_NAME, translations.withDefaultLanguage(nameI18n, entity.getName()));
        byPath.put(I18nScope.FIELD_DESCRIPTION,
                translations.withDefaultLanguage(descriptionI18n, entity.getDescription()));
        translations.saveAll(I18nScope.CATALOG_DE_SERVICE, entity.getId(), byPath);
    }

    /**
     * Amorce les libellés de la configuration dans la langue par défaut et applique
     * les traductions fournies par l'appelant.
     */
    private void saveConfigurationTranslations(
            CatalogDeServiceVersion version,
            Map<String, Map<String, String>> provided
    ) {
        translations.saveAll(
                I18nScope.CATALOG_DE_SERVICE_VERSION,
                version.getId(),
                configurationLocalizer.merge(version.getConfiguration(), provided));
    }

    private List<CatalogDeServiceResponse> toServiceResponses(List<CatalogDeService> services) {
        Map<UUID, TranslationService.Bundle> serviceBundles = translations.bundles(
                I18nScope.CATALOG_DE_SERVICE, services.stream().map(CatalogDeService::getId).toList());
        Map<UUID, TranslationService.Bundle> categoryBundles = translations.bundles(
                I18nScope.CATEGORIE_SERVICE,
                services.stream()
                        .map(CatalogDeService::getCategorie)
                        .filter(Objects::nonNull)
                        .map(CategorieService::getId)
                        .distinct()
                        .toList());
        return services.stream()
                .map(s -> toServiceResponse(
                        s,
                        serviceBundles.get(s.getId()),
                        s.getCategorie() != null ? categoryBundles.get(s.getCategorie().getId()) : null))
                .toList();
    }

    private CatalogDeServiceResponse toServiceResponse(CatalogDeService s) {
        CategorieService cat = s.getCategorie();
        return toServiceResponse(
                s,
                translations.bundle(I18nScope.CATALOG_DE_SERVICE, s.getId()),
                cat != null ? translations.bundle(I18nScope.CATEGORIE_SERVICE, cat.getId()) : null);
    }

    private static CatalogDeServiceResponse toServiceResponse(
            CatalogDeService s,
            TranslationService.Bundle bundle,
            TranslationService.Bundle categoryBundle
    ) {
        CatalogDeServiceVersion current = s.getCurrentVersion();
        CategorieService cat = s.getCategorie();
        String categoryName = cat == null ? null
                : categoryBundle != null
                        ? categoryBundle.text(I18nScope.FIELD_NAME, cat.getName())
                        : cat.getName();
        return new CatalogDeServiceResponse(
                s.getId(),
                s.getCode(),
                bundle.text(I18nScope.FIELD_NAME, s.getName()),
                bundle.text(I18nScope.FIELD_DESCRIPTION, s.getDescription()),
                bundle.messages(I18nScope.FIELD_NAME),
                bundle.messages(I18nScope.FIELD_DESCRIPTION),
                cat != null ? cat.getId() : null,
                cat != null ? cat.getCode() : null,
                categoryName,
                s.getStatus(),
                current != null ? current.getId() : null,
                current != null ? current.getVersion() : null,
                s.getCreatedAt(),
                s.getUpdatedAt()
        );
    }

    /** Configuration brute + traductions : l'admin édite les libellés langue par langue. */
    private CatalogDeServiceVersionResponse toVersionResponse(CatalogDeServiceVersion v) {
        return new CatalogDeServiceVersionResponse(
                v.getId(),
                v.getCatalogDeService().getId(),
                v.getCatalogDeService().getCode(),
                v.getVersion(),
                v.getStatus(),
                v.getEffectiveFrom(),
                v.getEffectiveTo(),
                v.getWorkflowKey(),
                v.getConfiguration(),
                translations.bundle(I18nScope.CATALOG_DE_SERVICE_VERSION, v.getId()).byPath(),
                v.getCreatedAt(),
                v.getUpdatedAt()
        );
    }

    private static String normalizeCode(String raw) {
        return raw.trim().toUpperCase(Locale.ROOT).replace(' ', '_').replace('-', '_');
    }

    private static String normalizeVersion(String raw) {
        String v = raw.trim();
        if (v.startsWith("v") || v.startsWith("V")) {
            v = v.substring(1);
        }
        if (!VERSION_PATTERN.matcher(v).matches()) {
            throw new BusinessException(
                    "CATALOG_SERVICE_VERSION_INVALID",
                    "Format de version invalide (attendu x.y): " + raw);
        }
        return v;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private Map<String, Object> normalizeConfig(Map<String, Object> configuration) {
        return castingService.normalizeConfiguration(
                configuration == null ? new LinkedHashMap<>() : configuration);
    }
}
