package com.gateway.payment.payment.application;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.payment.common.crypto.SecretEncryptionService;
import com.gateway.payment.common.domain.AuthType;
import com.gateway.payment.common.domain.PaymentStatus;
import com.gateway.payment.configuration.domain.PaymentProvider;
import com.gateway.payment.configuration.domain.ProviderApi;
import com.gateway.payment.configuration.domain.ProviderEnvironment;
import com.gateway.payment.configuration.domain.ProviderFseApi;
import com.gateway.payment.configuration.domain.ProviderOperation;
import com.gateway.payment.configuration.repository.PaymentProviderRepository;
import com.gateway.payment.configuration.repository.ProviderApiRepository;
import com.gateway.payment.configuration.repository.ProviderEnvironmentRepository;
import com.gateway.payment.configuration.repository.ProviderOperationRepository;
import com.gateway.payment.payment.domain.PaymentTransaction;
import com.gateway.payment.provider.ProviderExecutionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Appelle le connecteur HTTP configuré (provider_api + environment / FSE api).
 */
@Component
public class ConnectorHttpInvoker {

    private static final Logger log = LoggerFactory.getLogger(ConnectorHttpInvoker.class);

    private final PaymentProviderRepository providerRepository;
    private final ProviderOperationRepository operationRepository;
    private final ProviderApiRepository apiRepository;
    private final ProviderEnvironmentRepository environmentRepository;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final SecretEncryptionService secretEncryption;

    public ConnectorHttpInvoker(
            PaymentProviderRepository providerRepository,
            ProviderOperationRepository operationRepository,
            ProviderApiRepository apiRepository,
            ProviderEnvironmentRepository environmentRepository,
            @Qualifier("gatewayRestClient") RestClient restClient,
            ObjectMapper objectMapper,
            SecretEncryptionService secretEncryption
    ) {
        this.providerRepository = providerRepository;
        this.operationRepository = operationRepository;
        this.apiRepository = apiRepository;
        this.environmentRepository = environmentRepository;
        this.restClient = restClient;
        this.objectMapper = objectMapper;
        this.secretEncryption = secretEncryption;
    }

    public ProviderExecutionResult invoke(
            PaymentTransaction tx,
            String providerCode,
            String operationCode,
            Map<String, Object> input,
            Map<String, Object> context
    ) {
        Instant start = Instant.now();
        PaymentProvider provider = providerRepository.findByCode(providerCode)
                .orElse(null);
        if (provider == null) {
            return fail(start, "PROVIDER_NOT_FOUND", "Fournisseur introuvable: " + providerCode, 404);
        }

        Optional<ProviderOperation> op = operationRepository.findByProviderIdAndCode(
                provider.getId(), operationCode);
        if (op.isEmpty()) {
            return fail(start, "API_NOT_FOUND", "API introuvable pour " + operationCode, 404);
        }

        ProviderApi api = resolveApi(provider.getId(), op.get().getId(), operationCode);
        if (api == null || !api.isActif()) {
            return fail(start, "API_NOT_FOUND", "API introuvable pour " + operationCode, 404);
        }

        String baseUrl = resolveBaseUrl(provider.getId(), api);
        if (!StringUtils.hasText(baseUrl)) {
            return fail(start, "ENVIRONMENT_MISSING",
                    "Aucune base URL pour " + providerCode + " / " + operationCode, 500);
        }

        Map<String, Object> values = collectParamValues(input, context);
        Map<String, Object> flatBody = buildRequestBody(api, values);
        Object body = encodeConnectorBody(api, flatBody);
        String method = api.getHttpMethod() != null ? api.getHttpMethod().trim().toUpperCase() : "POST";
        String url = joinUrl(baseUrl, substitutePlaceholders(api.getPath(), values));

        String bearer = resolveBearerToken(provider.getId(), api);
        try {
            String raw;
            var spec = restClient.method(org.springframework.http.HttpMethod.valueOf(method))
                    .uri(url);
            if (StringUtils.hasText(bearer)) {
                spec = spec.header(HttpHeaders.AUTHORIZATION, "Bearer " + bearer);
            }
            if ("GET".equals(method) || "HEAD".equals(method) || "DELETE".equals(method)) {
                raw = spec.retrieve().body(String.class);
            } else {
                raw = spec.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .body(body)
                        .retrieve()
                        .body(String.class);
            }
            return mapResponse(start, api, raw);
        } catch (RestClientResponseException ex) {
            log.warn("Connector HTTP {} {} → {}", method, url, ex.getStatusCode().value());
            return ProviderExecutionResult.builder()
                    .success(false)
                    .timedOut(false)
                    .httpStatus(ex.getStatusCode().value())
                    .providerStatus(String.valueOf(ex.getStatusCode().value()))
                    .providerMessage(ex.getResponseBodyAsString())
                    .gatewayStatus(PaymentStatus.FAILED)
                    .rawResponse(ex.getResponseBodyAsString())
                    .durationMs(Instant.now().toEpochMilli() - start.toEpochMilli())
                    .build();
        } catch (Exception ex) {
            boolean timedOut = ex.getMessage() != null
                    && ex.getMessage().toLowerCase().contains("timeout");
            log.warn("Connector HTTP error {} {}: {}", method, url, ex.getMessage());
            return ProviderExecutionResult.builder()
                    .success(false)
                    .timedOut(timedOut)
                    .httpStatus(timedOut ? 504 : 503)
                    .providerStatus(timedOut ? "TIMEOUT" : "CONNECTOR_ERROR")
                    .providerMessage(ex.getMessage())
                    .gatewayStatus(PaymentStatus.UNKNOWN)
                    .durationMs(Instant.now().toEpochMilli() - start.toEpochMilli())
                    .build();
        }
    }

