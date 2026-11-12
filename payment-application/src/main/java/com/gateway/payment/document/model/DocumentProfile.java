package com.gateway.payment.document.model;

import java.util.List;

/**
 * Profil résolu (DMN → document_profile).
 */
public record DocumentProfile(
        String code,
        List<DocumentRequirement> required,
        List<DocumentRequirement> optional,
        boolean requiresApproval
) {
    public static DocumentProfile none(String code) {
        return new DocumentProfile(
                code != null ? code : "NONE",
                List.of(),
                List.of(),
                false
        );
    }

    public boolean requiresDocuments() {
        return required != null && !required.isEmpty();
    }
}
