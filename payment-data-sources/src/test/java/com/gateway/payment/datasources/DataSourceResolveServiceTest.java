package com.gateway.payment.datasources;

import com.gateway.payment.datasources.api.CatalogItemDto;
import com.gateway.payment.datasources.api.DataSourceResponse;
import com.gateway.payment.datasources.spi.CatalogDataSourceFetchContext;
import com.gateway.payment.datasources.spi.CatalogDataSourceFetchResult;
import com.gateway.payment.datasources.spi.CatalogDataSourceHandler;
import com.gateway.payment.datasources.spi.CatalogDataSourceRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DataSourceResolveServiceTest {

    @Test
    void localResolveDelegatesToRegistryHandler() {
        CatalogDataSourceHandler handler = new CatalogDataSourceHandler() {
            @Override
            public Set<String> codes() {
                return Set.of("BILLERS");
            }

            @Override
            public CatalogDataSourceFetchResult fetch(CatalogDataSourceFetchContext context) {
                return new CatalogDataSourceFetchResult(
                        List.of(new CatalogItemDto("SONABEL", "Sonabel", null, "flash-outline", null, Map.of())),
                        false
                );
            }
        };
        DataSourceResolveService service = new DataSourceResolveService(
                new CatalogDataSourceRegistry(List.of(handler)),
                provider(stubOmnicanal(CatalogDataSourceFetchResult.empty())),
                null,
                provider(null)
        );

        DataSourceResponse resp = service.resolve(
                "BILLERS", "LOCAL", Map.of(), "ENERGY", null, 0, 10, null
        );

        assertEquals("BILLERS", resp.code());
        assertEquals("LOCAL", resp.origin());
        assertEquals(1, resp.items().size());
        assertEquals("SONABEL", resp.items().get(0).id());
    }

    @Test
    void omnicanalResolveUsesFetcher() {
        CatalogDataSourceFetchResult remote = new CatalogDataSourceFetchResult(
                List.of(new CatalogItemDto("EXT", "Externe", null, "receipt-outline", "EXT", Map.of())),
                true
        );
        DataSourceResolveService service = new DataSourceResolveService(
                new CatalogDataSourceRegistry(List.of()),
                provider(stubOmnicanal(remote)),
                null,
                provider(null)
        );

        DataSourceResponse resp = service.resolve(
                "PARTICIPANTS_EXT",
                "OMNICANAL",
                Map.of("codeFse", "FSE1"),
                null,
                null,
                0,
                20,
                null
        );

        assertEquals(CatalogDataOrigin.OMNICANAL.name(), resp.origin());
        assertEquals("EXT", resp.items().get(0).id());
        assertTrue(resp.hasMore());
    }

    @Test
    void gatewayResolveUsesRemoteClient() {
        RemoteDataSourceClient remote = new RemoteDataSourceClient(null) {
            @Override
            public RemoteDataSourceResult fetch(
                    String code,
                    Map<String, Object> params,
                    String category,
                    String q,
                    int page,
                    int size
            ) {
                assertEquals("ESTABLISHMENTS", code);
                return new RemoteDataSourceResult(
                        List.of(new RemoteDataSourceItem(
                                "E1", "Agence Plateau", null, "business-outline", "E1", Map.of())),
                        false
                );
            }

            @Override
            public List<AppDescriptor> listConfiguredApps() {
                return List.of(new AppDescriptor("ESTABLISHMENTS", "Établissements"));
            }
        };
        DataSourceResolveService service = new DataSourceResolveService(
                new CatalogDataSourceRegistry(List.of()),
                provider(stubOmnicanal(CatalogDataSourceFetchResult.empty())),
                remote,
                provider(null)
        );

        DataSourceResponse resp = service.resolve(
                "ESTABLISHMENTS", "GATEWAY", Map.of(), null, null, 0, 20, null
        );

        assertEquals(CatalogDataOrigin.GATEWAY.name(), resp.origin());
        assertEquals("E1", resp.items().get(0).id());
        assertTrue(service.listDescriptors().stream().anyMatch(d ->
                "ESTABLISHMENTS".equals(d.code())
                        && CatalogDataOrigin.GATEWAY.name().equals(d.defaultOrigin())
        ));
    }

    @Test
    void hydrateFormSchemaInjectsValues() {
        CatalogDataSourceHandler handler = new CatalogDataSourceHandler() {
            @Override
            public Set<String> codes() {
                return Set.of("TRANSFER_PARTICIPANTS");
            }

            @Override
            public CatalogDataSourceFetchResult fetch(CatalogDataSourceFetchContext context) {
                return new CatalogDataSourceFetchResult(
                        List.of(new CatalogItemDto("WAVE", "Wave", "Wallet", "water-outline", null, Map.of())),
                        false
                );
            }
        };
        DataSourceResolveService service = new DataSourceResolveService(
                new CatalogDataSourceRegistry(List.of(handler)),
                provider(stubOmnicanal(CatalogDataSourceFetchResult.empty())),
                null,
                provider(null)
        );

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("components", List.of(
                new LinkedHashMap<>(Map.of(
                        "type", "select",
                        "key", "sourceProvider",
                        "properties", Map.of(
                                "rfDataSource", Map.of(
                                        "code", "TRANSFER_PARTICIPANTS",
                                        "origin", "LOCAL",
                                        "params", Map.of("role", "SOURCE")
                                )
                        )
                ))
        ));

        Map<String, Object> hydrated = service.hydrateFormSchema(schema, null);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> comps = (List<Map<String, Object>>) hydrated.get("components");
        Map<String, Object> select = comps.get(0);

        assertEquals("values", select.get("dataSrc"));
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) select.get("data");
        @SuppressWarnings("unchecked")
        List<Map<String, String>> values = (List<Map<String, String>>) data.get("values");
        assertEquals(1, values.size());
        assertEquals("WAVE", values.get(0).get("value"));
        assertEquals("Wave", values.get(0).get("label"));
    }

    @Test
    void listDescriptorsIncludesBuiltinsAndCustom() {
        CatalogDataSourceHandler stub = new CatalogDataSourceHandler() {
            @Override
            public Set<String> codes() {
                return Set.of(CatalogDataSource.BILLERS.name());
            }

            @Override
            public CatalogDataSourceFetchResult fetch(CatalogDataSourceFetchContext context) {
                return CatalogDataSourceFetchResult.empty();
            }
        };
        DataSourceResolveService service = new DataSourceResolveService(
                new CatalogDataSourceRegistry(List.of(stub)),
                provider(stubOmnicanal(CatalogDataSourceFetchResult.empty())),
                null,
                provider(null)
        );

        assertTrue(service.listDescriptors().stream().anyMatch(d -> "BILLERS".equals(d.code()) && d.localHandler()));
        assertTrue(service.listDescriptors().stream().anyMatch(d -> "CUSTOM".equals(d.code())));
    }

    private static OmnicanalDataSourcePort stubOmnicanal(CatalogDataSourceFetchResult result) {
        return (section, category, q, page, size) -> result;
    }

    private static <T> ObjectProvider<T> provider(T value) {
        return new ObjectProvider<>() {
            @Override
            public T getObject(Object... args) {
                return value;
            }

            @Override
            public T getObject() {
                return value;
            }

            @Override
            public T getIfAvailable() {
                return value;
            }

            @Override
            public T getIfUnique() {
                return value;
            }
        };
    }
}