    private ProviderApi resolveApi(UUID providerId, UUID operationId, String operationCode) {
        List<ProviderApi> byOp = apiRepository.findByOperationIdWithGraph(operationId);
        Optional<ProviderApi> active = byOp.stream().filter(ProviderApi::isActif).findFirst();
        if (active.isPresent()) {
            return active.get();
        }
        return apiRepository.findByProviderIdAndCodeWithGraph(providerId, operationCode).orElse(null);
    }

    private String resolveBearerToken(UUID providerId, ProviderApi api) {
        ProviderFseApi fse = api.getFseApi();
        if (fse == null) {
            return null;
        }
        AuthType authType = fse.getAuthType() != null ? fse.getAuthType() : AuthType.NONE;
        if (authType == AuthType.API_KEY || authType == AuthType.BEARER_TOKEN) {
            if (StringUtils.hasText(fse.getApiKeyEnc())) {
                try {
                    return secretEncryption.decrypt(fse.getApiKeyEnc());
                } catch (Exception ignored) {
                    return null;
                }
            }
            return null;
        }
        if (authType != AuthType.OAUTH2_CLIENT_CREDENTIALS) {
            return null;
        }
        String tokenUrl = fse.getTokenUrl();
        if (!StringUtils.hasText(tokenUrl)) {
            String base = resolveBaseUrl(providerId, api);
            if (StringUtils.hasText(base)) {
                tokenUrl = trimSlash(base) + "/oauth/token";
            }
        }
        if (!StringUtils.hasText(tokenUrl) || !StringUtils.hasText(fse.getClientId())) {
            return null;
        }
        String secret = null;
        if (StringUtils.hasText(fse.getClientSecretEnc())) {
            try {
                secret = secretEncryption.decrypt(fse.getClientSecretEnc());
            } catch (Exception ex) {
                log.warn("Decrypt client_secret failed for FSE {}: {}", fse.getCode(), ex.getMessage());
            }
        }
        if (!StringUtils.hasText(secret)) {
            return null;
        }
        try {
            Map<String, Object> tokenReq = Map.of(
                    "grant_type", "client_credentials",
                    "client_id", fse.getClientId(),
                    "client_secret", secret
            );
            String raw = restClient.post()
                    .uri(tokenUrl)
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .body(tokenReq)
                    .retrieve()
                    .body(String.class);
            Map<String, Object> parsed = objectMapper.readValue(raw, new TypeReference<>() {});
            Object access = parsed.get("access_token");
            return access != null ? String.valueOf(access) : null;
        } catch (Exception ex) {
            log.warn("OAuth token failed for FSE {}: {}", fse.getCode(), ex.getMessage());
            return null;
        }
    }

