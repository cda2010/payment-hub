package com.gateway.payment.catalog.presentation.datasource;

import com.gateway.payment.billing.api.dto.BillerCategoryResponse;
import com.gateway.payment.billing.api.dto.BillerResponse;
import com.gateway.payment.billing.domain.BillerCategory;
import com.gateway.payment.catalog.casting.CatalogParticipantDefinition;
import com.gateway.payment.catalog.casting.CatalogProductCasting;
import com.gateway.payment.catalog.casting.CatalogProductCastingService;
import com.gateway.payment.configuration.domain.PaymentProvider;
import com.gateway.payment.configuration.repository.PaymentProviderRepository;
import com.gateway.payment.datasources.StaticCatalogItem;
import com.gateway.payment.datasources.api.CatalogItemDto;
import com.gateway.payment.transfer.api.dto.TransferParticipantResponse;
import com.gateway.payment.transfer.domain.ParticipantKind;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** Mapping métier → {@link CatalogItemDto} pour les sources LOCAL. */
@Component
public class CatalogLocalItemMapper {

    private static final Map<String, String> CATEGORY_ICONS = Map.of(
            "ENERGY_WATER", "flash-outline",
            "TELECOM_MEDIA", "tv-outline",
            "EDUCATION", "school-outline",
            "GOVERNMENT", "business-outline",
            "INSURANCE_HEALTH", "medkit-outline",
            "OTHER", "receipt-outline"
    );

    private static final Map<ParticipantKind, String> PARTICIPANT_ICONS = Map.of(
            ParticipantKind.BANK, "business-outline",
            ParticipantKind.WALLET, "wallet-outline"
    );

    private static final Map<String, String> PAY_PROVIDER_ICONS = Map.of(
            "ORANGE_MONEY", "phone-portrait-outline",
            "WAVE", "water-outline",
            "CORIS_MONEY", "wallet-outline",
            "MOOV_MONEY", "phone-portrait-outline",
            "FREE_MONEY", "phone-portrait-outline",
            "CARD_PAYMENT", "card-outline",
            "CORIS_BANK", "business-outline"
    );

    private final CatalogProductCastingService castingService;
    private final PaymentProviderRepository providerRepository;

    public CatalogLocalItemMapper(
            CatalogProductCastingService castingService,
            PaymentProviderRepository providerRepository
    ) {
        this.castingService = castingService;
        this.providerRepository = providerRepository;
    }

    public List<CatalogItemDto> mapCategories(List<BillerCategoryResponse> categories) {
        List<CatalogItemDto> out = new ArrayList<>(categories.size());
        for (BillerCategoryResponse c : categories) {
            Map<String, Object> meta = new LinkedHashMap<>();
            meta.put("code", c.code());
            meta.put("billerCount", c.billerCount());
            meta.put("sortOrder", c.sortOrder());
            out.add(new CatalogItemDto(
                    c.code(),
                    c.label(),
                    String.valueOf(c.billerCount()),
                    CATEGORY_ICONS.getOrDefault(c.code(), "receipt-outline"),
                    null,
                    meta
            ));
        }
        return out;
    }

    public CatalogItemDto mapBiller(BillerResponse b) {
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("code", b.code());
        if (b.categoryCode() != null) {
            meta.put("categoryCode", b.categoryCode());
        }
        if (b.id() != null) {
            meta.put("billerId", b.id().toString());
        }
        String sub = firstNonBlank(b.description(), b.categoryLabel(), b.providerCode(), "Facture");
        return new CatalogItemDto(b.code(), b.name(), sub, categoryIcon(b.categoryCode()), null, meta);
    }

    public CatalogItemDto mapStatic(StaticCatalogItem def) {
        return new CatalogItemDto(
                def.id(),
                def.label(),
                def.sub(),
                def.icon() != null ? def.icon() : "receipt-outline",
                def.value(),
                def.meta() != null ? def.meta() : Map.of()
        );
    }

