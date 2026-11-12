package com.gateway.payment.catalog.presentation.datasource.local;

import com.gateway.payment.datasources.CatalogDataSource;
import com.gateway.payment.datasources.spi.CatalogDataSourceFetchContext;
import com.gateway.payment.datasources.spi.CatalogDataSourceFetchResult;
import com.gateway.payment.datasources.spi.CatalogDataSourceHandler;
import com.gateway.payment.catalog.presentation.datasource.CatalogLocalItemMapper;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class StaticItemsLocalDataSourceHandler implements CatalogDataSourceHandler {

    private final CatalogLocalItemMapper mapper;

    public StaticItemsLocalDataSourceHandler(CatalogLocalItemMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Set<String> codes() {
        return Set.of(CatalogDataSource.STATIC_ITEMS.name());
    }

    @Override
    public String label() {
        return "Items statiques";
    }

    @Override
    public CatalogDataSourceFetchResult fetch(CatalogDataSourceFetchContext context) {
        return new CatalogDataSourceFetchResult(
                context.section().staticItems().stream().map(mapper::mapStatic).toList(),
                false
        );
    }
}
