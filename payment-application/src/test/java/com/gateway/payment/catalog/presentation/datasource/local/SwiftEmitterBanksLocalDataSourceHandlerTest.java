package com.gateway.payment.catalog.presentation.datasource.local;

import com.gateway.payment.catalog.casting.CatalogProductCastingService;
import com.gateway.payment.catalog.presentation.datasource.CatalogLocalItemMapper;
import com.gateway.payment.configuration.domain.PaymentProvider;
import com.gateway.payment.configuration.repository.PaymentProviderRepository;
import com.gateway.payment.datasources.CatalogDataOrigin;
import com.gateway.payment.datasources.CatalogDataSource;
import com.gateway.payment.datasources.DataSourceSection;
import com.gateway.payment.datasources.ProductConfigurationPort;
import com.gateway.payment.datasources.api.CatalogItemDto;
import com.gateway.payment.datasources.spi.CatalogDataSourceFetchContext;
import com.gateway.payment.datasources.spi.CatalogDataSourceFetchResult;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SwiftEmitterBanksLocalDataSourceHandlerTest {

    @Test
    void loadsSwiftCastingAndKeepsOnlySourceRole() {
        ProductConfigurationPort port = code -> {
            assertEquals("SWIFT", code);
            return Map.of(
                    "participants", List.of(
                            Map.of("role", "SOURCE", "providerCode", "CORIS_BANK", "default", true),
                            Map.of("role", "DESTINATION", "providerCode", "OTHER_BANK", "default", true)
                    )
            );
        };
        CatalogLocalItemMapper mapper = new CatalogLocalItemMapper(
                new CatalogProductCastingService(),
                repo(Map.of("CORIS_BANK", provider("CORIS_BANK", "Coris Bank")))
        );
        SwiftEmitterBanksLocalDataSourceHandler handler =
                new SwiftEmitterBanksLocalDataSourceHandler(port, mapper);

        DataSourceSection section = new DataSourceSection(
                "test",
                CatalogDataSource.SWIFT_EMITTER_BANKS.name(),
                CatalogDataSource.SWIFT_EMITTER_BANKS,
                CatalogDataOrigin.LOCAL,
                Map.of(),
                List.of(),
                30
        );
        CatalogDataSourceFetchResult result = handler.fetch(
                new CatalogDataSourceFetchContext(section, Map.of(), null, null, 0, 30)
        );

        assertEquals(1, result.items().size());
        CatalogItemDto item = result.items().get(0);
        assertEquals("CORIS_BANK", item.id());
        assertEquals("Coris Bank", item.label());
        assertEquals("SOURCE", item.meta().get("role"));
        assertTrue(handler.codes().contains("SWIFT_EMITTER_BANKS"));
    }

    private static PaymentProvider provider(String code, String libelle) {
        PaymentProvider p = new PaymentProvider();
        p.setCode(code);
        p.setLibelle(libelle);
        return p;
    }

    private static PaymentProviderRepository repo(Map<String, PaymentProvider> byCode) {
        return (PaymentProviderRepository) Proxy.newProxyInstance(
                PaymentProviderRepository.class.getClassLoader(),
                new Class<?>[]{PaymentProviderRepository.class},
                (proxy, method, args) -> {
                    if ("findByCode".equals(method.getName()) && args != null && args.length == 1) {
                        return Optional.ofNullable(byCode.get(String.valueOf(args[0]).toUpperCase()));
                    }
                    if ("toString".equals(method.getName())) {
                        return "PaymentProviderRepositoryStub";
                    }
                    if ("hashCode".equals(method.getName())) {
                        return System.identityHashCode(proxy);
                    }
                    if ("equals".equals(method.getName())) {
                        return proxy == args[0];
                    }
                    return null;
                }
        );
    }
}
