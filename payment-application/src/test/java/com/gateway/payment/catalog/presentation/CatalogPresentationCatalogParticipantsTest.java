package com.gateway.payment.catalog.presentation;

import com.gateway.payment.datasources.api.CatalogItemDto;
import com.gateway.payment.catalog.casting.CatalogProductCastingService;
import com.gateway.payment.catalog.presentation.datasource.CatalogLocalItemMapper;
import com.gateway.payment.configuration.domain.PaymentProvider;
import com.gateway.payment.configuration.repository.PaymentProviderRepository;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Source LOCAL {@code CATALOG_PARTICIPANTS} : filtre sur le rôle casting
 * (PAYER pour CHECKOUT) et icônes des moyens de paiement.
 */
class CatalogPresentationCatalogParticipantsTest {

    @Test
    void filtersByPayerRoleAndCarriesIcon() {
        CatalogLocalItemMapper mapper = new CatalogLocalItemMapper(
                new CatalogProductCastingService(),
                repo(Map.of(
                        "CORIS_MONEY", provider("CORIS_MONEY", "Coris Money"),
                        "WAVE", provider("WAVE", "Wave")
                ))
        );

        Map<String, Object> configuration = Map.of(
                "participants", List.of(
                        Map.of("role", "PAYER", "providerCode", "CORIS_MONEY", "default", true),
                        Map.of("role", "PAYER", "providerCode", "WAVE", "default", false),
                        Map.of("role", "BILLER", "providerCode", "SONABEL", "default", false)
                )
        );

        List<CatalogItemDto> items = mapper.mapCatalogParticipants(
                configuration, Map.of("role", "PAYER")
        );

        assertEquals(List.of("CORIS_MONEY", "WAVE"), items.stream().map(CatalogItemDto::id).toList());
        assertEquals("wallet-outline", items.get(0).icon());
        assertEquals("water-outline", items.get(1).icon());
        assertEquals(Boolean.TRUE, items.get(0).meta().get("default"));
        assertEquals("PAYER", items.get(0).meta().get("role"));
    }

    @Test
    void blankRoleKeepsAllParticipants() {
        CatalogLocalItemMapper mapper = new CatalogLocalItemMapper(
                new CatalogProductCastingService(),
                repo(Map.of())
        );

        Map<String, Object> configuration = Map.of(
                "participants", List.of(
                        Map.of("role", "PAYER", "providerCode", "CORIS_MONEY", "default", true),
                        Map.of("role", "BILLER", "providerCode", "SONABEL", "default", false)
                )
        );

        assertEquals(2, mapper.mapCatalogParticipants(configuration, Map.of()).size());
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
                        return "PaymentProviderRepositoryProxy";
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
