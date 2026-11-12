package com.gateway.payment.bpm;

/**
 * Spécification déclarative d'une opération fournisseur (remplace les if/else du delegate).
 */
public record ProviderCallOperationSpec(
        String operation,
        ProviderInvokeRail rail,
        ProviderCallSuccessHandler onSuccess,
        boolean mapAmountFromLookup,
        boolean remapLookupReference,
        boolean retainOtpWhenNonTerminal,
        /** Clé contexte pour stocker la référence provider ({@code wallet…} / {@code bill…}). */
        String providerReferenceContextKey
) {
    public static ProviderCallOperationSpec of(
            String operation,
            ProviderInvokeRail rail,
            ProviderCallSuccessHandler onSuccess
    ) {
        return new ProviderCallOperationSpec(operation, rail, onSuccess, false, false, false, null);
    }
}