    private String resolveBaseUrl(UUID providerId, ProviderApi api) {
        if (api.getEnvironment() != null && StringUtils.hasText(api.getEnvironment().getBaseUrl())) {
            return api.getEnvironment().getBaseUrl().trim();
        }
        if (api.getFseApi() != null && StringUtils.hasText(api.getFseApi().getBaseUrl())) {
            return api.getFseApi().getBaseUrl().trim();
        }
        if (api.getFseApi() != null && api.getFseApi().getEnvironment() != null
                && StringUtils.hasText(api.getFseApi().getEnvironment().getBaseUrl())) {
            return api.getFseApi().getEnvironment().getBaseUrl().trim();
        }
        List<ProviderEnvironment> envs = environmentRepository.findByProviderId(providerId);
        return envs.stream()
                .filter(ProviderEnvironment::isActif)
                .filter(e -> StringUtils.hasText(e.getBaseUrl()))
                .min(Comparator.comparingInt(e -> "SANDBOX".equalsIgnoreCase(e.getCode()) ? 0 : 1))
                .map(ProviderEnvironment::getBaseUrl)
                .map(String::trim)
                .orElse(null);
    }

    /**
     * Sac de valeurs pour placeholders {@code ${…}} : context (params IU résolus) puis input.
     * Pas d’injection de champs {@link PaymentTransaction} — uniquement ParametreUtilisateur / mapping.
     */
    private static Map<String, Object> collectParamValues(
            Map<String, Object> input,
            Map<String, Object> context
    ) {
        Map<String, Object> values = new HashMap<>();
        putScalars(values, context);
        putScalars(values, input);
        return values;
    }

    /**
     * Body = {@code requestMappingJson} appliqué sur les valeurs IU, sinon scalaires input/context.
     * Aucun paramètre métier hardcodé (paymentId, amount, …).
     */
    private Map<String, Object> buildRequestBody(ProviderApi api, Map<String, Object> values) {
        String mapping = api != null ? api.getRequestMappingJson() : null;
        if (StringUtils.hasText(mapping)) {
            Map<String, Object> mapped = applyRequestMapping(mapping, values);
            if (!mapped.isEmpty()) {
                return mapped;
            }
        }
        Map<String, Object> body = new HashMap<>();
        putScalars(body, values);
        META_KEYS.forEach(body::remove);
        return body;
    }

    private Map<String, Object> applyRequestMapping(String mappingJson, Map<String, Object> values) {
        try {
            Object parsed = objectMapper.readValue(mappingJson, Object.class);
            Map<String, Object> body = new java.util.LinkedHashMap<>();
            if (parsed instanceof List<?> list) {
                for (Object item : list) {
                    if (!(item instanceof Map<?, ?> entry)) {
                        continue;
                    }
                    for (Map.Entry<?, ?> e : entry.entrySet()) {
                        if (e.getKey() == null) {
                            continue;
                        }
                        String key = String.valueOf(e.getKey()).trim();
                        if (!StringUtils.hasText(key)) {
                            continue;
                        }
                        Object resolved = resolveMappingValue(e.getValue(), values);
                        if (resolved != null) {
                            body.put(key, resolved);
                        }
                    }
                }
                return body;
            }
            if (parsed instanceof Map<?, ?> map) {
                for (Map.Entry<?, ?> e : map.entrySet()) {
                    if (e.getKey() == null) {
                        continue;
                    }
                    String key = String.valueOf(e.getKey()).trim();
                    if (!StringUtils.hasText(key)) {
                        continue;
                    }
                    Object resolved = resolveMappingValue(e.getValue(), values);
                    if (resolved != null) {
                        body.put(key, resolved);
                    }
                }
            }
            return body;
        } catch (Exception ex) {
            log.warn("requestMappingJson invalide — fallback scalaires: {}", ex.getMessage());
            return Map.of();
        }
    }

    private static Object resolveMappingValue(Object template, Map<String, Object> values) {
        if (template == null) {
            return null;
        }
        if (template instanceof Map || template instanceof List) {
            return null;
        }
        String raw = String.valueOf(template).trim();
        if (raw.startsWith("${") && raw.endsWith("}") && raw.length() > 3) {
            String key = raw.substring(2, raw.length() - 1).trim();
            Object v = values.get(key);
            if (v == null) {
                return null;
            }
            String s = String.valueOf(v).trim();
            return s.isEmpty() || "null".equalsIgnoreCase(s) ? null : s;
        }
        String substituted = substitutePlaceholders(raw, values);
        return StringUtils.hasText(substituted) ? substituted : null;
    }

