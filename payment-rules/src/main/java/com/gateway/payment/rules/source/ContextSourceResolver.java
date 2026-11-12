package com.gateway.payment.rules.source;

import com.gateway.payment.rules.domain.RuleVersion;
import com.gateway.payment.rules.domain.SourceType;
import com.gateway.payment.rules.moteur.DemandeEvaluation;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.Set;

/** Sources fournies par la requête / contexte (pas de ledger local). */
@Component
@Order(10)
public class ContextSourceResolver implements RuleSourceResolver {

    private static final Set<SourceType> SUPPORTED = EnumSet.of(
            SourceType.TRANSACTION_AMOUNT,
            SourceType.TRANSACTION_COUNT,
            SourceType.WALLET_BALANCE,
            SourceType.PROJECTED_BALANCE,
            SourceType.PROJECTED_HOLDING,
            SourceType.KYC_LEVEL,
            SourceType.CUSTOMER_STATUS,
            SourceType.TRANSACTION_TYPE,
            SourceType.COUNTRY,
            SourceType.CHANNEL,
            SourceType.CUSTOM_ATTRIBUTE
    );

    @Override
    public boolean supports(RuleVersion version) {
        return SUPPORTED.contains(version.getSourceType());
    }

    @Override
    public SourceValue resolve(RuleVersion version, DemandeEvaluation d, CumulSnapshot cumul) {
        return switch (version.getSourceType()) {
            case TRANSACTION_AMOUNT -> SourceValue.of(d.amount() == null ? BigDecimal.ZERO : d.amount());
            case TRANSACTION_COUNT -> SourceValue.of(1);
            case WALLET_BALANCE -> SourceValue.of(balance(d));
            case PROJECTED_BALANCE -> {
                BigDecimal bal = balance(d);
                BigDecimal amt = d.amount() == null ? BigDecimal.ZERO : d.amount();
                // Cash-out : solde projeté = solde - montant
                yield SourceValue.of(bal.subtract(amt));
            }
            case PROJECTED_HOLDING -> {
                BigDecimal bal = balance(d);
                BigDecimal amt = d.amount() == null ? BigDecimal.ZERO : d.amount();
                // Détention après crédit : solde + montant. Le solde fourni est celui du client
                // auprès de l'émetteur, tous instruments confondus.
                yield SourceValue.of(bal.add(amt));
            }
            case KYC_LEVEL -> SourceValue.of(first(d.ctx("kycLevel"), d.ctx("niveauKyc")));
            // Ne pas tomber sur un "status" générique (ex. statut paiement PENDING ≠ client ACTIVE).
            case CUSTOMER_STATUS -> SourceValue.of(d.ctx("customerStatus"));
            case TRANSACTION_TYPE -> SourceValue.of(d.transactionType());
            case COUNTRY -> SourceValue.of(d.ctx("country"));
            case CHANNEL -> SourceValue.of(d.ctx("channel"));
            case CUSTOM_ATTRIBUTE -> SourceValue.of(d.ctx(attrKey(version)));
            default -> throw new IllegalStateException("Source non supportée ici: " + version.getSourceType());
        };
    }

    private static BigDecimal balance(DemandeEvaluation d) {
        BigDecimal b = d.ctxDecimal("walletBalance");
        if (b != null) {
            return b;
        }
        b = d.ctxDecimal("balance");
        return b == null ? BigDecimal.ZERO : b;
    }

    private static String attrKey(RuleVersion version) {
        // conditions_json peut contenir attributeKey ; fallback "custom"
        String json = version.getConditionsJson();
        if (json != null && json.contains("attributeKey")) {
            int i = json.indexOf("attributeKey");
            int q1 = json.indexOf('"', i + 14);
            int q2 = json.indexOf('"', q1 + 1);
            if (q1 >= 0 && q2 > q1) {
                return json.substring(q1 + 1, q2);
            }
        }
        return "custom";
    }

    private static String first(String a, String b) {
        return a != null && !a.isBlank() ? a : b;
    }
}
