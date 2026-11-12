package com.gateway.payment.catalog.presentation;

import com.gateway.payment.catalog.api.dto.CatalogLayoutResponse;
import com.gateway.payment.catalog.api.dto.CatalogSectionDto;
import com.gateway.payment.catalog.i18n.CatalogConfigurationLocalizer;
import com.gateway.payment.catalog.journey.CatalogJourneySettings;
import com.gateway.payment.catalog.service.CatalogProductResolver;
import com.gateway.payment.common.domain.AccessChannel;
import com.gateway.payment.common.domain.ServiceTypeCode;
import com.gateway.payment.datasources.CatalogDataOrigin;
import com.gateway.payment.datasources.DataSourceResolveService;
import com.gateway.payment.datasources.api.CatalogItemDto;
import com.gateway.payment.datasources.spi.CatalogDataSourceFetchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Matérialise {@code configuration.presentation} → sections + {@link CatalogItemDto}.
 * Origin {@link CatalogDataOrigin#LOCAL}, {@link CatalogDataOrigin#OMNICANAL}
 * ou {@link CatalogDataOrigin#GATEWAY}.
 */
@Service
public class CatalogPresentationService {

    private static final Logger log = LoggerFactory.getLogger(CatalogPresentationService.class);

    private final CatalogProductResolver catalogProductResolver;
    private final DataSourceResolveService dataSourceResolveService;
    private final CatalogConfigurationLocalizer configurationLocalizer;

    public CatalogPresentationService(
            CatalogProductResolver catalogProductResolver,
            DataSourceResolveService dataSourceResolveService,
            CatalogConfigurationLocalizer configurationLocalizer
    ) {
        this.catalogProductResolver = catalogProductResolver;
        this.dataSourceResolveService = dataSourceResolveService;
        this.configurationLocalizer = configurationLocalizer;
    }

    public CatalogLayoutResponse resolve(
            String productCode,
            String channelRaw,
            String category,
            String q,
            int page,
            int size,
            boolean hydrate
    ) {
        String code = productCode != null
                ? productCode.trim().toUpperCase(Locale.ROOT).replace(' ', '_').replace('-', '_')
                : "BILL_PAYMENT";

        CatalogProductResolver.ResolvedCatalogProduct catalog = null;
        try {
            catalog = catalogProductResolver.resolve(code, null);
        } catch (RuntimeException ex) {
            log.debug("Catalogue produit {} introuvable — fallback presentation: {}", code, ex.getMessage());
        }

        Map<String, Object> configuration = catalog != null && catalog.configuration() != null
                ? catalog.configuration()
                : Map.of();
        if (catalog != null && catalog.version() != null) {
            configuration = configurationLocalizer.localize(catalog.version().getId(), configuration);
        }

        AccessChannel channel = AccessChannel.parse(channelRaw);
        if (channel == null) {
            channel = AccessChannel.MOBILE;
        }

        CatalogJourneySettings journey = CatalogJourneySettings.from(configuration);
        String journeyCode = journey.resolveJourneyCode(null);

        CatalogPresentationSettings settings = CatalogPresentationSettings.from(configuration, channel);
        if (settings.isEmpty()) {
            if (isBillProduct(code, catalog)) {
                settings = CatalogPresentationSettings.defaultBillPayment();
            } else {
                return new CatalogLayoutResponse(
                        1,
                        code,
                        journeyCode,
                        channel.name(),
                        List.of()
                );
            }
        }

        String selectedCategory = StringUtils.hasText(q) ? null : blankToNull(category);
        List<CatalogSectionDto> sections = new ArrayList<>();
        for (CatalogPresentationSettings.SectionDef def : settings.sections()) {
            sections.add(materialize(def, configuration, selectedCategory, q, page, size, hydrate));
        }

        return new CatalogLayoutResponse(
                settings.version(),
                catalog != null ? catalog.productCode() : code,
                journeyCode,
                channel.name(),
                sections
        );
    }

    /** Layout seul (sans hydrater les sources dynamiques). */
    public CatalogLayoutResponse layoutOnly(String productCode, String channelRaw) {
        return resolve(productCode, channelRaw, null, null, 0, 30, false);
    }

    private CatalogSectionDto materialize(
            CatalogPresentationSettings.SectionDef def,
            Map<String, Object> configuration,
            String category,
            String q,
            int page,
            int size,
            boolean hydrate
    ) {
        boolean hasMore = false;
        List<CatalogItemDto> items = List.of();

        if (!hydrate) {
            return toSection(def, items, hasMore, category);
        }

        CatalogDataSourceFetchResult result =
                dataSourceResolveService.fetch(def.toDataSourceSection(), configuration, category, q, page, size);
        items = result.items();
        hasMore = result.hasMore();

        return toSection(def, items, hasMore, category);
    }

    private CatalogSectionDto toSection(
            CatalogPresentationSettings.SectionDef def,
            List<CatalogItemDto> items,
            boolean hasMore,
            String selectedCategory
    ) {
        String selectedId = null;
        if (def.displayKind() == DisplayKind.FILTER_CHIPS) {
            selectedId = selectedCategory;
        }
        return new CatalogSectionDto(
                def.id(),
                def.displayKind().name(),
                def.dataSourceCode(),
                def.origin().name(),
                def.title(),
                def.bindsTo(),
                def.searchable(),
                def.pageable(),
                hasMore,
                false,
                selectedId,
                items
        );
    }

    private static boolean isBillProduct(String code, CatalogProductResolver.ResolvedCatalogProduct catalog) {
        if (catalog != null && catalog.serviceType() == ServiceTypeCode.PAIEMENT_FACTURE) {
            return true;
        }
        return "BILL_PAYMENT".equals(code) || "PAIEMENT_FACTURE".equals(code);
    }

    private static String blankToNull(String v) {
        return StringUtils.hasText(v) ? v.trim() : null;
    }
}
