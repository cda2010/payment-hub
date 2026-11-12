package com.gateway.payment.omnicanal.external;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.payment.common.domain.AuthType;
import com.gateway.payment.common.domain.ExecutionMode;
import com.gateway.payment.common.domain.PaymentMethodType;
import com.gateway.payment.configuration.api.dto.ApiResponse;
import com.gateway.payment.configuration.api.dto.AuthenticationResponse;
import com.gateway.payment.configuration.api.dto.EnvironmentResponse;
import com.gateway.payment.configuration.api.dto.FseApiResponse;
import com.gateway.payment.configuration.api.dto.ProviderRequest;
import com.gateway.payment.configuration.api.dto.ProviderResponse;
import com.gateway.payment.configuration.domain.FseStatut;
import com.gateway.payment.configuration.domain.PaymentProvider;
import com.gateway.payment.configuration.repository.PaymentProviderRepository;
import com.gateway.payment.orchestration.api.dto.InterfaceUtilisateurResponse;
import com.gateway.payment.orchestration.api.dto.ParametreUtilisateurResponse;
import com.gateway.payment.orchestration.domain.TypeEnvoi;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Projette le catalogue FSE de l'omnicanal externe vers les DTO des onglets fournisseur.
 */
@Service
public class OmnicanalProviderCatalog {

    private final ExternalOmnicanalClient client;
    private final ExternalOmnicanalProperties properties;
    private final PaymentProviderRepository providers;
    private final ObjectMapper json;

    public OmnicanalProviderCatalog(
            ExternalOmnicanalClient client,
            ExternalOmnicanalProperties properties,
            PaymentProviderRepository providers,
            ObjectMapper json
    ) {
        this.client = client;
        this.properties = properties;
        this.providers = providers;
        this.json = json;
    }

    public boolean supplies(UUID providerId) {
        return load(providerId).isPresent();
    }

    /**
     * Fournisseurs de l'omnicanal. {@code null} si le microservice n'est pas utilisé.
     */
    public List<ProviderResponse> providers() {
        if (!properties.isEnabled()) {
            return null;
        }
        List<ProviderResponse> out = new ArrayList<>();
        for (Map<String, Object> row : client.fournisseurs()) {
            String codeFse = text(row.get("codeFse"));
            if (codeFse != null) {
                out.add(toProvider(codeFse, row));
            }
        }
        return out;
    }

    public Optional<ProviderResponse> byCode(String codeFse) {
        if (!StringUtils.hasText(codeFse)) {
            return Optional.empty();
        }
        return provider(idOf(codeFse.trim()));
    }

    public UUID idOfCode(String codeFse) {
        return idOf(codeFse);
    }

    public Optional<ProviderResponse> provider(UUID providerId) {
        List<ProviderResponse> all = providers();
        if (all == null) {
            return Optional.empty();
        }
        return all.stream().filter(provider -> providerId.equals(provider.id())).findFirst();
    }

    /**
     * Enregistre la fiche générale dans {@code fournisseur_service} de l'omnicanal.
     *
     * @param codeFse code actuel de la ligne
     */
    public ProviderResponse updateProvider(String codeFse, ProviderRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("nom", request.libelle());
        body.put("codeFse", renamedCode(codeFse, request));
        body.put("groupe", request.groupCode());
        body.put("codeExterne", request.codeExterne());
        body.put("statut", request.fseStatut() == null ? null : request.fseStatut().name());
        body.put("typeMoyen", request.paymentMethodType() == null ? null : request.paymentMethodType().name());
        body.put("modeExecution", request.modeExecution() == null ? null : request.modeExecution().name());
        body.put("pays", request.countryCode());
        Map<String, Object> saved = client.updateFournisseur(codeFse, body);
        String savedCode = text(saved.get("codeFse"));
        return toProvider(StringUtils.hasText(savedCode) ? savedCode : codeFse, saved);
    }

    /**
     * Le formulaire expose le code FSE deux fois (code et code FSE). Un seul des deux
     * suffit pour renommer la ligne.
     */
    private static String renamedCode(String current, ProviderRequest request) {
        String requestedFse = text(request.codeFse());
        String requestedCode = text(request.code());
        if (StringUtils.hasText(requestedFse) && !requestedFse.equals(current)) {
            return requestedFse;
        }
        if (StringUtils.hasText(requestedCode) && !requestedCode.equals(current)) {
            return requestedCode;
        }
        return current;
    }

