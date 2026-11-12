package com.gateway.payment.datasources;

import com.gateway.payment.datasources.spi.CatalogDataSourceFetchResult;

/**
 * Port vers l'hydratation Omnicanal FSE ({@code typeMethode=DATASOURCE}).
 * Implémenté côté payment-application.
 */
public interface OmnicanalDataSourcePort {

    CatalogDataSourceFetchResult fetch(
            DataSourceSection section,
            String category,
            String q,
            int page,
            int size
    );
}
