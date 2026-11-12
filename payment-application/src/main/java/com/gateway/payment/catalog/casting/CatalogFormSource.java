package com.gateway.payment.catalog.casting;

/**
 * D'où vient le schéma UI d'une userTask.
 */
public enum CatalogFormSource {
    /** Formulaire classpath via {@code formKey} (modules/.../forms). */
    FORM_KEY,
    /** Écran défini sur {@code BillerProduct} (lookup/confirm/amount). */
    BILLER_PRODUCT,
    /**
     * Récap dynamique depuis {@code configuration.recap} + contexte transaction
     * (formKey conventionnel {@code product-recap}).
     */
    RECAP,
    /**
     * Upload dynamique depuis {@code requiredDocs} / {@code optionalDocs}
     * (formKey conventionnel {@code product-document-upload}).
     */
    DOCUMENTS
}