    /**
     * Participants du casting produit → items. Filtre par {@code params.role}.
     */
    public List<CatalogItemDto> mapCatalogParticipants(
            Map<String, Object> configuration,
            Map<String, Object> params
    ) {
        CatalogProductCasting casting = castingService.parse(configuration);
        String role = params.get("role") instanceof String s ? s.trim().toUpperCase(Locale.ROOT) : "";
        List<CatalogParticipantDefinition> filtered = casting.participants().stream()
                .filter(p -> role.isBlank() || role.equalsIgnoreCase(p.role()))
                .toList();
        if (filtered.isEmpty()) {
            return List.of();
        }
        Set<String> codes = filtered.stream()
                .map(CatalogParticipantDefinition::providerCode)
                .filter(StringUtils::hasText)
                .map(c -> c.trim().toUpperCase(Locale.ROOT))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<String, PaymentProvider> byCode = new LinkedHashMap<>();
        for (String code : codes) {
            providerRepository.findByCode(code).ifPresent(p -> byCode.put(code, p));
        }
        List<CatalogItemDto> out = new ArrayList<>(filtered.size());
        for (CatalogParticipantDefinition p : filtered) {
            String code = p.providerCode() != null ? p.providerCode().trim().toUpperCase(Locale.ROOT) : "";
            if (code.isBlank()) {
                continue;
            }
            PaymentProvider provider = byCode.get(code);
            String label = provider != null && StringUtils.hasText(provider.getLibelle())
                    ? provider.getLibelle()
                    : code;
            Map<String, Object> meta = new LinkedHashMap<>();
            meta.put("code", code);
            meta.put("role", p.role());
            meta.put("default", p.defaultParticipant());
            out.add(new CatalogItemDto(
                    code,
                    label,
                    catalogParticipantSub(code),
                    PAY_PROVIDER_ICONS.getOrDefault(code, "wallet-outline"),
                    null,
                    meta
            ));
        }
        return out;
    }

    /**
     * Participants transfert → items. {@code params.role} = SOURCE | DESTINATION.
     */
    public static List<CatalogItemDto> mapParticipants(
            List<TransferParticipantResponse> participants,
            Map<String, Object> params
    ) {
        String role = params.get("role") instanceof String s ? s.trim().toUpperCase(Locale.ROOT) : "";
        List<CatalogItemDto> out = new ArrayList<>(participants.size());
        for (TransferParticipantResponse p : participants) {
            if ("SOURCE".equals(role) && !p.canEmit()) {
                continue;
            }
            if ("DESTINATION".equals(role) && !p.canReceive()) {
                continue;
            }
            Map<String, Object> meta = new LinkedHashMap<>();
            meta.put("code", p.code());
            meta.put("canEmit", p.canEmit());
            meta.put("canReceive", p.canReceive());
            if (p.participantKind() != null) {
                meta.put("participantKind", p.participantKind().name());
            }
            if (p.groupCode() != null) {
                meta.put("groupCode", p.groupCode());
            }
            if (p.minAmount() != null) {
                meta.put("minAmount", p.minAmount());
            }
            if (p.maxAmount() != null) {
                meta.put("maxAmount", p.maxAmount());
            }
            out.add(new CatalogItemDto(
                    p.code(),
                    firstNonBlank(p.name(), p.code()),
                    participantSub(p),
                    participantIcon(p),
                    null,
                    meta
            ));
        }
        return out;
    }

    private static String catalogParticipantSub(String code) {
        if (code.contains("CARD")) {
            return "Carte";
        }
        if (code.contains("BANK")) {
            return "Compte bancaire";
        }
        return "Wallet mobile";
    }

    private static String participantIcon(TransferParticipantResponse p) {
        return p.participantKind() == null
                ? "wallet-outline"
                : PARTICIPANT_ICONS.getOrDefault(p.participantKind(), "wallet-outline");
    }

    private static String participantSub(TransferParticipantResponse p) {
        return p.participantKind() == ParticipantKind.BANK ? "Compte bancaire" : "Wallet";
    }

    private static String categoryIcon(String categoryCode) {
        if (!StringUtils.hasText(categoryCode)) {
            return "receipt-outline";
        }
        return CATEGORY_ICONS.getOrDefault(
                BillerCategory.orOther(categoryCode).name(),
                "receipt-outline"
        );
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
