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

@Component
public class PricingBcClient {

    private static final Logger log = LoggerFactory.getLogger(PricingBcClient.class);

    private final RestClient http;
    private final GatewayBcProperties properties;

    public PricingBcClient(RestClient.Builder builder, GatewayBcProperties properties) {
        this.properties = properties;
        Duration timeout = Duration.parse(properties.pricing().timeout());
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeout);
        factory.setReadTimeout(timeout);
        this.http = builder.baseUrl(properties.pricing().url()).requestFactory(factory).build();
    }

    public boolean enabled() {
        return properties.pricingEnabled();
    }

    public QuoteResult quote(QuoteCommand cmd) {
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("productCode", cmd.productCode());
            payload.put("channel", cmd.channel());
            payload.put("audience", cmd.audience());
            payload.put("amount", cmd.amount());
            payload.put("currency", cmd.currency());
            if (cmd.fromCurrency() != null) {
                payload.put("fromCurrency", cmd.fromCurrency());
            }
            if (cmd.toCurrency() != null) {
                payload.put("toCurrency", cmd.toCurrency());
            }
            if (cmd.countryCode() != null) {
                payload.put("countryCode", cmd.countryCode());
            }
            if (cmd.railCode() != null) {
                payload.put("railCode", cmd.railCode());
            }
            if (cmd.feeBaseAmount() != null) {
                payload.put("feeBaseAmount", cmd.feeBaseAmount());
            }
            if (cmd.aggregates() != null && !cmd.aggregates().isEmpty()) {
                payload.put("aggregates", cmd.aggregates());
            }
            Map<?, ?> body = http.post()
                    .uri("/api/v1/pricing/quote")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(this::auth)
                    .body(payload)
                    .retrieve()
                    .body(Map.class);
            if (body == null) {
                throw new IllegalStateException("Réponse pricing vide");
            }
            return new QuoteResult(
                    bool(body.get("allowed"), true),
                    listStrings(body.get("violations")),
                    decimal(body.get("principal")),
                    decimal(body.get("feeAmount")),
                    decimal(body.get("totalDebit")),
                    decimal(body.get("amountReceived")),
                    str(body.get("feeGridCode")),
                    str(body.get("formulaCode")),
                    str(body.get("feeBearer")),
                    str(body.get("feeBreakdown")),
                    str(body.get("snapshotId")),
                    decimalOrNull(body.get("feeBaseAmount")),
                    str(body.get("baseCalcul"))
            );
        } catch (Exception e) {
            log.warn("pricing quote échoué: {}", e.getMessage());
            throw new IllegalStateException("payment-pricing injoignable ou erreur: " + e.getMessage(), e);
        }
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

    private static BigDecimal decimal(Object o) {
        if (o == null) {
            return BigDecimal.ZERO;
        }
        if (o instanceof BigDecimal bd) {
            return bd;
        }
        if (o instanceof Number n) {
            return BigDecimal.valueOf(n.doubleValue());
        }
        return new BigDecimal(String.valueOf(o));
    }

    private static BigDecimal decimalOrNull(Object o) {
        if (o == null) {
            return null;
        }
        return decimal(o);
    }

    @SuppressWarnings("unchecked")
    private static List<String> listStrings(Object o) {
        if (o instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        return List.of();
    }

    public record QuoteCommand(
            String productCode,
            String channel,
            String audience,
            BigDecimal amount,
            String currency,
            String fromCurrency,
            String toCurrency,
            String countryCode,
            String railCode,
            BigDecimal feeBaseAmount,
            Map<String, BigDecimal> aggregates
    ) {
        public QuoteCommand(
                String productCode, String channel, String audience, BigDecimal amount, String currency,
                String fromCurrency, String toCurrency, String countryCode, String railCode,
                BigDecimal feeBaseAmount
        ) {
            this(productCode, channel, audience, amount, currency, fromCurrency, toCurrency, countryCode, railCode,
                    feeBaseAmount, null);
        }
    }

    public record QuoteResult(
            boolean allowed,
            List<String> violations,
            BigDecimal principal,
            BigDecimal feeAmount,
            BigDecimal totalDebit,
            BigDecimal amountReceived,
            String feeGridCode,
            String formulaCode,
            String feeBearer,
            String feeBreakdown,
            String snapshotId,
            BigDecimal feeBaseAmount,
            String baseCalcul
    ) {}
}
