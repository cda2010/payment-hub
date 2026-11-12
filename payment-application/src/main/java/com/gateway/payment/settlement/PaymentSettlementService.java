package com.gateway.payment.settlement;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.payment.bc.CommissionBcClient;
import com.gateway.payment.bc.RulesBcClient;
import com.gateway.payment.payment.domain.PaymentTransaction;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Post-traitement terminal : confirm/release quotas rules, accrual commission.
 */
@Service
public class PaymentSettlementService {

    private final RulesBcClient rules;
    private final CommissionBcClient commission;
    private final ObjectMapper objectMapper;

    public PaymentSettlementService(
            RulesBcClient rules,
            CommissionBcClient commission,
            ObjectMapper objectMapper
    ) {
        this.rules = rules;
        this.commission = commission;
        this.objectMapper = objectMapper;
    }

    public void onSuccess(PaymentTransaction tx) {
        if (tx == null) {
            return;
        }
        confirmRules(tx);
        accrueCommission(tx);
    }

    public void onFailure(PaymentTransaction tx) {
        if (tx == null) {
            return;
        }
        releaseRules(tx);
    }

    private void confirmRules(PaymentTransaction tx) {
        if (!rules.enabled()) {
            return;
        }
        // Plusieurs règles de cumul réservent sur le même paiement.
        // Le contexte ne garde que le dernier identifiant : on confirme toutes les lignes HELD.
        rules.confirmByTransaction(tx.getPaymentId());
    }

    private void releaseRules(PaymentTransaction tx) {
        if (!rules.enabled()) {
            return;
        }
        rules.releaseByTransaction(tx.getPaymentId());
    }

    private void accrueCommission(PaymentTransaction tx) {
        if (!commission.enabled()) {
            return;
        }
        Map<String, Object> ctx = readContext(tx);
        BigDecimal fees = toDecimal(ctx.get("fees"));
        if (fees == null) {
            fees = BigDecimal.ZERO;
        }
        // operation = code produit catalogue (CASH_IN, CASH_OUT…) pour matcher commission_grille.operation_type
        String productCode = str(first(ctx.get("productCode"), ctx.get("catalogProductCode")));
        String operation = productCode != null
                ? productCode
                : (tx.getServiceType() != null ? tx.getServiceType().name() : "PAYMENT");
        commission.accrue(new CommissionBcClient.AccrueCommand(
                tx.getPaymentId(),
                operation,
                productCode,
                str(first(ctx.get("channel"), ctx.get("canal"))),
                tx.getCurrency(),
                tx.getAmount(),
                fees,
                tx.getTenantId(),
                Map.of()
        ));
    }

    private Map<String, Object> readContext(PaymentTransaction tx) {
        if (tx.getContextJson() == null || tx.getContextJson().isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(tx.getContextJson(), new TypeReference<>() {});
        } catch (Exception e) {
            return Map.of();
        }
    }

    private static BigDecimal toDecimal(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof BigDecimal bd) {
            return bd;
        }
        if (v instanceof Number n) {
            return BigDecimal.valueOf(n.doubleValue());
        }
        try {
            return new BigDecimal(String.valueOf(v));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String str(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    private static Object first(Object a, Object b) {
        return a != null ? a : b;
    }
}
