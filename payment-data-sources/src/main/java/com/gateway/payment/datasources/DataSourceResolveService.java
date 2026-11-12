package com.gateway.payment.datasources;

import com.gateway.payment.datasources.api.CatalogItemDto;
import com.gateway.payment.datasources.api.DataSourceDescriptorDto;
import com.gateway.payment.datasources.api.DataSourceResponse;
import com.gateway.payment.datasources.spi.CatalogDataSourceFetchContext;
import com.gateway.payment.datasources.spi.CatalogDataSourceFetchResult;
import com.gateway.payment.datasources.spi.CatalogDataSourceHandler;
import com.gateway.payment.datasources.spi.CatalogDataSourceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Façade publique des sources de données — catalogue, Formio, mobile.
 * <p>
 * LOCAL → {@link CatalogDataSourceRegistry} ;
 * OMNICANAL → {@link OmnicanalDataSourcePort} ;
 * GATEWAY → {@link RemoteDataSourceClient} (apps peers).
 */
@Service
public class DataSourceResolveService {

    private static final Logger log = LoggerFactory.getLogger(DataSourceResolveService.class);

    private static final Map<String, String> LABELS = Map.ofEntries(
            Map.entry("BILLER_CATEGORIES", "Catégories facturiers"),
            Map.entry("BILLERS", "Facturiers"),
            Map.entry("BILL_FAVORITES", "Favoris factures"),
            Map.entry("STATIC_ITEMS", "Items statiques"),
            Map.entry("TRANSFER_PARTICIPANTS", "Participants transfert"),
            Map.entry("CATALOG_PARTICIPANTS", "Participants casting produit"),
            Map.entry("TRANSFER_BENEFICIARIES", "Bénéficiaires enregistrés"),
            Map.entry("SWIFT_EMITTER_BANKS", "Banques émettrices SWIFT"),
            Map.entry("CUSTOM", "Source personnalisée (OMNICANAL ou handler)")
    );

    private final CatalogDataSourceRegistry registry;
    private final ObjectProvider<OmnicanalDataSourcePort> omnicanalPort;
    private final RemoteDataSourceClient remoteDataSourceClient;
    private final ObjectProvider<ProductConfigurationPort> productConfigurationPort;

    public DataSourceResolveService(
            CatalogDataSourceRegistry registry,
            ObjectProvider<OmnicanalDataSourcePort> omnicanalPort,
            RemoteDataSourceClient remoteDataSourceClient,
            ObjectProvider<ProductConfigurationPort> productConfigurationPort
    ) {
        this.registry = registry;
        this.omnicanalPort = omnicanalPort;
        this.remoteDataSourceClient = remoteDataSourceClient;
        this.productConfigurationPort = productConfigurationPort;
    }

    /** Inventaire des codes connus (builtins + handlers Spring + apps GATEWAY). */
    public List<DataSourceDescriptorDto> listDescriptors() {
        List<DataSourceDescriptorDto> out = new ArrayList<>();
        for (CatalogDataSource ds : CatalogDataSource.values()) {
            if (ds == CatalogDataSource.CUSTOM) {
                continue;
            }
            out.add(new DataSourceDescriptorDto(
                    ds.name(),
                    LABELS.getOrDefault(ds.name(), ds.name()),
                    CatalogDataOrigin.LOCAL.name(),
                    registry.has(ds.name())
            ));
        }
        for (String code : registry.codes()) {
            if (CatalogDataSource.parse(code) != CatalogDataSource.CUSTOM) {
                continue;
            }
            CatalogDataSourceHandler handler = registry.get(code).orElse(null);
            out.add(new DataSourceDescriptorDto(
                    code,
                    handler != null ? handler.label() : code,
                    CatalogDataOrigin.LOCAL.name(),
                    true
            ));
        }
        if (remoteDataSourceClient != null) {
            for (RemoteDataSourceClient.AppDescriptor app : remoteDataSourceClient.listConfiguredApps()) {
                out.add(new DataSourceDescriptorDto(
                        app.code(),
                        app.label(),
                        CatalogDataOrigin.GATEWAY.name(),
                        false
                ));
            }
        }
        out.add(new DataSourceDescriptorDto(
                CatalogDataSource.CUSTOM.name(),
                LABELS.get(CatalogDataSource.CUSTOM.name()),
                CatalogDataOrigin.OMNICANAL.name(),
                false
        ));
        return List.copyOf(out);
    }