    private static String substitutePlaceholders(String template, Map<String, Object> values) {
        if (!StringUtils.hasText(template) || values == null || values.isEmpty()) {
            return template;
        }
        String out = template;
        for (Map.Entry<String, Object> e : values.entrySet()) {
            if (!StringUtils.hasText(e.getKey()) || e.getValue() == null) {
                continue;
            }
            if (e.getValue() instanceof Map || e.getValue() instanceof List) {
                continue;
            }
            String token = "${" + e.getKey() + "}";
            if (out.contains(token)) {
                out = out.replace(token, String.valueOf(e.getValue()).trim());
            }
        }
        return out;
    }

    private static void putScalars(Map<String, Object> target, Map<String, Object> source) {
        if (source == null) {
            return;
        }
        for (Map.Entry<String, Object> e : source.entrySet()) {
            if (!StringUtils.hasText(e.getKey()) || e.getValue() == null) {
                continue;
            }
            if (e.getValue() instanceof Map || e.getValue() instanceof List) {
                continue;
            }
            String s = String.valueOf(e.getValue()).trim();
            if (s.isEmpty() || "null".equalsIgnoreCase(s)) {
                continue;
            }
            target.put(e.getKey(), s);
        }
    }

    private static final java.util.Set<String> META_KEYS = java.util.Set.of(
            "typeMethode", "invocationRail", "roleAssignments", "catalogConfiguration",
            "omnicanal", "omnicanalTransfer", "legCount", "compensated"
    );

    /**
     * Connecteur bill-payment : body = {@code List<Map<String,String>>}
     * (une entrée = un paramètre), aligné {@code BillPaymentController}.
     */
    private static Object encodeConnectorBody(ProviderApi api, Map<String, Object> flatBody) {
        String path = api != null ? api.getPath() : null;
        if (path != null && path.contains("/bills/")) {
            return toBillParamList(flatBody);
        }
        return flatBody;
    }

    private static List<Map<String, String>> toBillParamList(Map<String, Object> flatBody) {
        List<Map<String, String>> list = new java.util.ArrayList<>();
        if (flatBody == null) {
            return list;
        }
        flatBody.forEach((key, value) -> {
            if (!StringUtils.hasText(key) || value == null) {
                return;
            }
            if (value instanceof Map || value instanceof List) {
                return;
            }
            String s = String.valueOf(value).trim();
            if (s.isEmpty() || "null".equalsIgnoreCase(s)) {
                return;
            }
            list.add(Map.of(key, s));
        });
        return list;
    }

    /**
     * Parse le JSON connecteur. Succès métier = champ {@code code} (ou mapping),
     * pas d’heuristiques payload (amountDue / bills / items…).
     * Interprétation métier facture → {@code BillResponseSupport} / merger.
     */
    private ProviderExecutionResult mapResponse(Instant start, ProviderApi api, String raw) {
        Map<String, Object> parsed = parseJsonObject(raw);
        Map<String, Object> body = applyResponseMapping(api, parsed);

        String status = firstString(body, "code", "providerStatus", "status");
        String message = firstString(body, "message", "providerMessage", "detail");
        String reference = firstString(body, "providerReference", "reference", "transactionId");
        boolean ok = resolveSuccess(body, status);

        return ProviderExecutionResult.builder()
                .success(ok)
                .httpStatus(200)
                .providerStatus(StringUtils.hasText(status) ? status : (ok ? "00" : "FAILED"))
                .providerMessage(message)
                .providerReference(reference)
                .gatewayStatus(ok ? PaymentStatus.SUCCESS : PaymentStatus.FAILED)
                .responseBody(body instanceof HashMap ? body : new HashMap<>(body))
                .rawResponse(raw)
                .durationMs(Instant.now().toEpochMilli() - start.toEpochMilli())
                .build();
    }

    private Map<String, Object> parseJsonObject(String raw) {
        if (!StringUtils.hasText(raw)) {
            return new HashMap<>();
        }
        try {
            Map<String, Object> parsed = objectMapper.readValue(raw, new TypeReference<>() {});
            return parsed != null ? parsed : new HashMap<>();
        } catch (Exception ignored) {
            return new HashMap<>(Map.of("raw", raw));
        }
    }

