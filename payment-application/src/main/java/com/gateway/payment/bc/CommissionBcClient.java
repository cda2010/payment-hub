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
import java.util.Map;

@Component
public class CommissionBcClient {

    private static final Logger log = LoggerFactory.getLogger(CommissionBcClient.class);

    private final RestClient http;
    private final GatewayBcProperties properties;

    public CommissionBcClient(RestClient.Builder builder, GatewayBcProperties properties) {
        this.properties = properties;
        Duration timeout = Duration.parse(properties.commission().timeout());
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeout);
        factory.setReadTimeout(timeout);
        this.http = builder.baseUrl(properties.commission().url()).requestFactory(factory).build();
    }

    public boolean enabled() {
        return properties.commissionEnabled();
    }

    /** Simule puis enregistre les droits (Lot 2). */
    public void accrue(AccrueCommand cmd) {
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("transactionId", cmd.transactionId());
            payload.put("operation", cmd.operation());
            payload.put("productCode", cmd.productCode());
            payload.put("channel", cmd.channel());
            payload.put("currency", cmd.currency());
            payload.put("amount", cmd.amount());
            payload.put("fees", cmd.fees());
            payload.put("tenantId", cmd.tenantId());
            payload.put("aggregates", cmd.aggregates() == null ? Map.of() : cmd.aggregates());

            http.post()
                    .uri("/api/v1/commission/accruals")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(this::auth)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("commission accrue {} échoué: {}", cmd.transactionId(), e.getMessage());
        }
    }

    private void auth(HttpHeaders h) {
        String token = BcAuthRelay.authorizationHeader();
        if (token != null) {
            h.set(HttpHeaders.AUTHORIZATION, token);
        }
    }

    public record AccrueCommand(
            String transactionId,
            String operation,
            String productCode,
            String channel,
            String currency,
            BigDecimal amount,
            BigDecimal fees,
            String tenantId,
            Map<String, BigDecimal> aggregates
    ) {}
}
