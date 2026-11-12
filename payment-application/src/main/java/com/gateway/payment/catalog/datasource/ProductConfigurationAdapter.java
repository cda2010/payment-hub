package com.gateway.payment.catalog.datasource;

import com.gateway.payment.catalog.service.CatalogProductResolver;
import com.gateway.payment.datasources.ProductConfigurationPort;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.Map;

/**
 * Bridge payment-application → module SPI pour chargement config produit.
 */
@Component
public class ProductConfigurationAdapter implements ProductConfigurationPort {

    private final CatalogProductResolver catalogProductResolver;

    public ProductConfigurationAdapter(CatalogProductResolver catalogProductResolver) {
        this.catalogProductResolver = catalogProductResolver;
    }

    @Override
    public Map<String, Object> loadConfiguration(String productCode) {
        if (!StringUtils.hasText(productCode) || catalogProductResolver == null) {
            return Map.of();
        }
        String code = productCode.trim().toUpperCase(Locale.ROOT).replace(' ', '_').replace('-', '_');
        CatalogProductResolver.ResolvedCatalogProduct catalog = catalogProductResolver.resolve(code, null);
        if (catalog != null && catalog.configuration() != null) {
            return catalog.configuration();
        }
        return Map.of();
    }
}
