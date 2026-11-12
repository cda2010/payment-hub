package com.gateway.payment.datasources;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RemoteDataSourceBodyMapperTest {

    @Test
    void mapsItemsArray() {
        RemoteDataSourceResult result = RemoteDataSourceBodyMapper.mapBody(Map.of(
                "items", List.of(
                        Map.of("id", "E1", "label", "Agence Plateau"),
                        Map.of("code", "E2", "name", "Agence Cocody")
                ),
                "hasMore", true
        ));
        assertEquals(2, result.items().size());
        assertEquals("E1", result.items().get(0).id());
        assertEquals("Agence Plateau", result.items().get(0).label());
        assertEquals("E2", result.items().get(1).id());
        assertTrue(result.hasMore());
    }

    @Test
    void mapsEstablishmentsKey() {
        RemoteDataSourceResult result = RemoteDataSourceBodyMapper.mapBody(Map.of(
                "establishments", List.of(Map.of("id", "X", "label", "X"))
        ));
        assertEquals(1, result.items().size());
    }

    @Test
    void emptyOnNull() {
        assertTrue(RemoteDataSourceBodyMapper.mapBody(null).items().isEmpty());
        assertTrue(RemoteDataSourceBodyMapper.mapBody(Map.of()).items().isEmpty());
    }
}
