package com.gateway.payment.catalog.recap;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CatalogRecapSchemaBuilderTest {

    private final CatalogRecapSchemaBuilder builder = new CatalogRecapSchemaBuilder();

    @Test
    void buildsHtmlAndConfirmCheckbox() {
        Map<String, Object> cfg = Map.of(
                "recap", Map.of(
                        "enabled", true,
                        "title", "Récap test",
                        "confirmField", "confirmRecap",
                        "fields", List.of(
                                Map.of("key", "amount", "label", "Montant"),
                                Map.of("key", "currency", "label", "Devise")
                        )
                )
        );
        Map<String, Object> ctx = Map.of("amount", 1500, "currency", "EUR");
        Map<String, Object> schema = builder.build(cfg, ctx);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> components = (List<Map<String, Object>>) schema.get("components");
        assertEquals(3, components.size());
        assertTrue(String.valueOf(components.get(0).get("content")).contains("1 500")
                || String.valueOf(components.get(0).get("content")).contains("1500"));
        assertEquals("confirmRecap", components.get(1).get("key"));
    }

    @Test
    void shouldRenderRecap_requiresConfigOrFormSource() {
        assertFalse(builder.shouldRenderRecap(null, "account-transfer-confirm", "CONFIRM", Map.of()));
        assertTrue(builder.shouldRenderRecap(
                com.gateway.payment.catalog.casting.CatalogFormSource.RECAP,
                null, "CONFIRM", Map.of()));
        assertTrue(builder.shouldRenderRecap(
                null, "product-recap", "FOO", Map.of()));
        assertTrue(builder.shouldRenderRecap(
                null, "x", "CONFIRM", Map.of("recap", Map.of("enabled", true))));
    }
}