    public ProviderResponse overlay(ProviderResponse local) {
        Optional<Map<String, Object>> body = load(local.id());
        if (body.isEmpty()) {
            return local;
        }
        Map<String, Object> fse = body.get();
        String nom = text(fse.get("nom"));
        return new ProviderResponse(
                local.id(),
                local.code(),
                StringUtils.hasText(nom) ? nom : local.libelle(),
                local.description(),
                local.logoUrl(),
                local.actif(),
                local.ordreAffichage(),
                enumValue(PaymentMethodType.class, fse.get("typeMoyen")).orElse(local.paymentMethodType()),
                enumValue(ExecutionMode.class, fse.get("modeExecution")).orElse(local.modeExecution()),
                local.serviceTypes(),
                text(fse.get("codeFse")),
                firstText(fse.get("codeExterne"), local.codeExterne()),
                firstText(fse.get("groupe"), local.groupCode()),
                firstText(fse.get("pays"), local.countryCode()),
                local.participantKind(),
                statut(fse.get("statut")).orElse(local.fseStatut()),
                local.createdAt(),
                local.updatedAt()
        );
    }

    public List<FseApiResponse> fseApis(UUID providerId) {
        return load(providerId).map(body -> apis(providerId, body).stream().map(ApiView::fse).toList()).orElse(null);
    }

    public List<ApiResponse> methods(UUID providerId) {
        return load(providerId).map(body -> apis(providerId, body).stream()
                .flatMap(api -> api.methods().stream())
                .toList()).orElse(null);
    }

    public List<ApiResponse> methodsOf(UUID providerId, UUID fseApiId) {
        List<ApiResponse> all = methods(providerId);
        if (all == null) {
            return null;
        }
        return all.stream().filter(method -> fseApiId.equals(method.fseApiId())).toList();
    }

    public List<EnvironmentResponse> environments(UUID providerId) {
        return load(providerId).map(body -> apis(providerId, body).stream().map(ApiView::environment).toList()).orElse(null);
    }

    public AuthenticationResponse authentication(UUID providerId, UUID environmentId) {
        return load(providerId)
                .flatMap(body -> apis(providerId, body).stream()
                        .filter(api -> environmentId.equals(api.environment.id()))
                        .findFirst()
                        .map(ApiView::authentication))
                .orElse(null);
    }

    public List<InterfaceUtilisateurResponse> interfaces(UUID providerId) {
        Optional<Map<String, Object>> body = load(providerId);
        if (body.isEmpty()) {
            return null;
        }
        String codeFse = text(body.get().get("codeFse"));
        List<InterfaceUtilisateurResponse> out = new ArrayList<>();
        for (Object raw : list(body.get().get("interfaces"))) {
            if (raw instanceof Map<?, ?> map) {
                out.add(toInterface(providerId, codeFse, map));
            }
        }
        return out;
    }

    private Optional<Map<String, Object>> load(UUID providerId) {
        if (!properties.isEnabled()) {
            return Optional.empty();
        }
        String codeFse = codeFse(providerId);
        if (!StringUtils.hasText(codeFse)) {
            return Optional.empty();
        }
        Map<String, Object> body = client.catalog(codeFse);
        if (!Boolean.TRUE.equals(body.get("found"))) {
            return Optional.empty();
        }
        return Optional.of(body);
    }

    private List<ApiView> apis(UUID providerId, Map<String, Object> body) {
        List<ApiView> out = new ArrayList<>();
        for (Object raw : list(body.get("apis"))) {
            if (raw instanceof Map<?, ?> map) {
                out.add(toApi(providerId, map));
            }
        }
        return out;
    }

