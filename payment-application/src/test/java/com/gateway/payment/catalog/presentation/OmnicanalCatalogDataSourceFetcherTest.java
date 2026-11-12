package com.gateway.payment.catalog.presentation;

import com.gateway.payment.datasources.api.CatalogItemDto;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class OmnicanalCatalogDataSourceFetcherTest {

    @Test
    void mapsItemsArray() {
        var result = OmnicanalCatalogDataSourceFetcher.mapBody(Map.of(
                "items", List.of(
                        Map.of("id", "A", "label", "Alpha", "icon", "flash-outline"),
                        Map.of("code", "B", "name", "Beta")
                ),
                "hasMore", true
        ));
        assertTrue(result.hasMore());
        assertEquals(2, result.items().size());
        CatalogItemDto first = result.items().get(0);
        assertEquals("A", first.id());
        assertEquals("Alpha", first.label());
        assertEquals("B", result.items().get(1).id());
        assertEquals("Beta", result.items().get(1).label());
    }

    @Test
    void emptyOnNull() {
        assertTrue(OmnicanalCatalogDataSourceFetcher.mapBody(null).items().isEmpty());
        assertTrue(OmnicanalCatalogDataSourceFetcher.mapBody(Map.of()).items().isEmpty());
    }
}
