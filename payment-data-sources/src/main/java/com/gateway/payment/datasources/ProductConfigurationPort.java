package com.gateway.payment.datasources;

import java.util.Map;

/**
 * Port de chargement de la configuration produit (casting / participants…).
 * Implémenté côté payment-application via {@code CatalogProductResolver}.
 */
public interface ProductConfigurationPort {

    Map<String, Object> loadConfiguration(String productCode);
}
