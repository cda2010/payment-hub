package com.gateway.payment.catalog.presentation.datasource.local;

import com.gateway.payment.billing.api.dto.BillerPageResponse;
import com.gateway.payment.billing.service.BillerService;
import com.gateway.payment.datasources.CatalogDataSource;
import com.gateway.payment.datasources.spi.CatalogDataSourceFetchContext;
import com.gateway.payment.datasources.spi.CatalogDataSourceFetchResult;
import com.gateway.payment.datasources.spi.CatalogDataSourceHandler;
import com.gateway.payment.catalog.presentation.datasource.CatalogLocalItemMapper;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class BillersLocalDataSourceHandler implements CatalogDataSourceHandler {

    private final BillerService billerService;
    private final CatalogLocalItemMapper mapper;

    public BillersLocalDataSourceHandler(BillerService billerService, CatalogLocalItemMapper mapper) {
        this.billerService = billerService;
        this.mapper = mapper;
    }

    @Override
    public Set<String> codes() {
        return Set.of(CatalogDataSource.BILLERS.name());
    }

    @Override
    public String label() {
        return "Facturiers";
    }

    @Override
    public CatalogDataSourceFetchResult fetch(CatalogDataSourceFetchContext context) {
        BillerPageResponse pageResp = billerService.searchActive(
                context.category(),
                context.q(),
                context.page(),
                context.size()
        );
        return new CatalogDataSourceFetchResult(
                pageResp.items().stream().map(mapper::mapBiller).toList(),
                pageResp.hasMore()
        );
    }
}