    private ApiView toApi(UUID providerId, Map<?, ?> raw) {
        long id = number(raw.get("id"));
        UUID apiId = stableId("omni-api-", id);
        UUID envId = stableId("omni-env-", id);
        String code = text(raw.get("code"));
        String host = text(raw.get("host"));
        String port = text(raw.get("port"));
        boolean ssl = Boolean.TRUE.equals(raw.get("useSsl"));
        String baseUrl = baseUrl(ssl, host, port);
        AuthType authType = authType(text(raw.get("authType")));
        boolean actif = raw.get("actif") == null || Boolean.TRUE.equals(raw.get("actif"));
        List<ApiResponse> methods = new ArrayList<>();
        for (Object methodRaw : list(raw.get("methods"))) {
            if (methodRaw instanceof Map<?, ?> method) {
                methods.add(toMethod(providerId, apiId, envId, code, method));
            }
        }
        FseApiResponse fse = new FseApiResponse(
                apiId,
                providerId,
                envId,
                code,
                code,
                text(raw.get("nom")),
                text(raw.get("type")),
                baseUrl,
                host,
                port,
                ssl,
                Boolean.TRUE.equals(raw.get("useProxy")),
                actif,
                methods.size(),
                authType,
                text(raw.get("clientId")),
                text(raw.get("tokenUrl")),
                text(raw.get("grantType")),
                null,
                Boolean.TRUE.equals(raw.get("hasClientSecret")),
                false,
                Boolean.TRUE.equals(raw.get("hasUsername")),
                Boolean.TRUE.equals(raw.get("hasPassword")),
                false,
                null
        );
        EnvironmentResponse environment = new EnvironmentResponse(
                envId, providerId, code, baseUrl, 30, actif, null, null);
        AuthenticationResponse authentication = new AuthenticationResponse(
                stableId("omni-auth-", id),
                providerId,
                envId,
                authType,
                text(raw.get("clientId")),
                text(raw.get("tokenUrl")),
                null,
                null,
                Boolean.TRUE.equals(raw.get("hasClientSecret")),
                false,
                Boolean.TRUE.equals(raw.get("hasUsername")),
                Boolean.TRUE.equals(raw.get("hasPassword")),
                false,
                null,
                null
        );
        return new ApiView(fse, environment, authentication, methods);
    }

    private ApiResponse toMethod(UUID providerId, UUID apiId, UUID envId, String apiCode, Map<?, ?> raw) {
        long id = number(raw.get("id"));
        String typeMethode = text(raw.get("typeMethode"));
        String path = text(raw.get("nomMethode"));
        return new ApiResponse(
                stableId("omni-method-", id),
                providerId,
                apiId,
                apiCode,
                envId,
                stableId("omni-op-", id),
                typeMethode,
                path,
                text(raw.get("typeAppel")),
                path,
                text(raw.get("typeReponse")),
                null,
                mapping(list(raw.get("parametres"))),
                null,
                false,
                null,
                null,
                null,
                null,
                true,
                null
        );
    }

    private String mapping(List<?> params) {
        Map<String, String> map = new LinkedHashMap<>();
        for (Object raw : params) {
            if (!(raw instanceof Map<?, ?> param)) {
                continue;
            }
            String external = text(param.get("nomExterne"));
            String code = text(param.get("codeParametre"));
            if (StringUtils.hasText(external) && StringUtils.hasText(code)) {
                map.put(external, "${" + code + "}");
            }
        }
        if (map.isEmpty()) {
            return null;
        }
        try {
            return json.writeValueAsString(map);
        } catch (JsonProcessingException ex) {
            return null;
        }
    }

    private String codeFse(UUID providerId) {
        PaymentProvider provider = providers.findById(providerId).orElse(null);
        if (provider != null && StringUtils.hasText(provider.getCodeFse())) {
            return provider.getCodeFse().trim();
        }
        for (Map<String, Object> row : client.fournisseurs()) {
            String codeFse = text(row.get("codeFse"));
            if (codeFse != null && providerId.equals(idOf(codeFse))) {
                return codeFse;
            }
        }
        return null;
    }

    private ProviderResponse toProvider(String codeFse, Map<String, Object> row) {
        return new ProviderResponse(
                idOf(codeFse),
                codeFse,
                firstText(row.get("nom"), codeFse),
                null,
                null,
                true,
                ordre(codeFse),
                enumValue(PaymentMethodType.class, row.get("typeMoyen")).orElse(PaymentMethodType.OTHER),
                enumValue(ExecutionMode.class, row.get("modeExecution")).orElse(ExecutionMode.DIRECT),
                List.of(),
                codeFse,
                text(row.get("codeExterne")),
                text(row.get("groupe")),
                text(row.get("pays")),
                null,
                statut(row.get("statut")).orElse(null),
                null,
                null
        );
    }

