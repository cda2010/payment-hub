package com.gateway.payment.datasources.spi;

import java.util.Set;

/**
 * Handler LOCAL d'une (ou plusieurs) source(s) catalogue.
 * <p>
 * Les beans Spring sont collectés par {@link CatalogDataSourceRegistry}.
 * Pour une source distante FSE : {@code origin=OMNICANAL}.
 * Pour une app peer (Product Gateway) : {@code origin=GATEWAY} ({@code payment-data-sources}).
 */
public interface CatalogDataSourceHandler {

    /** Codes supportés (ex. {@code BILLERS}, {@code TRANSFER_PARTICIPANTS}). */
    Set<String> codes();

    /** Libellé court pour le designer / inventaire API. */
    default String label() {
        return codes().stream().findFirst().orElse("?");
    }

    CatalogDataSourceFetchResult fetch(CatalogDataSourceFetchContext context);
}