    /**
     * Résout une source par code (appel direct API / Formio).
     *
     * @param code        code source ({@code BILLERS}, clé libre…)
     * @param originRaw   {@code LOCAL} | {@code OMNICANAL} | {@code GATEWAY} (défaut LOCAL)
     * @param params      params section (role, codeFse, limit…)
     * @param productCode optionnel — requis pour {@code CATALOG_PARTICIPANTS}
     */
    public DataSourceResponse resolve(
            String code,
            String originRaw,
            Map<String, Object> params,
            String category,
            String q,
            int page,
            int size,
            String productCode
    ) {
        String dataSourceCode = CatalogDataSourceRegistry.normalize(
                StringUtils.hasText(code) ? code : CatalogDataSource.CUSTOM.name()
        );
        CatalogDataSource source = CatalogDataSource.parse(dataSourceCode);
        CatalogDataOrigin origin = CatalogDataOrigin.parse(originRaw);
        Map<String, Object> effectiveParams = params != null ? new LinkedHashMap<>(params) : new LinkedHashMap<>();

        DataSourceSection section = new DataSourceSection(
                "api:" + dataSourceCode,
                dataSourceCode,
                source != null ? source : CatalogDataSource.CUSTOM,
                origin,
                Map.copyOf(effectiveParams),
                List.of(),
                size > 0 ? size : 30
        );

        Map<String, Object> configuration = loadConfiguration(productCode);
        CatalogDataSourceFetchResult result = fetch(section, configuration, category, q, page, size);

        return new DataSourceResponse(
                dataSourceCode,
                origin.name(),
                result.items(),
                result.hasMore(),
                Map.copyOf(effectiveParams)
        );
    }

    /**
     * Hydrate une section data-source (catalogue présentation, API, Formio).
     */
    public CatalogDataSourceFetchResult fetch(
            DataSourceSection section,
            Map<String, Object> configuration,
            String category,
            String q,
            int page,
            int size
    ) {
        int effectiveSize = section.pageSize() != null && section.pageSize() > 0
                ? section.pageSize()
                : (size > 0 ? size : 30);

        try {
            if (section.origin() == CatalogDataOrigin.OMNICANAL) {
                OmnicanalDataSourcePort port = omnicanalPort.getIfAvailable();
                if (port == null) {
                    log.warn("OMNICANAL data-source {} — OmnicanalDataSourcePort indisponible", section.dataSourceCode());
                    return CatalogDataSourceFetchResult.empty();
                }
                return port.fetch(section, category, q, page, effectiveSize);
            }
            if (section.origin() == CatalogDataOrigin.GATEWAY) {
                return fetchGateway(section, category, q, page, effectiveSize);
            }
            return fetchLocal(section, configuration, category, q, page, effectiveSize);
        } catch (RuntimeException ex) {
            log.warn(
                    "Échec résolution dataSource {} ({} / {}): {}",
                    section.id(), section.origin(), section.dataSourceCode(), ex.getMessage()
            );
            return CatalogDataSourceFetchResult.empty();
        }
    }

    private CatalogDataSourceFetchResult fetchGateway(
            DataSourceSection section,
            String category,
            String q,
            int page,
            int effectiveSize
    ) {
        if (remoteDataSourceClient == null) {
            log.warn("GATEWAY data-source {} — RemoteDataSourceClient indisponible", section.dataSourceCode());
            return CatalogDataSourceFetchResult.empty();
        }
        RemoteDataSourceResult remote = remoteDataSourceClient.fetch(
                section.dataSourceCode(),
                section.params(),
                category,
                q,
                page,
                effectiveSize
        );
        List<CatalogItemDto> items = new ArrayList<>(remote.items().size());
        for (RemoteDataSourceItem item : remote.items()) {
            items.add(new CatalogItemDto(
                    item.id(), item.label(), item.sub(), item.icon(), item.value(), item.meta()
            ));
        }
        return new CatalogDataSourceFetchResult(items, remote.hasMore());
    }

    private CatalogDataSourceFetchResult fetchLocal(
            DataSourceSection section,
            Map<String, Object> configuration,
            String category,
            String q,
            int page,
            int effectiveSize
    ) {
        CatalogDataSourceFetchContext ctx = new CatalogDataSourceFetchContext(
                section, configuration, category, q, page, effectiveSize
        );

        return registry.get(section.dataSourceCode())
                .or(() -> section.dataSource() != null
                        ? registry.get(section.dataSource().name())
                        : java.util.Optional.empty())
                .map(h -> h.fetch(ctx))
                .orElseGet(() -> {
                    log.warn(
                            "Aucun handler LOCAL pour dataSource {} (section {})",
                            section.dataSourceCode(),
                            section.id()
                    );
                    return CatalogDataSourceFetchResult.empty();
                });
    }

