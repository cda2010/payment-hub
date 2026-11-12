package com.gateway.payment.rules.moteur;

import java.math.BigDecimal;
import java.util.Map;

public record DemandeEvaluation(
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
) {
    public String ctx(String key) {
        if (context == null || key == null) {
            return null;
        }
        Object v = context.get(key);
        return v == null ? null : String.valueOf(v);
    }

    public BigDecimal ctxDecimal(String key) {
        if (context == null || key == null || !context.containsKey(key)) {
            return null;
        }
        Object v = context.get(key);
        if (v instanceof BigDecimal bd) {
            return bd;
        }
        if (v instanceof Number n) {
            return BigDecimal.valueOf(n.doubleValue());
        }
        return new BigDecimal(String.valueOf(v));
    }
}
