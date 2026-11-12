package com.gateway.payment.catalog.casting;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CatalogFormKeyNomenclatureTest {

    @Test
    void conventionalFormKey_accountTransferSourceWave() {
        assertEquals(
                "account-transfer-source-wave",
                CatalogFormKeyNomenclature.conventionalFormKey("ACCOUNT_TRANSFER", "SOURCE", "WAVE")
        );
    }

    @Test
    void conventionalFormKey_corisMoney() {
        assertEquals(
                "account-transfer-destination-coris-money",
                CatalogFormKeyNomenclature.conventionalFormKey(
                        "ACCOUNT_TRANSFER", "DESTINATION", "CORIS_MONEY")
        );
    }

    @Test
    void conventionalFormKey_requiresAllSegments() {
        assertNull(CatalogFormKeyNomenclature.conventionalFormKey("ACCOUNT_TRANSFER", "SOURCE", null));
        assertNull(CatalogFormKeyNomenclature.conventionalFormKey(null, "SOURCE", "WAVE"));
    }

    @Test
    void slug_normalizes() {
        assertEquals("orange-money", CatalogFormKeyNomenclature.slug("ORANGE_MONEY"));
        assertEquals("payer", CatalogFormKeyNomenclature.slug(" PAYER "));
    }
}
