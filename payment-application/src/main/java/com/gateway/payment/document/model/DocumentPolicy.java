package com.gateway.payment.document.model;

import java.util.List;

/**
 * Policy catalogue {@code configuration.documentPolicy}.
 */
public record DocumentPolicy(
        String code,
        boolean enabled,
        String dmnDecisionKey,
        /** Champ montant à évaluer (amount | debitAmount | …). */
        String amountField,
        String formKey
) {
    public static DocumentPolicy disabled() {
        return new DocumentPolicy(null, false, null, "amount", "product-document-upload");
    }
}