    /**
     * Remplit {@code data.values} des selects Formio déclarant {@code properties.rfDataSource}.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> hydrateFormSchema(Map<String, Object> schema, String productCode) {
        if (schema == null || schema.isEmpty()) {
            return schema;
        }
        Map<String, Object> copy = new LinkedHashMap<>(schema);
        Object components = copy.get("components");
        if (!(components instanceof List<?> list)) {
            return copy;
        }
        List<Object> updated = new ArrayList<>(list.size());
        for (Object c : list) {
            if (c instanceof Map<?, ?> comp) {
                updated.add(hydrateComponent(new LinkedHashMap<>((Map<String, Object>) comp), productCode));
            } else {
                updated.add(c);
            }
        }
        copy.put("components", updated);
        return copy;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> hydrateComponent(Map<String, Object> comp, String productCode) {
        Object nested = comp.get("components");
        if (nested instanceof List<?> list) {
            List<Object> kids = new ArrayList<>(list.size());
            for (Object c : list) {
                if (c instanceof Map<?, ?> m) {
                    kids.add(hydrateComponent(new LinkedHashMap<>((Map<String, Object>) m), productCode));
                } else {
                    kids.add(c);
                }
            }
            comp.put("components", kids);
        }

        Map<String, Object> rf = extractRfDataSource(comp);
        if (rf == null || !StringUtils.hasText(String.valueOf(rf.get("code")))) {
            return comp;
        }

        // dependsOn : options chargées au onChange client — pas à l'hydratation initiale
        if (hasDependsOn(rf)) {
            ensureEmptySelectValues(comp);
            if (rf.get("redrawOn") == null && comp.get("redrawOn") == null) {
                Object deps = rf.get("dependsOn");
                if (deps instanceof List<?> list && !list.isEmpty() && list.get(0) != null) {
                    comp.put("redrawOn", String.valueOf(list.get(0)));
                } else if (deps instanceof String s && StringUtils.hasText(s)) {
                    comp.put("redrawOn", s.trim());
                }
            }
            return comp;
        }

        String code = String.valueOf(rf.get("code"));
        String origin = rf.get("origin") != null ? String.valueOf(rf.get("origin")) : CatalogDataOrigin.LOCAL.name();
        Map<String, Object> params = rf.get("params") instanceof Map<?, ?> pm
                ? new LinkedHashMap<>((Map<String, Object>) pm)
                : new LinkedHashMap<>();

        DataSourceResponse resolved = resolve(code, origin, params, null, null, 0, 100, productCode);
        List<Map<String, String>> values = toFormioValues(resolved.items());

        Object data = comp.get("data");
        Map<String, Object> dataMap = data instanceof Map<?, ?> dm
                ? new LinkedHashMap<>((Map<String, Object>) dm)
                : new LinkedHashMap<>();
        dataMap.put("values", values);
        comp.put("data", dataMap);
        comp.put("dataSrc", "values");
        return comp;
    }

    private static boolean hasDependsOn(Map<String, Object> rf) {
        Object deps = rf.get("dependsOn");
        if (deps instanceof List<?> list) {
            return !list.isEmpty();
        }
        return deps instanceof String s && StringUtils.hasText(s);
    }

    @SuppressWarnings("unchecked")
    private static void ensureEmptySelectValues(Map<String, Object> comp) {
        Object data = comp.get("data");
        Map<String, Object> dataMap = data instanceof Map<?, ?> dm
                ? new LinkedHashMap<>((Map<String, Object>) dm)
                : new LinkedHashMap<>();
        if (!(dataMap.get("values") instanceof List<?>)) {
            dataMap.put("values", List.of());
        }
        comp.put("data", dataMap);
        comp.put("dataSrc", "values");
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> extractRfDataSource(Map<String, Object> comp) {
        if (comp.get("rfDataSource") instanceof Map<?, ?> direct) {
            return (Map<String, Object>) direct;
        }
        Object props = comp.get("properties");
        if (props instanceof Map<?, ?> p && p.get("rfDataSource") instanceof Map<?, ?> rf) {
            return (Map<String, Object>) rf;
        }
        return null;
    }

    private static List<Map<String, String>> toFormioValues(List<CatalogItemDto> items) {
        List<Map<String, String>> values = new ArrayList<>(items.size());
        for (CatalogItemDto item : items) {
            Map<String, String> opt = new LinkedHashMap<>();
            String value = StringUtils.hasText(item.value()) ? item.value() : item.id();
            opt.put("value", value != null ? value : "");
            opt.put("label", item.label() != null ? item.label() : value);
            values.add(opt);
        }
        return values;
    }

    private Map<String, Object> loadConfiguration(String productCode) {
        if (!StringUtils.hasText(productCode)) {
            return Map.of();
        }
        ProductConfigurationPort port = productConfigurationPort.getIfAvailable();
        if (port == null) {
            return Map.of();
        }
        try {
            Map<String, Object> cfg = port.loadConfiguration(productCode);
            return cfg != null ? cfg : Map.of();
        } catch (RuntimeException ex) {
            log.debug("Produit {} introuvable pour data-source: {}", productCode, ex.getMessage());
            return Map.of();
        }
    }
}
