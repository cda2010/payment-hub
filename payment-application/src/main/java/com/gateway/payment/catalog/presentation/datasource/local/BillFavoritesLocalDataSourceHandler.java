package com.gateway.payment.catalog.presentation.datasource.local;

import com.gateway.payment.datasources.api.CatalogItemDto;
import com.gateway.payment.datasources.CatalogDataSource;
import com.gateway.payment.datasources.spi.CatalogDataSourceFetchContext;
import com.gateway.payment.datasources.spi.CatalogDataSourceFetchResult;
import com.gateway.payment.datasources.spi.CatalogDataSourceHandler;
import com.gateway.payment.catalog.presentation.datasource.CatalogLocalItemMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class BillFavoritesLocalDataSourceHandler implements CatalogDataSourceHandler {

    private final CatalogLocalItemMapper mapper;

    public BillFavoritesLocalDataSourceHandler(CatalogLocalItemMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Set<String> codes() {
        return Set.of(CatalogDataSource.BILL_FAVORITES.name());
    }

    @Override
    public String label() {
        return "Favoris factures";
    }

    @Override
    public CatalogDataSourceFetchResult fetch(CatalogDataSourceFetchContext context) {
        var def = context.section();
        List<CatalogItemDto> fav = def.staticItems().isEmpty()
                ? List.of()
                : def.staticItems().stream().map(mapper::mapStatic).toList();
        Object limit = def.params().get("limit");
        if (limit instanceof Number n && n.intValue() > 0 && fav.size() > n.intValue()) {
            fav = fav.subList(0, n.intValue());
        }
        return new CatalogDataSourceFetchResult(fav, false);
    }
}
