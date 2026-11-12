package com.gateway.payment.datasources;

import com.gateway.payment.datasources.api.CatalogItemDto;
import com.gateway.payment.datasources.spi.CatalogDataSourceFetchContext;
import com.gateway.payment.datasources.spi.CatalogDataSourceFetchResult;
import com.gateway.payment.datasources.spi.CatalogDataSourceHandler;
import com.gateway.payment.datasources.spi.CatalogDataSourceRegistry;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CatalogDataSourceRegistryTest {

    @Test
    void indexesAllCodesFromHandler() {
        CatalogDataSourceHandler handler = new CatalogDataSourceHandler() {
            @Override
            public Set<String> codes() {
                return Set.of("BILLERS", "billers");
            }

            @Override
            public CatalogDataSourceFetchResult fetch(CatalogDataSourceFetchContext context) {
                return CatalogDataSourceFetchResult.empty();
            }
        };

        CatalogDataSourceRegistry registry = new CatalogDataSourceRegistry(List.of(handler));
        assertTrue(registry.has("BILLERS"));
        assertTrue(registry.has("billers"));
        assertSame(handler, registry.get("BILLERS").orElseThrow());
    }

    @Test
    void rejectsDuplicateCodesFromDifferentHandlers() {
        CatalogDataSourceHandler a = stub("CUSTOM_A", "FOO");
        CatalogDataSourceHandler b = stub("CUSTOM_B", "FOO");
        assertThrows(IllegalStateException.class, () -> new CatalogDataSourceRegistry(List.of(a, b)));
    }

    private static CatalogDataSourceHandler stub(String label, String code) {
        return new CatalogDataSourceHandler() {
            @Override
            public Set<String> codes() {
                return Set.of(code);
            }

            @Override
            public String label() {
                return label;
            }

            @Override
            public CatalogDataSourceFetchResult fetch(CatalogDataSourceFetchContext context) {
                return new CatalogDataSourceFetchResult(
                        List.of(new CatalogItemDto(code, label, null, null, null, Map.of())),
                        false
                );
            }
        };
    }
}
