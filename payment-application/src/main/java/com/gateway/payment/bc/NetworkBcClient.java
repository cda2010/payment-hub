package com.gateway.payment.bc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Client HTTP payment-network (positions / lignée) pour valider une UO agent
 * contre le réseau wallet (ex. CORIS_MONEY → codeFse 0011).
 */
@Component
public class NetworkBcClient {

    private static final Logger log = LoggerFactory.getLogger(NetworkBcClient.class);

    private final RestClient http;
    private final GatewayBcProperties properties;

    public NetworkBcClient(RestClient.Builder builder, GatewayBcProperties properties) {
        this.properties = properties;
        Duration timeout = Duration.parse(properties.network().timeout());
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeout);
        factory.setReadTimeout(timeout);
        this.http = builder.baseUrl(properties.network().url()).requestFactory(factory).build();
    }

    public boolean enabled() {
        return properties.networkEnabled();
    }

    /**
     * Position d'une unité par son code métier (ex. {@code AG-OUAHIGOUYA}).
     *
     * @return empty si introuvable (404)
     */
    @SuppressWarnings("unchecked")
    public Optional<Position> positionByCode(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        try {
            Map<?, ?> body = http.get()
                    .uri(u -> u.path("/api/v1/network/positions").queryParam("code", code.trim().toUpperCase()).build())
                    .headers(this::auth)
                    .retrieve()
                    .body(Map.class);
            return Optional.ofNullable(parse(body));
        } catch (HttpStatusCodeException e) {
            if (e.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
                return Optional.empty();
            }
            log.warn("payment-network positions code={} → {}", code, e.getStatusCode().value());
            throw new IllegalStateException("payment-network a refusé la lecture (" + e.getStatusCode().value() + ")");
        } catch (Exception e) {
            log.warn("payment-network injoignable: {}", e.getMessage());
            throw new IllegalStateException("payment-network injoignable: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private static Position parse(Map<?, ?> body) {
        if (body == null) {
            return null;
        }
        String zone = str(body.get("zone"));
        String devise = str(body.get("devise"));
        Unite unite = parseUnite(body.get("unite"));
        List<Unite> lignee = List.of();
        if (body.get("lignee") instanceof List<?> list) {
            lignee = list.stream().map(NetworkBcClient::parseUnite).filter(u -> u != null).toList();
        }
        return new Position(zone, devise, unite, lignee);
    }

    @SuppressWarnings("unchecked")
    private static Unite parseUnite(Object o) {
        if (!(o instanceof Map<?, ?> m)) {
            return null;
        }
        return new Unite(
                str(m.get("id")),
                str(m.get("code")),
                str(m.get("libelle")),
                str(m.get("type")),
                str(m.get("zone")),
                str(m.get("codeFse")),
                str(m.get("statut")));
    }

    private void auth(HttpHeaders headers) {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
            String token = attrs.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
            if (token != null && !token.isBlank()) {
                headers.set(HttpHeaders.AUTHORIZATION, token);
            }
        }
    }

    private static String str(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    public record Position(String zone, String devise, Unite unite, List<Unite> lignee) {
    }

    public record Unite(
            String id,
            String code,
            String libelle,
            String type,
            String zone,
            String codeFse,
            String statut
    ) {
    }
}
