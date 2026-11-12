package com.gateway.payment.catalog.casting;

/**
 * Nature d'un binding task → catalogue.
 */
public enum CatalogBindingKind {
    /** userTask — formulaire. */
    UI,
    /** serviceTask — appel provider. */
    INVOKE,
    /** Pass-through / logique locale. */
    SOFT,
    /** Attente callback / settlement. */
    WAIT,
    /** Circuit d'approbation (ApprovalEngine + DMN profile). */
    APPROVAL,
    /** Exécution d'une {@code CapabilityHandler} (locale ou future INVOKE unifiée). */
    CAPABILITY
}
