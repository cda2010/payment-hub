package com.m2i.omnicanal.presentation.http;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.m2i.omnicanal.outils.restclient.AuthTokenInfo;
import com.m2i.omnicanal.outils.restclient.constants.ErreurConstants;
import com.m2i.omnicanal.outils.restclient.model.ResultMessage;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Appels HTTP vers les partenaires. Remplace le RestClient maison (RestTemplate)
 * par le client synchrone de Spring.
 */
@Component
@Lazy(false)
public class PartnerHttpClient {

    private static final Logger log = LoggerFactory.getLogger(PartnerHttpClient.class);
    private static final ObjectMapper JSON = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private static volatile PartnerHttpClient current;

    private final RestClient http;

    @Autowired
    public PartnerHttpClient(ObjectProvider<RestClient.Builder> builders) {
        this(client(builders));
    }

    PartnerHttpClient(RestClient http) {
        this.http = http;
        current = this;
    }

    private static RestClient client(ObjectProvider<RestClient.Builder> builders) {
        RestClient.Builder builder = builders == null ? null : builders.getIfAvailable();
        return builder != null ? builder.build() : RestClient.create();
    }

    /** Pour les processeurs Camel construits avec {@code new}. */
    public static PartnerHttpClient current() {
        PartnerHttpClient client = current;
        if (client == null) {
            throw new IllegalStateException("PartnerHttpClient is not initialized");
        }
        return client;
    }

    public ResultMessage call(String url, AuthTokenInfo token, Map<String, ?> uriVariables,
            String httpMethod, String couche, String typeReponse, Map<String, ?> extraHeaders) {
        try {
            Object body = http.method(method(httpMethod))
                    .uri(url, uriVariables == null ? Map.of() : uriVariables)
                    .headers(headers -> fill(headers, token, extraHeaders))
                    .retrieve()
                    .body(Object.class);
            return result(ErreurConstants.SUCCESS, "Opération terminée avec Succès", couche, body, typeReponse);
        } catch (HttpClientErrorException ex) {
            return result(ErreurConstants.HTTP_CLIENT_STATUS_ERROR, ex.getMessage(), couche, null, typeReponse);
        } catch (HttpStatusCodeException ex) {
            return result(ErreurConstants.HTTP_CODE_STATUS_ERROR, ex.getMessage(), couche, null, typeReponse);
        } catch (ResourceAccessException ex) {
            return result(ErreurConstants.RESSOURCE_ACCESS_ERROR,
                    "Ressource non disponible: erreur de connexion", couche, null, typeReponse);
        } catch (Exception ex) {
            return result(ErreurConstants.UNKNOWN_ERROR,
                    "Ressource non disponible: erreur de connexion ou erreur non déterminée:erreur spécifique" + ex.getMessage(),
                    couche, null, typeReponse);
        }
    }

    public ResultMessage post(String url, AuthTokenInfo token, Object body, String couche, String typeReponse) {
        try {
            Object response = http.post()
                    .uri(url)
                    .headers(headers -> fill(headers, token, null))
                    .body(body)
                    .retrieve()
                    .body(Object.class);
            return result(ErreurConstants.SUCCESS, "Opération terminée avec Succès", couche, response, typeReponse);
        } catch (HttpClientErrorException ex) {
            return result(ErreurConstants.HTTP_CLIENT_STATUS_ERROR, ex.getMessage(), couche, null, typeReponse);
        } catch (HttpStatusCodeException ex) {
            return result(ErreurConstants.HTTP_CODE_STATUS_ERROR, ex.getMessage(), couche, null, typeReponse);
        } catch (ResourceAccessException ex) {
            return result(ErreurConstants.RESSOURCE_ACCESS_ERROR,
                    "Ressource non disponible: erreur de connexion", couche, null, typeReponse);
        } catch (Exception ex) {
            return result(ErreurConstants.UNKNOWN_ERROR,
                    "Ressource non disponible: erreur de connexion ou erreur non déterminée",
                    couche, null, typeReponse);
        }
    }

    public ResponseEntity<Object> postEntity(String url, AuthTokenInfo token, Object body) {
        return http.post()
                .uri(url)
                .headers(headers -> fill(headers, token, null))
                .body(body)
                .exchange((request, response) -> {
                    Object payload = null;
                    byte[] raw = response.getBody().readAllBytes();
                    if (raw.length > 0) {
                        payload = JSON.readValue(raw, Object.class);
                    }
                    return ResponseEntity.status(response.getStatusCode()).body(payload);
                });
    }

