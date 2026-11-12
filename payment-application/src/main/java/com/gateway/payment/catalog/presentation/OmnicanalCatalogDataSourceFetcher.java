package com.gateway.payment.catalog.presentation;

import com.gateway.payment.catalog.omnicanal.CatalogOmnicanalProperties;
import com.gateway.payment.datasources.DataSourceSection;
import com.gateway.payment.datasources.api.CatalogItemDto;
import com.gateway.payment.common.domain.OmnicanalTypeMethode;
import com.gateway.payment.common.domain.PaymentStatus;
import com.gateway.payment.common.domain.ServiceTypeCode;
import com.gateway.payment.configuration.domain.PaymentProvider;
import com.gateway.payment.configuration.repository.PaymentProviderRepository;
import com.gateway.payment.orchestration.api.dto.SchemaLegResult;
import com.gateway.payment.orchestration.domain.ProprieteApi;
import com.gateway.payment.orchestration.service.SchemaLegExecutorService;
import com.gateway.payment.payment.domain.PaymentTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Hydrate une section présentation via Omnicanal {@code typeMethode=DATASOURCE}.
 */
@Service
public class OmnicanalCatalogDataSourceFetcher {

    private static final Logger log = LoggerFactory.getLogger(OmnicanalCatalogDataSourceFetcher.class);
    private static final String TYPE_METHODE = OmnicanalTypeMethode.DATASOURCE.name();

    public record FetchResult(List<CatalogItemDto> items, boolean hasMore) {
        public static FetchResult empty() {
            return new FetchResult(List.of(), false);
        }
    }

    private final CatalogOmnicanalProperties properties;
    private final SchemaLegExecutorService legExecutor;
    private final PaymentProviderRepository providerRepository;

    public OmnicanalCatalogDataSourceFetcher(
            CatalogOmnicanalProperties properties,
            SchemaLegExecutorService legExecutor,
            PaymentProviderRepository providerRepository
    ) {
        this.properties = properties;
        this.legExecutor = legExecutor;
        this.providerRepository = providerRepository;
    }

    public FetchResult fetch(
            DataSourceSection section,
            String category,
            String q,
            int page,
            int size
    ) {
        if (!properties.isEnabled()) {
            log.debug("DATASOURCE skip — gateway.catalog.omnicanal.enabled=false (section {})", section.id());
            return FetchResult.empty();
        }

        String codeFse = resolveCodeFse(section.params());
        if (!StringUtils.hasText(codeFse)) {
            log.warn("DATASOURCE section {} — codeFse manquant (params.codeFse / providerCode)", section.id());
            return FetchResult.empty();
        }

        String dataKey = firstNonBlank(
                stringParam(section.params(), "dataKey"),
                stringParam(section.params(), "data_key"),
                section.dataSourceCode()
        );
        Map<String, String> userParams = buildUserParams(section.params(), codeFse, dataKey, category, q, page, size);

        PaymentTransaction stub = catalogStubTx(codeFse);
        try {
            SchemaLegResult leg = legExecutor.executeLegByStep(
                    stub,
                    null,
                    TYPE_METHODE,
                    codeFse,
                    codeFse,
                    userParams,
                    ProprieteApi.FSESOURCE,
                    TYPE_METHODE
            );
            if (!leg.success()) {
                FetchResult partial = mapBody(leg.responseBody());
                if (!partial.items().isEmpty()) {
                    log.debug(
                            "DATASOURCE section {} — success=false mais items présents ({})",
                            section.id(),
                            partial.items().size()
                    );
                    return partial;
                }
                log.warn(
                        "DATASOURCE section {} fse={} dataKey={} — échec: {}",
                        section.id(), codeFse, dataKey, leg.providerMessage()
                );
                return FetchResult.empty();
            }
            return mapBody(leg.responseBody());
        } catch (RuntimeException ex) {
            log.warn(
                    "DATASOURCE section {} fse={} dataKey={} — erreur: {}",
                    section.id(), codeFse, dataKey, ex.getMessage()
            );
            return FetchResult.empty();
        }
    }

    private String resolveCodeFse(Map<String, Object> params) {
        String direct = firstNonBlank(
                stringParam(params, "codeFse"),
                stringParam(params, "code_fse"),
                stringParam(params, "fse")
        );
        if (StringUtils.hasText(direct)) {
            return direct.trim();
        }
        String providerCode = firstNonBlank(
                stringParam(params, "providerCode"),
                stringParam(params, "provider_code")
        );
        if (!StringUtils.hasText(providerCode)) {
            return null;
        }
        return providerRepository.findByCode(providerCode.trim())
                .map(PaymentProvider::getCodeFse)
                .filter(StringUtils::hasText)
                .map(String::trim)
                .orElse(null);
    }

