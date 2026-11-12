package com.gateway.payment.payment.application;

import com.gateway.payment.catalog.domain.CatalogDeService;
import com.gateway.payment.catalog.domain.CatalogDeServiceVersion;
import com.gateway.payment.catalog.i18n.CatalogConfigurationLocalizer;
import com.gateway.payment.common.domain.PaymentStatus;
import com.gateway.payment.common.domain.ServiceTypeCode;
import com.gateway.payment.common.exception.BusinessException;
import com.gateway.payment.common.i18n.I18nScope;
import com.gateway.payment.common.i18n.TranslationService;
import com.gateway.payment.integration.api.dto.GatewayTransactionPageResponse;
import com.gateway.payment.integration.api.dto.GatewayTransactionSummary;
import com.gateway.payment.payment.api.dto.PaymentEventDto;
import com.gateway.payment.payment.api.dto.PaymentResponse;
import com.gateway.payment.payment.domain.PaymentTransaction;
import com.gateway.payment.payment.repository.PaymentEventRepository;
import com.gateway.payment.payment.repository.PaymentTransactionRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.payment.security.TenantContext;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PaymentQueryService {

    /**
     * Clés du contexte paiement exposées à l'historique (libellés + rejeu).
     * Tout le reste (OTP, config catalogue, BPM…) reste serveur.
     */
    private static final Set<String> HISTORY_CONTEXT_KEYS = Set.of(
            "amount",
            "currency",
            "phoneNumber",
            "provider",
            "destinationProvider",
            "destinationName",
            "beneficiaryId",
            "beneficiaryName",
            "destPhone",
            "destAccount",
            "accountNumber",
            "sourceAccount",
            "msisdn",
            "sourceRail",
            "billerCode",
            "billerName",
            "productCode",
            "productName",
            "productJourneyCode",
            "billReference",
            "meterNumber",
            "customerReference",
            "withdrawalChannel",
            "payProvider",
            "payProviderCode",
            "lookupMode",
            "amountPolicy",
            "channel",
            "accessChannel",
            // Contrepartie / narratif (libellés serveur)
            "recipientName",
            "counterpartyName",
            "merchantName",
            "agentName",
            "senderName",
            "beneficiaryPhone",
            "destinationAccount",
            "beneficiaryAccount",
            "iban",
            "agentLabel",
            "uniteLabel",
            "agencyName",
            // Point de retrait / dépôt (cash-out / cash-in agent)
            "agentUniteDisplay",
            "agentUniteLibelle",
            "agentUniteId",
            "agentCode",
            "agentNetwork",
            "agentUniteType",
            "agentZone",
            "agentFseCode",
            "agentFseLibelle",
            "agentAliasCode",
            "agentAccount",
            "motive",
            "purpose",
            "remittanceInfo",
            "description",
            "narration",
            "invoiceNumber",
            "contratNumber",
            "voucherCode",
            "withdrawalCode",
            "generatedCode",
            "endToEndId",
            "uetr",
            "bankReference",
            "externalReference",
            "providerReference"
    );

    private final PaymentTransactionRepository transactionRepository;
    private final PaymentEventRepository eventRepository;
    private final ObjectMapper objectMapper;
    private final HistoryLabelFormatter historyLabelFormatter;
    private final TranslationService translations;
    private final CatalogConfigurationLocalizer configurationLocalizer;

    public PaymentQueryService(
            PaymentTransactionRepository transactionRepository,
            PaymentEventRepository eventRepository,
            ObjectMapper objectMapper,
            HistoryLabelFormatter historyLabelFormatter,
            TranslationService translations,
            CatalogConfigurationLocalizer configurationLocalizer
    ) {
        this.transactionRepository = transactionRepository;
        this.eventRepository = eventRepository;
        this.objectMapper = objectMapper;
        this.historyLabelFormatter = historyLabelFormatter;
        this.translations = translations;
        this.configurationLocalizer = configurationLocalizer;
    }

    @Transactional(readOnly = true)
    public List<PaymentEventDto> timeline(String paymentId) {
        ensureExists(paymentId);
        return eventRepository.findByPaymentIdOrderByCreatedAtAsc(paymentId).stream()
                .map(e -> new PaymentEventDto(e.getCreatedAt(), e.getEventType(), e.getMessage()))
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<PaymentResponse> search(
            String paymentId,
            String provider,
            String status,
            String customerReference,
            String providerReference,
            String phoneNumber,
            Instant from,
            Instant to,
            Pageable pageable
    ) {
        Specification<PaymentTransaction> spec = (root, query, cb) -> {
            // Évite N+1 catalogue pour les libellés produit.
            if (query != null
                    && query.getResultType() != Long.class
                    && query.getResultType() != long.class) {
                root.fetch("catalogServiceVersion", JoinType.LEFT)
                        .fetch("catalogDeService", JoinType.LEFT);
                query.distinct(true);
            }
            return cb.conjunction();
        };
        if (paymentId != null && !paymentId.isBlank()) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("paymentId"), paymentId));
        }
        if (provider != null && !provider.isBlank()) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("providerCode"), provider));
        }
        if (status != null && !status.isBlank()) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("status"), PaymentStatus.valueOf(status)));
        }
        if (customerReference != null && !customerReference.isBlank()) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("customerReference"), customerReference));
        }
        if (providerReference != null && !providerReference.isBlank()) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("providerReference"), providerReference));
        }
        if (phoneNumber != null && !phoneNumber.isBlank()) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("phoneNumber"), phoneNumber));
        }
        if (from != null) {
            spec = spec.and((root, q, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), from));
        }
        if (to != null) {
            spec = spec.and((root, q, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), to));
        }
        spec = applyTenantFilter(spec);
        Page<PaymentTransaction> result = transactionRepository.findAll(spec, pageable);
        Map<UUID, TranslationService.Bundle> catalogBundles = catalogNameBundles(result.getContent());
        return result.map(tx -> toAdminListResponse(tx, catalogBundles));
    }

    /**
     * Historique canal — lecture légère depuis {@code payment_transaction}
     * (sans résolution nextAction / PaymentEngine).
     */
    @Transactional(readOnly = true)
    public GatewayTransactionPageResponse searchHistory(
            String customerReference,
            String status,
            String serviceType,
            String catalogProduct,
            String provider,
            String q,
            Instant from,
            Instant to,
            int page,
            int size
    ) {
        return searchHistory(
                customerReference, null, status, serviceType, catalogProduct, provider, q, from, to, page, size);
    }

    @Transactional(readOnly = true)
    public GatewayTransactionPageResponse searchHistory(
            String customerReference,
            String initiatedBy,
            String status,
            String serviceType,
            String catalogProduct,
            String provider,
            String q,
            Instant from,
            Instant to,
            int page,
            int size
    ) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(1, size), 100);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        Specification<PaymentTransaction> spec = buildHistorySpec(
                customerReference, initiatedBy, status, serviceType, catalogProduct, provider, q, from, to);
        Page<PaymentTransaction> result = transactionRepository.findAll(spec, pageable);
        Map<UUID, TranslationService.Bundle> catalogBundles = catalogNameBundles(result.getContent());
        List<GatewayTransactionSummary> items = result.getContent().stream()
                .map(tx -> toHistorySummary(tx, catalogBundles))
                .toList();
        boolean hasMore = result.hasNext();
        return new GatewayTransactionPageResponse(items, safePage, safeSize, result.getTotalElements(), hasMore);
    }

    private Map<UUID, TranslationService.Bundle> catalogNameBundles(List<PaymentTransaction> txs) {
        List<UUID> ids = txs.stream()
                .map(PaymentQueryService::catalogServiceId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (ids.isEmpty()) {
            return Map.of();
        }
        return translations.bundles(I18nScope.CATALOG_DE_SERVICE, ids);
    }

    private static UUID catalogServiceId(PaymentTransaction tx) {
        if (tx.getCatalogServiceVersion() == null
                || tx.getCatalogServiceVersion().getCatalogDeService() == null) {
            return null;
        }
        return tx.getCatalogServiceVersion().getCatalogDeService().getId();
    }

    private Specification<PaymentTransaction> buildHistorySpec(
            String customerReference,
            String initiatedBy,
            String status,
            String serviceType,
            String catalogProduct,
            String provider,
            String q,
            Instant from,
            Instant to
    ) {
        Specification<PaymentTransaction> spec = (root, query, cb) -> {
            // Évite N+1 sur catalogServiceCode (sauf count query).
            if (query != null
                    && query.getResultType() != Long.class
                    && query.getResultType() != long.class) {
                root.fetch("catalogServiceVersion", JoinType.LEFT)
                        .fetch("catalogDeService", JoinType.LEFT);
                query.distinct(true);
            }
            return cb.conjunction();
        };
        String scopedInitiator = resolveInitiatedByScope(initiatedBy);
        if (StringUtils.hasText(scopedInitiator)) {
            // context_json.initiatedBy posé à la création (PaymentEngine / JWT username).
            String needle = "\"initiatedBy\":\"" + scopedInitiator.trim() + "\"";
            spec = spec.and((root, query, cb) -> cb.like(root.get("contextJson"), "%" + needle + "%"));
        }
        if (StringUtils.hasText(customerReference)) {
            String ref = customerReference.trim();
            spec = spec.and((root, query, cb) -> cb.equal(root.get("customerReference"), ref));
        }
        if (StringUtils.hasText(status)) {
            PaymentStatus st = PaymentStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), st));
        }
        if (StringUtils.hasText(serviceType)) {
            ServiceTypeCode st = ServiceTypeCode.valueOf(serviceType.trim().toUpperCase(Locale.ROOT));
            spec = spec.and((root, query, cb) -> cb.equal(root.get("serviceType"), st));
        }
        if (StringUtils.hasText(catalogProduct)) {
            String code = catalogProduct.trim().toUpperCase(Locale.ROOT);
            spec = spec.and((root, query, cb) -> {
                Join<PaymentTransaction, CatalogDeServiceVersion> ver =
                        root.join("catalogServiceVersion", JoinType.LEFT);
                Join<CatalogDeServiceVersion, CatalogDeService> svc =
                        ver.join("catalogDeService", JoinType.LEFT);
                return cb.equal(cb.upper(svc.get("code")), code);
            });
        }
        if (StringUtils.hasText(provider)) {
            String code = provider.trim();
            spec = spec.and((root, query, cb) -> cb.equal(root.get("providerCode"), code));
        }
        if (StringUtils.hasText(q)) {
            String term = q.trim();
            String like = "%" + term.toLowerCase(Locale.ROOT) + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("paymentId")), like),
                    cb.like(cb.lower(root.get("providerReference")), like),
                    cb.like(cb.lower(root.get("phoneNumber")), like),
                    cb.like(cb.lower(root.get("customerReference")), like),
                    cb.like(cb.lower(root.get("merchantReference")), like)
            ));
        }
        if (from != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), from));
        }
        if (to != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), to));
        }
        return applyTenantFilter(spec);
    }

    private Specification<PaymentTransaction> applyTenantFilter(Specification<PaymentTransaction> spec) {
        if (TenantContext.isPlatformUser()) {
            return spec;
        }
        String tenant = TenantContext.currentTenantId();
        if (tenant == null || tenant.isBlank()) {
            return spec;
        }
        return spec.and((root, q, cb) -> cb.or(
                cb.equal(root.get("tenantId"), tenant),
                cb.isNull(root.get("tenantId"))));
    }

    /**
     * Canal retail (CUSTOMER) : force l'historique sur l'utilisateur JWT.
     * Admin / ops : filtre {@code initiatedBy} optionnel uniquement.
     */
    private static String resolveInitiatedByScope(String requested) {
        String me = TenantContext.currentUsername();
        if (!TenantContext.isPlatformUser() && StringUtils.hasText(me)) {
            return me.trim();
        }
        return StringUtils.hasText(requested) ? requested.trim() : null;
    }

    /**
     * Ligne supervision — même libellés que l'historique canal, sans nextAction
     * (évite N appels {@link PaymentEngine#get}).
     */
    private PaymentResponse toAdminListResponse(
            PaymentTransaction tx,
            Map<UUID, TranslationService.Bundle> catalogBundles
    ) {
        CatalogDeService catalog = null;
        String catalogCode = null;
        UUID catalogVersionId = null;
        String catalogVersion = null;
        if (tx.getCatalogServiceVersion() != null) {
            catalogVersionId = tx.getCatalogServiceVersion().getId();
            catalogVersion = tx.getCatalogServiceVersion().getVersion();
            if (tx.getCatalogServiceVersion().getCatalogDeService() != null) {
                catalog = tx.getCatalogServiceVersion().getCatalogDeService();
                catalogCode = catalog.getCode();
            }
        }
        Map<String, Object> context = sanitizeHistoryContext(tx.getContextJson());
        String catalogName = null;
        if (catalog != null) {
            TranslationService.Bundle bundle = catalogBundles.getOrDefault(
                    catalog.getId(), TranslationService.Bundle.empty(translations.defaultLanguage()));
            catalogName = bundle.text(I18nScope.FIELD_NAME, catalog.getName());
        }
        String status = tx.getStatus() != null ? tx.getStatus().name() : null;
        String serviceType = tx.getServiceType() != null ? tx.getServiceType().name() : null;
        HistoryLabelFormatter.Labels labels = historyLabelFormatter.format(
                catalogCode,
                serviceType,
                status,
                tx.getProviderCode(),
                tx.getCreatedAt(),
                context,
                catalogName,
                historyConfigOf(tx)
        );
        Object journey = context.get("journeyCode");
        Object channel = context.get("accessChannel");
        if (channel == null) {
            channel = context.get("channel");
        }
        return PaymentResponse.builder()
                .paymentId(tx.getPaymentId())
                .provider(tx.getProviderCode())
                .serviceType(tx.getServiceType())
                .catalogServiceCode(catalogCode)
                .catalogServiceVersionId(catalogVersionId)
                .catalogServiceVersion(catalogVersion)
                .journeyCode(journey != null ? String.valueOf(journey) : null)
                .accessChannel(channel != null ? String.valueOf(channel) : null)
                .amount(tx.getAmount())
                .currency(tx.getCurrency())
                .status(tx.getStatus())
                .currentStep(tx.getCurrentStep())
                .providerReference(tx.getProviderReference())
                .customerReference(tx.getCustomerReference())
                .phoneNumber(tx.getPhoneNumber())
                .errorCode(tx.getErrorCode())
                .errorMessage(tx.getErrorMessage())
                .createdAt(tx.getCreatedAt())
                .updatedAt(tx.getUpdatedAt())
                .title(labels.title())
                .subtitle(labels.subtitle())
                .productLabel(labels.productLabel())
                .statusLabel(labels.statusLabel())
                .agentDisplay(historyLabelFormatter.agentDisplay(context))
                .withdrawalChannel(strCtx(context, "withdrawalChannel"))
                .build();
    }

    private static String strCtx(Map<String, Object> ctx, String key) {
        Object v = ctx.get(key);
        if (v == null) {
            return null;
        }
        String s = String.valueOf(v).trim();
        return s.isEmpty() ? null : s;
    }

    /** Section history du produit, localisée (Accept-Language). */
    private Map<String, Object> historyConfigOf(PaymentTransaction tx) {
        try {
            CatalogDeServiceVersion version = tx.getCatalogServiceVersion();
            if (version == null || version.getId() == null) {
                return null;
            }
            return configurationLocalizer.historyOf(version.getId(), version.getConfiguration());
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private GatewayTransactionSummary toHistorySummary(
            PaymentTransaction tx,
            Map<UUID, TranslationService.Bundle> catalogBundles
    ) {
        CatalogDeService catalog = null;
        String catalogCode = null;
        if (tx.getCatalogServiceVersion() != null
                && tx.getCatalogServiceVersion().getCatalogDeService() != null) {
            catalog = tx.getCatalogServiceVersion().getCatalogDeService();
            catalogCode = catalog.getCode();
        }
        Map<String, Object> context = sanitizeHistoryContext(tx.getContextJson());
        String catalogName = null;
        if (catalog != null) {
            TranslationService.Bundle bundle = catalogBundles.getOrDefault(
                    catalog.getId(), TranslationService.Bundle.empty(translations.defaultLanguage()));
            catalogName = bundle.text(I18nScope.FIELD_NAME, catalog.getName());
        }
        String status = tx.getStatus() != null ? tx.getStatus().name() : null;
        String serviceType = tx.getServiceType() != null ? tx.getServiceType().name() : null;
        HistoryLabelFormatter.Labels labels = historyLabelFormatter.format(
                catalogCode,
                serviceType,
                status,
                tx.getProviderCode(),
                tx.getCreatedAt(),
                context,
                catalogName,
                historyConfigOf(tx)
        );
        return new GatewayTransactionSummary(
                tx.getPaymentId(),
                tx.getProviderCode(),
                serviceType,
                catalogCode,
                tx.getAmount(),
                tx.getCurrency(),
                status,
                tx.getCurrentStep(),
                tx.getProviderReference(),
                tx.getCustomerReference(),
                tx.getPhoneNumber(),
                tx.getErrorCode(),
                tx.getCreatedAt(),
                tx.getUpdatedAt(),
                tx.getCompletedAt(),
                labels.title(),
                labels.subtitle(),
                labels.productLabel(),
                labels.statusLabel(),
                historyLabelFormatter.agentDisplay(context),
                context
        );
    }

    /**
     * Extrait du {@code contextJson} uniquement les champs utiles à l'UI
     * (titre digeste) et au rejeu d'un nouveau paiement.
     */
    private Map<String, Object> sanitizeHistoryContext(String contextJson) {
        if (!StringUtils.hasText(contextJson)) {
            return Map.of();
        }
        try {
            Map<String, Object> raw = objectMapper.readValue(contextJson, new TypeReference<>() {});
            if (raw == null || raw.isEmpty()) {
                return Map.of();
            }
            Map<String, Object> out = new LinkedHashMap<>();
            for (String key : HISTORY_CONTEXT_KEYS) {
                Object value = raw.get(key);
                if (value == null) {
                    continue;
                }
                if (value instanceof String s && s.isBlank()) {
                    continue;
                }
                if (value instanceof Map || value instanceof List) {
                    continue;
                }
                out.put(key, value);
            }
            return out.isEmpty() ? Map.of() : Map.copyOf(out);
        } catch (Exception ignored) {
            return Map.of();
        }
    }

    @Transactional(readOnly = true)
    public Map<String, Object> dailyStats() {
        Instant start = LocalDate.now(ZoneOffset.UTC).atStartOfDay().toInstant(ZoneOffset.UTC);
        String tenantFilter = TenantContext.isPlatformUser() ? null : TenantContext.currentTenantId();
        List<Object[]> rows = tenantFilter == null
                ? transactionRepository.countByStatusSince(start)
                : transactionRepository.countByStatusSinceForTenant(start, tenantFilter);
        long total = 0;
        long success = 0;
        long pending = 0;
        long failed = 0;
        Map<String, Long> byStatus = new HashMap<>();
        for (Object[] row : rows) {
            PaymentStatus st = (PaymentStatus) row[0];
            long count = (Long) row[1];
            byStatus.put(st.name(), count);
            total += count;
            if (st == PaymentStatus.SUCCESS) {
                success += count;
            } else if (st == PaymentStatus.FAILED || st == PaymentStatus.CANCELLED || st == PaymentStatus.EXPIRED) {
                failed += count;
            } else {
                pending += count;
            }
        }
        double rate = total == 0 ? 0 : (success * 100.0 / total);
        Map<String, Object> result = new HashMap<>();
        result.put("date", LocalDate.now(ZoneOffset.UTC).toString());
        result.put("total", total);
        result.put("success", success);
        result.put("pending", pending);
        result.put("failed", failed);
        result.put("successRate", Math.round(rate * 10.0) / 10.0);
        result.put("byStatus", byStatus);
        result.put("tenantId", tenantFilter);
        return result;
    }

    private void ensureExists(String paymentId) {
        PaymentTransaction tx = transactionRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new BusinessException("PAYMENT_NOT_FOUND", "Paiement introuvable", HttpStatus.NOT_FOUND));
        if (!TenantContext.canAccess(tx.getTenantId())) {
            throw new BusinessException("PAYMENT_FORBIDDEN", "Accès refusé à ce paiement", HttpStatus.FORBIDDEN);
        }
    }
}
