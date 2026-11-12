package com.gateway.payment.catalog.datasource;

import com.gateway.payment.catalog.presentation.OmnicanalCatalogDataSourceFetcher;
import com.gateway.payment.datasources.DataSourceSection;
import com.gateway.payment.datasources.OmnicanalDataSourcePort;
import com.gateway.payment.datasources.spi.CatalogDataSourceFetchResult;
import org.springframework.stereotype.Component;

/**
 * Bridge payment-application → module SPI pour origin OMNICANAL.
 */
@Component
public class OmnicanalDataSourceAdapter implements OmnicanalDataSourcePort {

    private final OmnicanalCatalogDataSourceFetcher fetcher;

    public OmnicanalDataSourceAdapter(OmnicanalCatalogDataSourceFetcher fetcher) {
        this.fetcher = fetcher;
    }

    @Override
    public CatalogDataSourceFetchResult fetch(
            DataSourceSection section,
            String category,
            String q,
            int page,
            int size
    ) {
        OmnicanalCatalogDataSourceFetcher.FetchResult remote =
                fetcher.fetch(section, category, q, page, size);
        return new CatalogDataSourceFetchResult(remote.items(), remote.hasMore());
    }
}
