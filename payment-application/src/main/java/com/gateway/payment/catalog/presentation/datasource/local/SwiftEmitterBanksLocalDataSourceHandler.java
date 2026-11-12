package com.gateway.payment.catalog.presentation.datasource.local;

import com.gateway.payment.catalog.presentation.datasource.CatalogLocalItemMapper;
import com.gateway.payment.datasources.CatalogDataSource;
import com.gateway.payment.datasources.ProductConfigurationPort;
import com.gateway.payment.datasources.spi.CatalogDataSourceFetchContext;
import com.gateway.payment.datasources.spi.CatalogDataSourceFetchResult;
import com.gateway.payment.datasources.spi.CatalogDataSourceHandler;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Banques (providers) émettrices du produit catalogue {@code SWIFT}
 * — casting {@code configuration.participants} filtrés {@code role=SOURCE}.
 */
@Component
public class SwiftEmitterBanksLocalDataSourceHandler implements CatalogDataSourceHandler {

    public static final String PRODUCT_CODE = "SWIFT";
    public static final String EMITTER_ROLE = "SOURCE";

    private final ProductConfigurationPort productConfigurationPort;
    private final CatalogLocalItemMapper mapper;

    public SwiftEmitterBanksLocalDataSourceHandler(
            ProductConfigurationPort productConfigurationPort,
            CatalogLocalItemMapper mapper
    ) {
        this.productConfigurationPort = productConfigurationPort;
        this.mapper = mapper;
    }

    @Override
    public Set<String> codes() {
        return Set.of(CatalogDataSource.SWIFT_EMITTER_BANKS.name());
    }

    @Override
    public String label() {
        return "Banques émettrices SWIFT";
    }

    @Override
    public CatalogDataSourceFetchResult fetch(CatalogDataSourceFetchContext context) {
        Map<String, Object> configuration = productConfigurationPort.loadConfiguration(PRODUCT_CODE);
        Map<String, Object> params = new LinkedHashMap<>();
        if (context.section() != null && context.section().params() != null) {
            params.putAll(context.section().params());
        }
        params.put("role", EMITTER_ROLE);
        return new CatalogDataSourceFetchResult(
                mapper.mapCatalogParticipants(configuration, params),
                false
        );
    }
}
