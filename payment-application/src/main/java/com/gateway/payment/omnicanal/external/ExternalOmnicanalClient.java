package com.gateway.payment.omnicanal.external;

import com.gateway.payment.common.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Appelle l'omnicanal externe : jeton, puis {@code boundaryByTypeMethode} ou {@code omnicanalPresentation}.
 * Le parcours catalogue l'utilise quand {@code gateway.omnicanal.external.enabled} est vrai.
 */
@Component
public class ExternalOmnicanalClient {

    private static final Logger log = LoggerFactory.getLogger(ExternalOmnicanalClient.class);

    private final ExternalOmnicanalProperties properties;
    private final RestClient http;
    private volatile CachedToken cachedToken;

    @Autowired
    public ExternalOmnicanalClient(RestClient.Builder builder, ExternalOmnicanalProperties properties) {
        this(http(builder, properties), properties);
    }

    ExternalOmnicanalClient(RestClient http, ExternalOmnicanalProperties properties) {
        this.properties = properties;
        this.http = http;
    }

    private static RestClient http(RestClient.Builder builder, ExternalOmnicanalProperties properties) {
        Duration timeout = Duration.parse(properties.getTimeout());
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeout);
        factory.setReadTimeout(timeout);
        String base = properties.getBaseUrl() == null ? "" : properties.getBaseUrl().replaceAll("/+$", "");
        return builder.baseUrl(base).requestFactory(factory).build();
    }

    public boolean enabled() {
        return properties.isEnabled();
    }

    public Map<String, Object> boundary(String codeFse, String codeMethode, String otrParametre) {
        requireEnabled();
        try {
            Map<String, Object> body = http.post()
                    .uri(uri -> uri.path("/api/boundaryByTypeMethode")
                            .queryParam("codeFSE", codeFse)
                            .queryParam("codeMethode", codeMethode)
                            .queryParam("otrParametre", otrParametre == null ? "" : otrParametre)
                            .build())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(Map.class);
            return body == null ? Map.of() : body;
        } catch (RestClientException ex) {
            log.warn("omnicanal externe boundary {} / {} indisponible: {}", codeFse, codeMethode, ex.getMessage());
            throw new BusinessException(
                    "EXTERNAL_OMNICANAL_UNAVAILABLE",
                    "Appel omnicanal externe indisponible",
                    HttpStatus.BAD_GATEWAY
            );
        }
    }

    public List<Map<String, Object>> comptes() {
        requireEnabled();
        try {
            List<Map<String, Object>> body = http.get()
                    .uri("/api/comptes/catalogue")
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });
            return body == null ? List.of() : body;
        } catch (RestClientException ex) {
            log.warn("comptes omnicanal indisponibles: {}", ex.getMessage());
            throw new BusinessException(
                    "EXTERNAL_OMNICANAL_UNAVAILABLE",
                    "Comptes omnicanal indisponibles",
                    HttpStatus.BAD_GATEWAY
            );
        }
    }

    public List<Map<String, Object>> schemas() {
        requireEnabled();
        try {
            List<Map<String, Object>> body = http.get()
                    .uri("/api/schemas")
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });
            return body == null ? List.of() : body;
        } catch (RestClientException ex) {
            log.warn("schémas omnicanal indisponibles: {}", ex.getMessage());
            throw new BusinessException(
                    "EXTERNAL_OMNICANAL_UNAVAILABLE",
                    "Schémas d'écriture omnicanal indisponibles",
                    HttpStatus.BAD_GATEWAY
            );
        }
    }

    public List<Map<String, Object>> fournisseurs() {
        requireEnabled();
        try {
            List<Map<String, Object>> body = http.get()
                    .uri("/api/fse/fournisseurs")
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });
            return body == null ? List.of() : body;
        } catch (RestClientException ex) {
            log.warn("liste fournisseurs omnicanal indisponible: {}", ex.getMessage());
            throw new BusinessException(
                    "EXTERNAL_OMNICANAL_UNAVAILABLE",
                    "Liste des fournisseurs omnicanal indisponible",
                    HttpStatus.BAD_GATEWAY
            );
        }
    }

    public Map<String, Object> updateFournisseur(String codeFse, Map<String, Object> body) {
        requireEnabled();
        try {
            Map<String, Object> saved = http.put()
                    .uri("/api/fse/{codeFse}", codeFse)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });
            return saved == null ? Map.of() : saved;
        } catch (RestClientResponseException ex) {
            HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
            throw new BusinessException(
                    "OMNICANAL_FOURNISSEUR_REJECTED",
                    fournisseurError(ex),
                    status == null ? HttpStatus.BAD_GATEWAY : status
            );
        } catch (RestClientException ex) {
            log.warn("mise à jour fournisseur omnicanal {} indisponible: {}", codeFse, ex.getMessage());
            throw new BusinessException(
                    "EXTERNAL_OMNICANAL_UNAVAILABLE",
                    "Mise à jour du fournisseur omnicanal indisponible",
                    HttpStatus.BAD_GATEWAY
            );
        }
    }

    public Map<String, Object> catalog(String codeFse) {
        requireEnabled();
        try {
            Map<String, Object> body = http.get()
                    .uri("/api/fse/{codeFse}/catalog", codeFse)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });
            return body == null ? Map.of() : body;
        } catch (RestClientException ex) {
            log.warn("catalogue omnicanal {} indisponible: {}", codeFse, ex.getMessage());
            throw new BusinessException(
                    "EXTERNAL_OMNICANAL_UNAVAILABLE",
                    "Catalogue omnicanal indisponible pour le FSE " + codeFse,
                    HttpStatus.BAD_GATEWAY
            );
        }
    }

    public Map<String, Object> presentation(ExternalPresentationRequest request) {
        requireEnabled();
        try {
            Map<String, Object> body = http.post()
                    .uri("/api/omnicanalPresentation")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(Map.class);
            return body == null ? Map.of() : body;
        } catch (RestClientException ex) {
            log.warn("omnicanal externe presentation indisponible: {}", ex.getMessage());
            throw new BusinessException(
                    "EXTERNAL_OMNICANAL_UNAVAILABLE",
                    "Présentation omnicanal externe indisponible",
                    HttpStatus.BAD_GATEWAY
            );
        }
    }

    public static String otrParametre(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return "";
        }
        return params.entrySet().stream()
                .filter(e -> StringUtils.hasText(e.getKey()))
                .map(e -> e.getKey() + "=" + (e.getValue() == null ? "" : e.getValue()))
                .collect(Collectors.joining("|"));
    }

    private void requireEnabled() {
        if (!properties.isEnabled()) {
            throw new BusinessException(
                    "EXTERNAL_OMNICANAL_DISABLED",
                    "Client omnicanal externe désactivé (gateway.omnicanal.external.enabled=false)"
            );
        }
    }

    private String accessToken() {
        CachedToken current = cachedToken;
        if (current != null && current.expiresAt.isAfter(Instant.now().plusSeconds(15))) {
            return current.value;
        }
        synchronized (this) {
            current = cachedToken;
            if (current != null && current.expiresAt.isAfter(Instant.now().plusSeconds(15))) {
                return current.value;
            }
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("username", properties.getLogin());
            form.add("password", properties.getPassword());
            form.add("grant_type", "password");
            form.add("scope", "read write");
            form.add("client_id", properties.getClientId());
            if (StringUtils.hasText(properties.getClientSecret())) {
                form.add("client_secret", properties.getClientSecret());
            }
            try {
                Map<?, ?> token = http.post()
                        .uri("/oauth/token")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .body(form)
                        .retrieve()
                        .body(Map.class);
                if (token == null || token.get("access_token") == null) {
                    throw new BusinessException(
                            "EXTERNAL_OMNICANAL_AUTH",
                            "Jeton omnicanal externe absent",
                            HttpStatus.BAD_GATEWAY
                    );
                }
                long expiresIn = 1800;
                Object raw = token.get("expires_in");
                if (raw instanceof Number number) {
                    expiresIn = number.longValue();
                }
                cachedToken = new CachedToken(String.valueOf(token.get("access_token")), Instant.now().plusSeconds(expiresIn));
                return cachedToken.value;
            } catch (BusinessException ex) {
                throw ex;
            } catch (RestClientException ex) {
                log.warn("authentification omnicanal externe refusée: {}", ex.getMessage());
                throw new BusinessException(
                        "EXTERNAL_OMNICANAL_AUTH",
                        "Authentification omnicanal externe refusée",
                        HttpStatus.BAD_GATEWAY
                );
            }
        }
    }

    private static final Pattern MESSAGE_FIELD = Pattern.compile("\"message\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\"");

    private static String fournisseurError(RestClientResponseException ex) {
        String raw = ex.getResponseBodyAsString();
        if (StringUtils.hasText(raw)) {
            Matcher matcher = MESSAGE_FIELD.matcher(raw);
            if (matcher.find()) {
                return matcher.group(1).replace("\\\"", "\"").replace("\\n", " ");
            }
        }
        return "Mise à jour du fournisseur omnicanal refusée (" + ex.getStatusCode().value() + ")";
    }

    private record CachedToken(String value, Instant expiresAt) {
    }
}