    private static Map<String, String> buildUserParams(
            Map<String, Object> sectionParams,
            String codeFse,
            String dataKey,
            String category,
            String q,
            int page,
            int size
    ) {
        Map<String, String> params = new LinkedHashMap<>();
        if (sectionParams != null) {
            for (Map.Entry<String, Object> e : sectionParams.entrySet()) {
                if (e.getValue() != null) {
                    params.put(e.getKey(), String.valueOf(e.getValue()));
                }
            }
        }
        params.put("codeFse", codeFse);
        params.put("CODEFSESOURCE", codeFse);
        if (StringUtils.hasText(dataKey)) {
            params.put("dataKey", dataKey);
            params.putIfAbsent("dataSource", dataKey);
        }
        if (StringUtils.hasText(category)) {
            params.putIfAbsent("category", category);
        }
        if (StringUtils.hasText(q)) {
            params.putIfAbsent("q", q);
            params.putIfAbsent("query", q);
        }
        params.putIfAbsent("page", String.valueOf(Math.max(0, page)));
        params.putIfAbsent("size", String.valueOf(size > 0 ? size : 30));
        return params;
    }

    private static PaymentTransaction catalogStubTx(String codeFse) {
        String providerCode = "CATALOG";
        PaymentTransaction tx = PaymentTransaction.builder()
                .paymentId("CAT-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16))
                .providerCode(providerCode)
                .serviceType(ServiceTypeCode.PAYEMENT)
                .amount(BigDecimal.ZERO)
                .currency("XOF")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        tx.hydrateStatus(PaymentStatus.INITIATED);
        tx.setCurrentStep(TYPE_METHODE);
        tx.setContextJson("{\"codeFse\":\"" + codeFse + "\",\"catalogRead\":true}");
        return tx;
    }

    @SuppressWarnings("unchecked")
    static FetchResult mapBody(Map<String, Object> body) {
        if (body == null || body.isEmpty()) {
            return FetchResult.empty();
        }
        boolean hasMore = bool(body.get("hasMore")) || bool(body.get("has_more"));
        Object rawItems = first(body, "items", "data", "results", "content", "billers", "participants");
        if (rawItems instanceof List<?> list) {
            return new FetchResult(mapItemList(list), hasMore);
        }
        // Corps unique traité comme un item
        CatalogItemDto single = mapOneItem(body);
        if (single != null) {
            return new FetchResult(List.of(single), false);
        }
        return FetchResult.empty();
    }

    @SuppressWarnings("unchecked")
    private static List<CatalogItemDto> mapItemList(List<?> list) {
        List<CatalogItemDto> out = new ArrayList<>();
        for (Object o : list) {
            if (o instanceof Map<?, ?> m) {
                CatalogItemDto item = mapOneItem((Map<String, Object>) m);
                if (item != null) {
                    out.add(item);
                }
            } else if (o != null) {
                String v = String.valueOf(o);
                out.add(new CatalogItemDto(v, v, null, "receipt-outline", v, Map.of("code", v)));
            }
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    private static CatalogItemDto mapOneItem(Map<String, Object> raw) {
        String id = firstNonBlank(
                str(raw.get("id")),
                str(raw.get("code")),
                str(raw.get("value")),
                str(raw.get("key"))
        );
        String label = firstNonBlank(
                str(raw.get("label")),
                str(raw.get("name")),
                str(raw.get("libelle")),
                id
        );
        if (!StringUtils.hasText(id) || !StringUtils.hasText(label)) {
            return null;
        }
        String sub = firstNonBlank(str(raw.get("sub")), str(raw.get("description")), str(raw.get("subtitle")));
        String icon = firstNonBlank(str(raw.get("icon")), "receipt-outline");
        String value = str(raw.get("value"));
        Map<String, Object> meta;
        Object rawMeta = raw.get("meta");
        if (rawMeta instanceof Map<?, ?> mm) {
            meta = new LinkedHashMap<>((Map<String, Object>) mm);
        } else {
            meta = new LinkedHashMap<>();
        }
        meta.putIfAbsent("code", id);
        return new CatalogItemDto(id, label, sub, icon, value, Map.copyOf(meta));
    }

    private static Object first(Map<String, Object> map, String... keys) {
        for (String k : keys) {
            if (map.containsKey(k) && map.get(k) != null) {
                return map.get(k);
            }
        }
        return null;
    }

    private static String stringParam(Map<String, Object> params, String key) {
        if (params == null || !params.containsKey(key) || params.get(key) == null) {
            return null;
        }
        return String.valueOf(params.get(key));
    }

    private static String str(Object v) {
        if (v == null) {
            return null;
        }
        String s = String.valueOf(v).trim();
        return s.isEmpty() || "null".equalsIgnoreCase(s) ? null : s;
    }

    private static boolean bool(Object v) {
        return v != null && Boolean.parseBoolean(String.valueOf(v));
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String v : values) {
            if (StringUtils.hasText(v)) {
                return v.trim();
            }
        }
        return null;
    }
}
