package com.gateway.payment.catalog.presentation.datasource.local;

import com.gateway.payment.billing.service.BillerService;
import com.gateway.payment.datasources.CatalogDataSource;
import com.gateway.payment.datasources.spi.CatalogDataSourceFetchContext;
import com.gateway.payment.datasources.spi.CatalogDataSourceFetchResult;
import com.gateway.payment.datasources.spi.CatalogDataSourceHandler;
import com.gateway.payment.catalog.presentation.datasource.CatalogLocalItemMapper;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class BillerCategoriesLocalDataSourceHandler implements CatalogDataSourceHandler {

    private final BillerService billerService;
    private final CatalogLocalItemMapper mapper;

    public BillerCategoriesLocalDataSourceHandler(BillerService billerService, CatalogLocalItemMapper mapper) {
        this.billerService = billerService;
        this.mapper = mapper;
    }

    @Override
    public Set<String> codes() {
        return Set.of(CatalogDataSource.BILLER_CATEGORIES.name());
    }

    @Override
    public String label() {
        return "Catégories facturiers";
    }

    @Override
    public CatalogDataSourceFetchResult fetch(CatalogDataSourceFetchContext context) {
        return new CatalogDataSourceFetchResult(
                mapper.mapCategories(billerService.listActiveCategories()),
                false
        );
    }
}