    /**
     * Jeton OAuth2. Le login et le mot de passe sont déjà dans {@code grantQuery}
     * (l'ancien client inversait les deux champs de l'API).
     */
    public AuthTokenInfo requestToken(String tokenUrl, String grantQuery, String clientId, String clientSecret) {
        try {
            String basic = Base64.getEncoder().encodeToString(
                    (clientId + ":" + clientSecret).getBytes(StandardCharsets.UTF_8));
            log.debug("Demande de jeton {}", tokenUrl);
            Map<String, Object> payload = http.post()
                    .uri(tokenUrl + grantQuery)
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, "Basic " + basic)
                    .retrieve()
                    .body(new org.springframework.core.ParameterizedTypeReference<LinkedHashMap<String, Object>>() {
                    });
            return toToken(payload);
        } catch (Exception ex) {
            log.warn("Jeton refusé: {}", ex.getMessage());
            return null;
        }
    }

    public static String passwordGrant(String username, String password, String grantType,
            String clientSecret, String clientId) {
        return "?username=" + username
                + "&password=" + password
                + "&grant_type=" + grantType
                + "&scope=read write&client_secret=" + clientSecret
                + "&client_id=" + clientId;
    }

    public static ResultMessage result(int errCode, String message, String couche, Object body, String typeReponse) {
        ResultMessage messageResult = new ResultMessage();
        messageResult.setErrCode(errCode);
        messageResult.setMsg(message);
        messageResult.setNumCouche(couche);
        messageResult.setResponseType(typeReponse);
        messageResult.setResponseEntity(body);
        return messageResult;
    }

    public static <T> T convert(Object body, Class<T> type) {
        try {
            String json = JSON.writeValueAsString(body);
            return JSON.readValue(json, type);
        } catch (Exception ex) {
            return null;
        }
    }

    public static Map<String, Object> appHeaders(HttpServletRequest request) {
        ConcurrentHashMap<String, Object> headers = new ConcurrentHashMap<>();
        Collections.list(request.getHeaderNames()).forEach(name -> {
            if ("X-AppID".equals(name)) {
                headers.put(name, request.getHeader(name));
            }
        });
        return headers;
    }

    private static AuthTokenInfo toToken(Map<String, Object> payload) {
        if (payload == null || payload.get("access_token") == null) {
            return null;
        }
        AuthTokenInfo token = new AuthTokenInfo();
        token.setAccess_token(String.valueOf(payload.get("access_token")));
        if (payload.get("token_type") != null) {
            token.setToken_type(String.valueOf(payload.get("token_type")));
        }
        if (payload.get("refresh_token") != null) {
            token.setRefresh_token(String.valueOf(payload.get("refresh_token")));
        }
        if (payload.get("scope") != null) {
            token.setScope(String.valueOf(payload.get("scope")));
        }
        int expiresIn = payload.get("expires_in") instanceof Number number ? number.intValue() : 0;
        token.setExpires_in(expiresIn);
        token.setExpireTime(System.currentTimeMillis() + (long) expiresIn * 1000L);
        return token;
    }

    private static void fill(HttpHeaders headers, AuthTokenInfo token, Map<String, ?> extraHeaders) {
        headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (token != null && token.getAccess_token() != null) {
            String type = token.getToken_type();
            if (type == null || type.isBlank()) {
                headers.set(HttpHeaders.AUTHORIZATION, token.getAccess_token());
            } else if (type.endsWith(" ")) {
                headers.set(HttpHeaders.AUTHORIZATION, type + token.getAccess_token());
            } else {
                headers.set(HttpHeaders.AUTHORIZATION, type + " " + token.getAccess_token());
            }
        }
        if (extraHeaders == null) {
            return;
        }
        extraHeaders.forEach((name, value) -> {
            if (value != null) {
                headers.add(name, String.valueOf(value));
            }
        });
    }

    private static HttpMethod method(String httpMethod) {
        if (httpMethod == null) {
            return HttpMethod.POST;
        }
        return switch (httpMethod) {
            case "GET" -> HttpMethod.GET;
            case "PUT" -> HttpMethod.PUT;
            case "POST" -> HttpMethod.POST;
            default -> HttpMethod.POST;
        };
    }
}
