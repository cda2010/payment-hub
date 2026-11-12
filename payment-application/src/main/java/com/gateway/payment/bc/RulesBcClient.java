package com.gateway.payment.bc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class RulesBcClient {

    private static final Logger log = LoggerFactory.getLogger(RulesBcClient.class);

    private final RestClient http;
    private final GatewayBcProperties properties;

    public RulesBcClient(RestClient.Builder builder, GatewayBcProperties properties) {
        this.properties = properties;
        Duration timeout = Duration.parse(properties.rules().timeout());
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeout);
        factory.setReadTimeout(timeout);
        this.http = builder.baseUrl(properties.rules().url()).requestFactory(factory).build();
    }

    public boolean enabled() {
        return properties.rulesEnabled();
    }

    public EvaluationResult evaluate(EvaluateCommand cmd) {
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("transactionId", cmd.transactionId());
            payload.put("transactionType", cmd.transactionType());
            payload.put("productCode", cmd.productCode());
            payload.put("groupCode", cmd.groupCode());
            payload.put("amount", cmd.amount());
            payload.put("currency", cmd.currency());
            payload.put("customerId", cmd.customerId());
            payload.put("walletId", cmd.walletId());
            payload.put("agentId", cmd.agentId());
            payload.put("merchantId", cmd.merchantId());
            payload.put("context", cmd.context() == null ? Map.of() : cmd.context());

            Map<?, ?> body = http.post()
                    .uri("/api/v1/rules/evaluate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(this::auth)
                    .body(payload)
                    .retrieve()
                    .body(Map.class);
            return parse(body);
        } catch (Exception e) {
            log.warn("rules evaluate échoué: {}", e.getMessage());
            throw new IllegalStateException("payment-rules injoignable ou erreur: " + e.getMessage(), e);
        }
    }

    public void confirm(UUID reservationId) {
        if (reservationId == null) {
            return;
        }
        try {
            http.post()
                    .uri("/api/v1/limits/reservations/{id}/confirm", reservationId)
                    .headers(this::auth)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("confirm réservation {} échoué: {}", reservationId, e.getMessage());
        }
    }

    public void release(UUID reservationId) {
        if (reservationId == null) {
            return;
        }
        try {
            http.post()
                    .uri("/api/v1/limits/reservations/{id}/release", reservationId)
                    .headers(this::auth)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("release réservation {} échoué: {}", reservationId, e.getMessage());
        }
    }

    public void confirmByTransaction(String transactionId) {
        if (transactionId == null || transactionId.isBlank()) {
            return;
        }
        try {
            http.post()
                    .uri("/api/v1/limits/reservations/by-transaction/{txId}/confirm", transactionId)
                    .headers(this::auth)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("confirm by-tx {} échoué: {}", transactionId, e.getMessage());
        }
    }

    public void releaseByTransaction(String transactionId) {
        if (transactionId == null || transactionId.isBlank()) {
            return;
        }
        try {
            http.post()
                    .uri("/api/v1/limits/reservations/by-transaction/{txId}/release", transactionId)
                    .headers(this::auth)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("release by-tx {} échoué: {}", transactionId, e.getMessage());
        }
    }

    private EvaluationResult parse(Map<?, ?> body) {
        if (body == null) {
            throw new IllegalStateException("Réponse rules vide");
        }
        UUID reservationId = null;
        Object rid = body.get("reservationId");
        if (rid != null && !String.valueOf(rid).isBlank()) {
            reservationId = UUID.fromString(String.valueOf(rid));
        }
        return new EvaluationResult(
                str(body.get("decision")),
                bool(body.get("authorized"), false),
                reservationId,
                listStrings(body.get("violations")),
                listStrings(body.get("evaluatedRules"))
        );
    }

    private void auth(HttpHeaders h) {
        String token = BcAuthRelay.authorizationHeader();
        if (token != null) {
            h.set(HttpHeaders.AUTHORIZATION, token);
        }
    }

    private static String str(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    private static boolean bool(Object o, boolean def) {
        if (o instanceof Boolean b) {
            return b;
        }
        return def;
    }

    @SuppressWarnings("unchecked")
    private static List<String> listStrings(Object o) {
        if (o instanceof List<?> list) {
            return list.stream().map(x -> {
                if (x instanceof Map<?, ?> m) {
                    Object msg = m.get("message");
                    Object code = m.get("ruleCode");
                    return (code != null ? code + ": " : "") + (msg != null ? msg : x);
                }
                return String.valueOf(x);
            }).toList();
        }
        return List.of();
    }

    public record EvaluateCommand(
            String transactionId,
            String transactionType,
            String productCode,
            String groupCode,
            BigDecimal amount,
            String currency,
            String customerId,
            String walletId,
            String agentId,
            String merchantId,
            Map<String, Object> context
    ) {}

    public record EvaluationResult(
            String decision,
            boolean authorized,
            UUID reservationId,
            List<String> violations,
            List<String> evaluatedRules
    ) {}
}