    /**
     * {@code responseMappingJson} : clés gateway → template {@code ${chemin}} ou littéral.
     * Absente → body brut inchangé.
     */
    private Map<String, Object> applyResponseMapping(ProviderApi api, Map<String, Object> parsed) {
        String mapping = api != null ? api.getResponseMappingJson() : null;
        if (!StringUtils.hasText(mapping) || parsed == null) {
            return parsed != null ? parsed : new HashMap<>();
        }
        try {
            Object decoded = objectMapper.readValue(mapping, Object.class);
            Map<String, Object> out = new HashMap<>(parsed);
            if (decoded instanceof Map<?, ?> map) {
                for (Map.Entry<?, ?> e : map.entrySet()) {
                    if (e.getKey() == null) {
                        continue;
                    }
                    String key = String.valueOf(e.getKey()).trim();
                    if (!StringUtils.hasText(key)) {
                        continue;
                    }
                    Object resolved = resolveResponseMappingValue(e.getValue(), parsed);
                    if (resolved != null) {
                        out.put(key, resolved);
                    }
                }
            }
            return out;
        } catch (Exception ex) {
            log.warn("responseMappingJson invalide — body brut: {}", ex.getMessage());
            return parsed;
        }
    }

    private static Object resolveResponseMappingValue(Object template, Map<String, Object> parsed) {
        if (template == null) {
            return null;
        }
        if (template instanceof Map || template instanceof List) {
            return null;
        }
        String raw = String.valueOf(template).trim();
        if (raw.startsWith("${") && raw.endsWith("}") && raw.length() > 3) {
            String path = raw.substring(2, raw.length() - 1).trim();
            Object v = lookupPath(parsed, path);
            if (v == null) {
                return null;
            }
            if (v instanceof Map || v instanceof List) {
                return v;
            }
            String s = String.valueOf(v).trim();
            return s.isEmpty() || "null".equalsIgnoreCase(s) ? null : s;
        }
        return raw;
    }

    @SuppressWarnings("unchecked")
    private static Object lookupPath(Map<String, Object> root, String path) {
        if (root == null || !StringUtils.hasText(path)) {
            return null;
        }
        if (!path.contains(".")) {
            return root.get(path);
        }
        Object cur = root;
        for (String part : path.split("\\.")) {
            if (!(cur instanceof Map<?, ?> m)) {
                return null;
            }
            cur = m.get(part);
            if (cur == null) {
                return null;
            }
        }
        return cur;
    }

    private static boolean resolveSuccess(Map<String, Object> body, String status) {
        Object flag = body != null ? body.get("success") : null;
        if (flag instanceof Boolean b) {
            return b;
        }
        if (flag != null) {
            String s = String.valueOf(flag).trim();
            if ("true".equalsIgnoreCase(s) || "false".equalsIgnoreCase(s)) {
                return Boolean.parseBoolean(s);
            }
        }
        // HTTP 2xx déjà passé : sans code → succès transport ; sinon codes canal standards.
        return !StringUtils.hasText(status) || isSuccessStatus(status);
    }

    private static boolean isSuccessStatus(String status) {
        if (status == null) {
            return false;
        }
        String s = status.trim();
        return "00".equals(s) || "0".equals(s) || "OK".equalsIgnoreCase(s)
                || "SUCCESS".equalsIgnoreCase(s) || "true".equalsIgnoreCase(s);
    }

    private static String firstString(Map<String, Object> map, String... keys) {
        for (String k : keys) {
            Object v = map.get(k);
            if (v != null && StringUtils.hasText(String.valueOf(v))) {
                return String.valueOf(v);
            }
        }
        return null;
    }

    private static String joinUrl(String base, String path) {
        String b = trimSlash(base);
        if (!StringUtils.hasText(path)) {
            return b;
        }
        String p = path.trim();
        if (p.startsWith("http://") || p.startsWith("https://")) {
            return p;
        }
        if (!p.startsWith("/")) {
            p = "/" + p;
        }
        return b + p;
    }

    private static String trimSlash(String url) {
        String u = url.trim();
        while (u.endsWith("/")) {
            u = u.substring(0, u.length() - 1);
        }
        return u;
    }

    private static ProviderExecutionResult fail(Instant start, String code, String message, int http) {
        return ProviderExecutionResult.builder()
                .success(false)
                .httpStatus(http)
                .providerStatus(code)
                .providerMessage(message)
                .gatewayStatus(PaymentStatus.FAILED)
                .durationMs(Instant.now().toEpochMilli() - start.toEpochMilli())
                .build();
    }
}
