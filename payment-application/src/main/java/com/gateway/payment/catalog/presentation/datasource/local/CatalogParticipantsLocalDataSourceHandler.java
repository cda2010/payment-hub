package com.gateway.payment.catalog.presentation.datasource.local;

import com.gateway.payment.datasources.CatalogDataSource;
import com.gateway.payment.datasources.spi.CatalogDataSourceFetchContext;
import com.gateway.payment.datasources.spi.CatalogDataSourceFetchResult;
import com.gateway.payment.datasources.spi.CatalogDataSourceHandler;
import com.gateway.payment.catalog.presentation.datasource.CatalogLocalItemMapper;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class CatalogParticipantsLocalDataSourceHandler implements CatalogDataSourceHandler {

    private final CatalogLocalItemMapper mapper;

    public CatalogParticipantsLocalDataSourceHandler(CatalogLocalItemMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Set<String> codes() {
        return Set.of(CatalogDataSource.CATALOG_PARTICIPANTS.name());
    }

    @Override
    public String label() {
        return "Participants casting produit";
    }

    @Override
    public CatalogDataSourceFetchResult fetch(CatalogDataSourceFetchContext context) {
        return new CatalogDataSourceFetchResult(
                mapper.mapCatalogParticipants(context.configuration(), context.section().params()),
                false
        );
    }
}
