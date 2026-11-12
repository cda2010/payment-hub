package com.gateway.payment.catalog.domain;

/**
 * Cycle de vie d'une entrée du catalogue de services.
 */
public enum CatalogDeServiceStatus {
    DRAFT,
    VALIDATED,
    PUBLISHED,
    SUSPENDED,
    ARCHIVED
}