    private static UUID idOf(String codeFse) {
        return UUID.nameUUIDFromBytes(("omni-fse-" + codeFse).getBytes(StandardCharsets.UTF_8));
    }

    private static Integer ordre(String codeFse) {
        try {
            return Integer.parseInt(codeFse);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private InterfaceUtilisateurResponse toInterface(UUID providerId, String codeFse, Map<?, ?> raw) {
        long id = number(raw.get("id"));
        UUID interfaceId = stableId("omni-iu-", id);
        String typeMethode = text(raw.get("typeMethode"));
        List<ParametreUtilisateurResponse> params = new ArrayList<>();
        int ordre = 1;
        for (Object paramRaw : list(raw.get("parametres"))) {
            if (paramRaw instanceof Map<?, ?> param) {
                params.add(new ParametreUtilisateurResponse(
                        stableId("omni-iup-", number(param.get("id"))),
                        interfaceId,
                        text(param.get("nomParametre")),
                        text(param.get("codeParametre")),
                        text(param.get("codeParametreMethode")),
                        text(param.get("typeParametre")),
                        text(param.get("valeur")),
                        null,
                        text(param.get("attribut")),
                        null,
                        null,
                        null,
                        null,
                        true,
                        ordre++,
                        null
                ));
            }
        }
        return new InterfaceUtilisateurResponse(
                interfaceId,
                providerId,
                codeFse,
                codeFse,
                null,
                typeMethode,
                TypeEnvoi.REQUEST,
                null,
                text(raw.get("methodeCode")),
                null,
                text(raw.get("methodeNom")),
                text(raw.get("codeInterface")),
                text(raw.get("nomInterface")),
                true,
                Instant.EPOCH,
                params
        );
    }

    private static AuthType authType(String raw) {
        if (!StringUtils.hasText(raw)) {
            return AuthType.NONE;
        }
        return switch (raw.trim().toUpperCase()) {
            case "OAUTH2" -> AuthType.OAUTH2_CLIENT_CREDENTIALS;
            case "BASIC" -> AuthType.BASIC;
            case "OAUTH" -> AuthType.BEARER_TOKEN;
            case "NOAUTH", "NONE" -> AuthType.NONE;
            default -> AuthType.NONE;
        };
    }

    private static <E extends Enum<E>> Optional<E> enumValue(Class<E> type, Object raw) {
        String value = text(raw);
        if (!StringUtils.hasText(value)) {
            return Optional.empty();
        }
        try {
            return Optional.of(Enum.valueOf(type, value.trim().toUpperCase()));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    private static Optional<FseStatut> statut(Object raw) {
        String value = text(raw);
        if (!StringUtils.hasText(value)) {
            return Optional.empty();
        }
        try {
            return Optional.of(FseStatut.valueOf(value.trim().toUpperCase()));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    private static String baseUrl(boolean ssl, String host, String port) {
        if (!StringUtils.hasText(host)) {
            return null;
        }
        String scheme = ssl ? "https" : "http";
        if (!StringUtils.hasText(port)) {
            return scheme + "://" + host;
        }
        return scheme + "://" + host + ":" + port;
    }

    private static UUID stableId(String prefix, long id) {
        return UUID.nameUUIDFromBytes((prefix + id).getBytes(StandardCharsets.UTF_8));
    }

    private static long number(Object raw) {
        if (raw instanceof Number number) {
            return number.longValue();
        }
        return 0L;
    }

    private static String text(Object raw) {
        if (raw == null) {
            return null;
        }
        String value = String.valueOf(raw).trim();
        return value.isEmpty() || "null".equals(value) ? null : value;
    }

    private static String firstText(Object preferred, String fallback) {
        String value = text(preferred);
        return StringUtils.hasText(value) ? value : fallback;
    }

    @SuppressWarnings("unchecked")
    private static List<Object> list(Object raw) {
        if (raw instanceof List<?> values) {
            return (List<Object>) values;
        }
        return List.of();
    }

    private record ApiView(
            FseApiResponse fse,
            EnvironmentResponse environment,
            AuthenticationResponse authentication,
            List<ApiResponse> methods
    ) {
    }
}
