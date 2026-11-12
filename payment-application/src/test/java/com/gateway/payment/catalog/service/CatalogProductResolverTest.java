package com.gateway.payment.catalog.service;

import com.gateway.payment.common.domain.ServiceTypeCode;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CatalogProductResolverTest {

    @Test
    void mapToServiceType_readsConfigurationOnly() {
        assertEquals(ServiceTypeCode.PAIEMENT_FACTURE,
                CatalogProductResolver.mapToServiceType(
                        "ANY_PRODUCT",
                        Map.of("serviceType", "PAIEMENT_FACTURE")));
        assertEquals(ServiceTypeCode.TRANSFERT,
                CatalogProductResolver.mapToServiceType(
                        "MES_BENEFICIAIRES",
                        Map.of("serviceType", "TRANSFERT")));
        assertEquals(ServiceTypeCode.LINK,
                CatalogProductResolver.mapToServiceType(
                        "LINK",
                        Map.of("service_type", "LINK")));
    }

    @Test
    void mapToServiceType_missingOrUnknownUsesDefaut() {
        assertEquals(ServiceTypeCode.DEFAUT,
                CatalogProductResolver.mapToServiceType("BILL_PAYMENT", null));
        assertEquals(ServiceTypeCode.DEFAUT,
                CatalogProductResolver.mapToServiceType("ACCOUNT_TRANSFER", Map.of()));
        assertEquals(ServiceTypeCode.DEFAUT,
                CatalogProductResolver.mapToServiceType(
                        "CUSTOM",
                        Map.of("serviceType", "NOT_A_REAL_TYPE")));
        assertEquals(ServiceTypeCode.DEFAUT,
                CatalogProductResolver.mapToServiceType(null, null));
    }

    @Test
    void mapToServiceType_acceptsDefautAliases() {
        assertEquals(ServiceTypeCode.DEFAUT,
                CatalogProductResolver.mapToServiceType(
                        "X", Map.of("serviceType", "DEFAUT")));
        assertEquals(ServiceTypeCode.DEFAUT,
                CatalogProductResolver.mapToServiceType(
                        "X", Map.of("serviceType", "DEFAULT")));
        assertEquals(ServiceTypeCode.DEFAUT,
                CatalogProductResolver.mapToServiceType(
                        "X", Map.of("serviceType", "GENERIQUE")));
    }

    @Test
    void configString_readsDefaultProviderAliases() {
        Map<String, Object> config = Map.of(
                "defaultPayProvider", "CORIS_MONEY",
                "defaultBillProvider", "BILL_PAYMENT",
                "defaultProvider", "WAVE"
        );
        assertEquals("CORIS_MONEY",
                CatalogProductResolver.configString(config, "defaultPayProvider", "default_pay_provider"));
        assertEquals("BILL_PAYMENT",
                CatalogProductResolver.configString(config, "defaultBillProvider", "default_bill_provider"));
        assertEquals("WAVE",
                CatalogProductResolver.configString(config, "defaultProvider", "default_provider"));
    }

    @Test
    void configString_snakeCaseFallback() {
        Map<String, Object> config = Map.of("default_pay_provider", "ORANGE_MONEY");
        assertEquals("ORANGE_MONEY",
                CatalogProductResolver.configString(
                        config, "defaultPayProvider", "default_pay_provider", "payProvider"));
    }

    @Test
    void configString_blankOrMissingReturnsNull() {
        assertNull(CatalogProductResolver.configString(Map.of("defaultProvider", "  "), "defaultProvider"));
        assertNull(CatalogProductResolver.configString(Map.of(), "defaultProvider"));
        assertNull(CatalogProductResolver.configString(null, "defaultProvider"));
    }
}
