package com.gateway.payment.integration.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProductGatewayFacadeNormalizeTest {

    @Test
    void normalizeProductCode_coffreAliases() {
        assertEquals("COFFRE", ProductGatewayFacade.normalizeProductCode("COFFRE"));
        assertEquals("COFFRE", ProductGatewayFacade.normalizeProductCode("coffre"));
        assertEquals("COFFRE", ProductGatewayFacade.normalizeProductCode("VAULT"));
        assertEquals("COFFRE", ProductGatewayFacade.normalizeProductCode("SAFE"));
    }

    @Test
    void normalizeProductCode_tontine() {
        assertEquals("TONTINE", ProductGatewayFacade.normalizeProductCode("TONTINE"));
        assertEquals("TONTINE", ProductGatewayFacade.normalizeProductCode("tontine"));
    }

    @Test
    void normalizeProductCode_checkoutUnchanged() {
        assertEquals("CHECKOUT", ProductGatewayFacade.normalizeProductCode("PAYMENT"));
        assertEquals("CHECKOUT", ProductGatewayFacade.normalizeProductCode("PAYEMENT"));
    }
}
