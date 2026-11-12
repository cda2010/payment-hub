package com.gateway.payment.integration.service;

import com.gateway.payment.approval.api.dto.ApprovalDtos;
import com.gateway.payment.approval.service.ApprovalQueryService;
import com.gateway.payment.billing.api.dto.BillSearchResponse;
import com.gateway.payment.billing.api.dto.BillerCategoryResponse;
import com.gateway.payment.billing.api.dto.BillerPageResponse;
import com.gateway.payment.billing.api.dto.BillerProductResponse;
import com.gateway.payment.billing.api.dto.BillerResponse;
import com.gateway.payment.billing.service.BillCatalogService;
import com.gateway.payment.billing.service.BillerService;
import com.gateway.payment.catalog.api.dto.CatalogLayoutResponse;
import com.gateway.payment.datasources.api.DataSourceDescriptorDto;
import com.gateway.payment.datasources.api.DataSourceResponse;
import com.gateway.payment.catalog.gateway.CatalogGatewaySettings;
import com.gateway.payment.catalog.journey.CatalogJourneySettings;
import com.gateway.payment.catalog.presentation.CatalogPresentationService;
import com.gateway.payment.datasources.DataSourceResolveService;
import com.gateway.payment.catalog.service.CatalogProductResolver;
import com.gateway.payment.common.domain.AccessChannel;
import com.gateway.payment.common.domain.ServiceTypeCode;
import com.gateway.payment.common.exception.BusinessException;
import com.gateway.payment.configuration.api.dto.ProviderResponse;
import com.gateway.payment.configuration.domain.FseParticipantKind;
import com.gateway.payment.configuration.service.ProviderService;
import com.gateway.payment.integration.api.dto.GatewayTransactionPageResponse;
import com.gateway.payment.integration.api.dto.IntegrationScreenResponse;
import com.gateway.payment.payment.api.dto.PaymentResponse;
import com.gateway.payment.payment.application.PaymentEngine;
import com.gateway.payment.payment.application.PaymentQueryService;
import com.gateway.payment.transfer.api.dto.TransferParticipantResponse;
import com.gateway.payment.transfer.service.TransferCatalogService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Façade produit pour systèmes externes.
 * <p>
 * Entrée générique : {@link #startProduct(String, Map)} — plus de Java par produit.
 * Les alias {@code /checkout}, {@code /rtgs}… restent pour compat.
 */
@Service
public class ProductGatewayFacade {

    private final PaymentEngine paymentEngine;
    private final ProviderService providerService;
    private final TransferCatalogService transferCatalogService;
    private final BillerService billerService;
    private final BillCatalogService billCatalogService;
    private final ApprovalQueryService approvalQueryService;
    private final CatalogProductResolver catalogProductResolver;
    private final CatalogPresentationService catalogPresentationService;
    private final DataSourceResolveService dataSourceResolveService;
    private final PaymentQueryService paymentQueryService;

    public ProductGatewayFacade(
            PaymentEngine paymentEngine,
            ProviderService providerService,
            TransferCatalogService transferCatalogService,
            BillerService billerService,
            BillCatalogService billCatalogService,
            ApprovalQueryService approvalQueryService,
            CatalogProductResolver catalogProductResolver,
            CatalogPresentationService catalogPresentationService,
            DataSourceResolveService dataSourceResolveService,
            PaymentQueryService paymentQueryService
    ) {
        this.paymentEngine = paymentEngine;
        this.providerService = providerService;
        this.transferCatalogService = transferCatalogService;
        this.billerService = billerService;
        this.billCatalogService = billCatalogService;
        this.approvalQueryService = approvalQueryService;
        this.catalogProductResolver = catalogProductResolver;
        this.catalogPresentationService = catalogPresentationService;
        this.dataSourceResolveService = dataSourceResolveService;
        this.paymentQueryService = paymentQueryService;
    }

    /**
     * Démarrage générique d'un produit catalogue PUBLISHED (sans code Java dédié).
     */
    public IntegrationScreenResponse startProduct(String productCode, Map<String, Object> request) {
        String code = normalizeProductCode(productCode);
        Map<String, Object> req = request != null ? request : Map.of();

        // Produits historiques à logique composite — délègue aux helpers dédiés.
        if ("BILL_PAYMENT".equals(code) || "PAIEMENT_FACTURE".equals(code)) {
            return startBillPayment(req);
        }
        if ("CHECKOUT".equals(code) || "PAYEMENT".equals(code) || "PAYMENT".equals(code)) {
            Map<String, Object> checkoutReq = new LinkedHashMap<>(req);
            checkoutReq.putIfAbsent("catalogServiceCode", code);
            return startCheckout(checkoutReq);
        }
        if ("TONTINE".equals(code)) {
            return startCheckout(checkoutForTontine(req));
        }

        CatalogProductResolver.ResolvedCatalogProduct catalog =
                catalogProductResolver.resolve(code, null);
        if (catalog == null) {
            throw new BusinessException("CATALOG_SERVICE_NOT_FOUND",
                    "Produit catalogue introuvable: " + code, HttpStatus.NOT_FOUND);
        }

        ServiceTypeCode serviceType = catalog.serviceType();
        if (serviceType == null) {
            throw new BusinessException("CATALOG_SERVICE_TYPE_MISSING",
                    "serviceType manquant sur le produit " + code
                            + " — déclarez-le dans configuration.serviceType");
        }
        if (serviceType.isBillPayment()) {
            return startBillPayment(req);
        }
        if (serviceType == ServiceTypeCode.PAYEMENT) {
            // Propager le code catalogue (ex. COFFRE) — sinon startCheckout retombe sur CHECKOUT.
            Map<String, Object> checkoutReq = new LinkedHashMap<>(req);
            checkoutReq.putIfAbsent("catalogServiceCode", code);
            return startCheckout(checkoutReq);
        }

        CatalogGatewaySettings gateway = CatalogGatewaySettings.fromConfiguration(catalog.configuration());
        CatalogJourneySettings journey = CatalogJourneySettings.from(catalog.configuration());
        Map<String, Object> data = dataMap(req);
        Map<String, Object> body = new LinkedHashMap<>();

        String requestedChannel = firstNonBlank(str(req.get("channel")), str(req.get("accessChannel")));
        AccessChannel channel = journey.resolveChannel(requestedChannel);
        String requestedAudience = firstNonBlank(
                str(req.get("audience")),
                str(req.get("productAudience")),
                str(req.get("accessAudience")));
        var audience = journey.resolveAudience(requestedAudience);
        String journeyCode = journey.resolveJourneyCode(str(req.get("journeyCode")));
        if (StringUtils.hasText(journeyCode)) {
            data.put("journeyCode", journeyCode);
        }
        if (channel != null) {
            data.put("accessChannel", channel.name());
            data.put("channel", channel.name());
        }
        if (audience != null) {
            data.put("productAudience", audience.name());
            data.put("audience", audience.name());
        }

        String provider = str(req.get("provider"));
        if (gateway.providerRequired() && !StringUtils.hasText(provider)) {
            throw new BusinessException("PROVIDER_REQUIRED", "provider is required", HttpStatus.BAD_REQUEST);
        }
        if (StringUtils.hasText(provider)) {
            body.put("provider", provider);
        } else if (gateway.optionalProviderAtStart()) {
            body.put("provider", "");
        }

        Object amount = req.get("amount");
        if (amount != null) {
            body.put("amount", toDecimal(amount));
        }
        body.put("currency", req.getOrDefault("currency", gateway.defaultCurrency("XOF")));
        putIfAbsent(body, "phoneNumber", req.get("phoneNumber"));
        putIfAbsent(body, "customerReference", req.get("customerReference"));
        Object merchant = req.get("merchantReference");
        if (merchant == null || String.valueOf(merchant).isBlank()) {
            merchant = "EXT-" + UUID.randomUUID().toString().substring(0, 8);
        }
        body.put("merchantReference", merchant);

        Object roles = req.get("roleAssignments");
        if (roles == null) {
            Map<String, Object> assignments = new LinkedHashMap<>();
            if (StringUtils.hasText(provider)) {
                assignments.put("SOURCE", provider);
            }
            Object dest = req.get("destinationProvider");
            if (dest != null && !String.valueOf(dest).isBlank()) {
                assignments.put("DESTINATION", dest);
            }
            if (!assignments.isEmpty()) {
                roles = assignments;
            }
        }
        if (roles != null) {
            data.put("roleAssignments", roles);
        }
        putIfAbsent(data, "destinationProvider", req.get("destinationProvider"));
        data.put("catalogProductCode", catalog.productCode());

        for (String field : gateway.copyRequestFields()) {
            putIfAbsent(data, field, req.get(field));
        }
        // Champs fréquents transfert / RTGS / SWIFT sans config explicite
        for (String field : List.of(
                "purpose", "bic", "iban", "countryCode", "chargeBearer",
                "skipCutoffCheck", "skipComplianceCheck", "beneficiaryName", "bankCode",
                "agentNetwork", "agentCode", "sourceAccount", "destinationAccount",
                "withdrawalChannel", "gabReference", "agencyCode",
                "destinationRail", "providerRail", "accountAlias", "cashOutCode", "cashInCode",
                "senderName", "senderPhone", "beneficiaryPhone", "destinationCountry",
                "receiveCurrency", "providerCode", "payoutMethod", "payoutAccount",
                "sourceRail", "quoteId", "remittanceReference", "mtcn", "reference",
                "beneficiaryIdNumber", "beneficiaryIdType", "parentPaymentId")) {
            putIfAbsent(data, field, req.get(field));
        }
        for (Map.Entry<String, Object> e : req.entrySet()) {
            String key = e.getKey();
            if (key == null || isReservedStartKey(key)) {
                continue;
            }
            putIfAbsent(data, key, e.getValue());
        }

        body.put("data", data);
        return create(body, serviceType, catalog.productCode(), str(req.get("idempotencyKey")));
    }

    /**
     * La cotisation s'encaisse sur le produit CHECKOUT. Le code TONTINE ne porte
     * pas son propre débit : identifiant, tour et compte à créditer restent dans le contexte.
     */
    private Map<String, Object> checkoutForTontine(Map<String, Object> request) {
        Map<String, Object> checkoutReq = new LinkedHashMap<>(request);
        checkoutReq.put("catalogServiceCode", "CHECKOUT");
        Map<String, Object> data = dataMap(checkoutReq);
        for (String field : List.of(
                "tontineId", "tontineName", "inviteCode", "round", "beneficiaryName",
                "destAccount", "compteCredit")) {
            putIfAbsent(data, field, request.get(field));
        }
        checkoutReq.put("data", data);
        return checkoutReq;
    }

    public IntegrationScreenResponse startCheckout(Map<String, Object> request) {
        Map<String, Object> req = request != null ? new LinkedHashMap<>(request) : new LinkedHashMap<>();
        String catalogCode = str(req.get("catalogServiceCode"));
        if (!StringUtils.hasText(catalogCode)) {
            catalogCode = "CHECKOUT";
        }

        CatalogProductResolver.ResolvedCatalogProduct catalog = null;
        try {
            catalog = catalogProductResolver.resolve(catalogCode, null);
        } catch (BusinessException ignored) {
            // Compat : checkout historique sans ligne catalogue
        }

        CatalogGatewaySettings gateway = catalog != null
                ? CatalogGatewaySettings.fromConfiguration(catalog.configuration())
                : CatalogGatewaySettings.fromConfiguration(Map.of());
        CatalogJourneySettings journey = catalog != null
                ? CatalogJourneySettings.from(catalog.configuration())
                : CatalogJourneySettings.from(Map.of());

        Map<String, Object> data = dataMap(req);
        String requestedChannel = firstNonBlank(str(req.get("channel")), str(req.get("accessChannel")));
        AccessChannel channel = journey.resolveChannel(requestedChannel);
        String requestedAudience = firstNonBlank(
                str(req.get("audience")),
                str(req.get("productAudience")),
                str(req.get("accessAudience")));
        var audience = journey.resolveAudience(requestedAudience);
        String journeyCode = journey.resolveJourneyCode(str(req.get("journeyCode")));
        if (StringUtils.hasText(journeyCode)) {
            data.put("journeyCode", journeyCode);
        }
        if (channel != null) {
            data.put("accessChannel", channel.name());
            data.put("channel", channel.name());
        }
        if (audience != null) {
            data.put("productAudience", audience.name());
            data.put("audience", audience.name());
        }
        data.put("catalogProductCode", catalogCode);
        data.put("catalogServiceCode", catalogCode);

        Map<String, Object> body = new LinkedHashMap<>();
        String provider = str(req.get("provider"));
        if (!StringUtils.hasText(provider)) {
            Map<String, Object> host = gateway.hostFlow();
            Object hostCode = host != null ? host.get("providerCode") : null;
            provider = hostCode != null && StringUtils.hasText(String.valueOf(hostCode))
                    ? String.valueOf(hostCode).trim()
                    : "CHECKOUT";
        }
        body.put("provider", provider);

        Object amount = req.get("amount");
        if (amount != null) {
            body.put("amount", toDecimal(amount));
        }
        body.put("currency", req.getOrDefault("currency", gateway.defaultCurrency("XOF")));
        putIfAbsent(body, "phoneNumber", req.get("phoneNumber"));
        putIfAbsent(body, "customerReference", req.get("customerReference"));
        Object merchant = req.get("merchantReference");
        if (merchant == null || String.valueOf(merchant).isBlank()) {
            merchant = "EXT-" + UUID.randomUUID().toString().substring(0, 8);
        }
        body.put("merchantReference", merchant);

        putIfAbsent(data, "payProvider", req.get("payProvider"));
        putIfAbsent(data, "payProviderCode", firstNonBlank(
                str(req.get("payProviderCode")), str(req.get("payProvider"))));
        for (String field : gateway.copyRequestFields()) {
            putIfAbsent(data, field, req.get(field));
        }
        for (String field : List.of(
                "destAccount", "compteCredit", "tontineId", "tontineName",
                "inviteCode", "round", "beneficiaryName")) {
            putIfAbsent(data, field, req.get(field));
        }
        body.put("data", data);

        return create(body, ServiceTypeCode.PAYEMENT, catalogCode, str(req.get("idempotencyKey")));
    }

    public IntegrationScreenResponse startBillPayment(Map<String, Object> request) {
        Map<String, Object> data = dataMap(request);
        Object explicitPay = request.get("payProvider");
        if (explicitPay == null) {
            explicitPay = request.get("payProviderCode");
        }
        putIfAbsent(data, "payProvider", explicitPay);

        Map<String, Object> body = new LinkedHashMap<>();
        String provider = str(request.get("provider"));
        if (!StringUtils.hasText(provider)) {
            provider = "BILL_PAYMENT";
        } else if (explicitPay == null && !"BILL_PAYMENT".equalsIgnoreCase(provider)) {
            putIfAbsent(data, "payProvider", provider);
        }
        body.put("provider", provider);
        Object amount = request.get("amount");
        if (amount != null) {
            body.put("amount", toDecimal(amount));
        }
        body.put("currency", request.getOrDefault("currency", "XOF"));
        putIfAbsent(body, "phoneNumber", request.get("phoneNumber"));
        putIfAbsent(body, "customerReference", request.get("customerReference"));
        Object merchant = request.get("merchantReference");
        if (merchant == null || String.valueOf(merchant).isBlank()) {
            merchant = "EXT-" + UUID.randomUUID().toString().substring(0, 8);
        }
        body.put("merchantReference", merchant);

        putIfAbsent(data, "billerCode", request.get("billerCode"));
        putIfAbsent(data, "productCode", request.get("productCode"));
        putIfAbsent(data, "billIds", request.get("billIds"));
        putIfAbsent(data, "billReference", request.get("billReference"));
        putIfAbsent(data, "subscriberReference", request.get("subscriberReference"));
        putIfAbsent(data, "meterNumber", request.get("meterNumber"));
        putIfAbsent(data, "amountDue", request.get("amountDue"));
        putIfAbsent(data, "billLines", request.get("billLines"));

        // Canal / journey catalogue — comme startProduct générique (PaymentEngine lit data.*).
        String requestedChannel = firstNonBlank(str(request.get("accessChannel")), str(request.get("channel")));
        if (StringUtils.hasText(requestedChannel)) {
            putIfAbsent(data, "accessChannel", requestedChannel);
            putIfAbsent(data, "channel", requestedChannel);
        }
        // Ne pas traiter JourneyCode produit (OPEN_BILLS/PREPAID/GET_INFOS) comme journey catalogue.
        String requestedJourney = str(request.get("journeyCode"));
        if (StringUtils.hasText(requestedJourney) && !isBillInquiryJourneyCode(requestedJourney)) {
            putIfAbsent(data, "journeyCode", requestedJourney);
        }

        for (Map.Entry<String, Object> e : request.entrySet()) {
            String key = e.getKey();
            if (key == null || isReservedBillStartKey(key)) {
                continue;
            }
            putIfAbsent(data, key, e.getValue());
        }
        body.put("data", data);
        return create(body, ServiceTypeCode.PAIEMENT_FACTURE, "BILL_PAYMENT", str(request.get("idempotencyKey")));
    }

    public IntegrationScreenResponse startAccountTransfer(Map<String, Object> request) {
        return startProduct("ACCOUNT_TRANSFER", request);
    }

    public IntegrationScreenResponse startRtgs(Map<String, Object> request) {
        return startProduct("RTGS", request);
    }

    public IntegrationScreenResponse startSwift(Map<String, Object> request) {
        return startProduct("SWIFT", request);
    }

    public IntegrationScreenResponse getScreen(String paymentId) {
        return IntegrationScreenMapper.fromPayment(paymentEngine.get(paymentId));
    }

    public IntegrationScreenResponse submitAction(String paymentId, String action, Map<String, Object> data) {
        String act = StringUtils.hasText(action) ? action.trim() : "SUBMIT_FORM";
        Map<String, Object> payload = data != null ? data : Map.of();
        try {
            return IntegrationScreenMapper.fromPayment(paymentEngine.submitAction(paymentId, act, payload));
        } catch (RuntimeException ex) {
            BusinessException hold = otpRequired(ex);
            if (hold == null) {
                throw ex;
            }
            return otpScreen(paymentId, hold.getMessage());
        }
    }

    private static BusinessException otpRequired(Throwable error) {
        Throwable current = error;
        while (current != null) {
            if (current instanceof BusinessException business && "OTP_REQUIRED".equals(business.getCode())) {
                return business;
            }
            current = current.getCause();
        }
        return null;
    }

    private static IntegrationScreenResponse otpScreen(String paymentId, String message) {
        IntegrationScreenResponse screen = new IntegrationScreenResponse();
        screen.setType("ENTER_OTP");
        screen.setTitle("Code OTP");
        screen.setMessage(message != null && !message.isBlank()
                ? message
                : "Saisissez le code OTP reçu");
        screen.setOtpLength(6);
        screen.setActions(List.of(Map.of(
                "id", "SUBMIT_OTP",
                "label", "Valider",
                "style", "primary"
        )));
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("paymentId", paymentId);
        meta.put("status", "OTP_REQUIRED");
        screen.setMeta(meta);
        return screen;
    }

    public Object listProvidersForProduct(String product) {
        String raw = product == null ? "checkout" : product.trim();
        String code = normalizeProductCode(raw);

        // Compat path aliases historiques
        if ("ACCOUNT_TRANSFER".equals(code) || "account-transfer".equalsIgnoreCase(raw)) {
            try {
                CatalogProductResolver.ResolvedCatalogProduct catalog =
                        catalogProductResolver.resolve("ACCOUNT_TRANSFER", null);
                if (catalog != null) {
                    CatalogGatewaySettings gw = CatalogGatewaySettings.fromConfiguration(catalog.configuration());
                    if ("TRANSFER_EMITTERS".equals(gw.providersSource())) {
                        return listTransferEmitters();
                    }
                }
            } catch (BusinessException ignored) {
                // fall through
            }
            return listTransferEmitters();
        }

        try {
            CatalogProductResolver.ResolvedCatalogProduct catalog =
                    catalogProductResolver.resolve(code, null);
            if (catalog != null) {
                return listProvidersFromCatalog(catalog);
            }
        } catch (BusinessException ignored) {
            // fallback legacy
        }

        return listProvidersLegacy(raw.toLowerCase(Locale.ROOT));
    }

    private List<?> listProvidersFromCatalog(CatalogProductResolver.ResolvedCatalogProduct catalog) {
        CatalogGatewaySettings gateway = CatalogGatewaySettings.fromConfiguration(catalog.configuration());
        if ("TRANSFER_EMITTERS".equals(gateway.providersSource())) {
            return listTransferEmitters();
        }
        FseParticipantKind kind = gateway.participantKind();
        ServiceTypeCode st = gateway.serviceTypeFilter();
        if (st == null) {
            st = catalog.serviceType();
        }
        final ServiceTypeCode filterSt = st;
        return providerService.listActive().stream()
                .filter(pr -> kind == null || pr.participantKind() == kind)
                .filter(pr -> filterSt == null
                        || pr.serviceTypes() == null
                        || pr.serviceTypes().isEmpty()
                        || pr.serviceTypes().contains(filterSt))
                .toList();
    }

    private List<ProviderResponse> listProvidersLegacy(String p) {
        return switch (p) {
            case "checkout", "bill-payment" -> providerService.listActiveByServiceType(ServiceTypeCode.PAYEMENT);
            case "account-transfer" -> List.of();
            case "rtgs", "swift" -> providerService.listActive().stream()
                    .filter(pr -> pr.participantKind() == FseParticipantKind.BANQUE)
                    .filter(pr -> pr.serviceTypes() == null
                            || pr.serviceTypes().isEmpty()
                            || pr.serviceTypes().contains(ServiceTypeCode.TRANSFERT))
                    .toList();
            default -> providerService.listActive();
        };
    }

    public List<TransferParticipantResponse> listTransferEmitters() {
        return transferCatalogService.listParticipants(true).stream()
                .filter(TransferParticipantResponse::canEmit)
                .toList();
    }

    public List<TransferParticipantResponse> listTransferDestinations() {
        return transferCatalogService.listParticipants(true).stream()
                .filter(TransferParticipantResponse::canReceive)
                .toList();
    }

    public List<BillerResponse> listBillers() {
        return billerService.listActive();
    }

    public List<BillerCategoryResponse> listBillerCategories() {
        return billerService.listActiveCategories();
    }

    public BillerPageResponse searchBillers(String category, String q, int page, int size) {
        return billerService.searchActive(category, q, page, size);
    }

    /**
     * Historique des transactions gateway (lecture {@code payment_transaction}).
     * Canal retail : scopé automatiquement sur l'utilisateur JWT ({@code initiatedBy}).
     */
    public GatewayTransactionPageResponse searchTransactions(
            String customerReference,
            String initiatedBy,
            String status,
            String serviceType,
            String product,
            String provider,
            String q,
            Instant from,
            Instant to,
            int page,
            int size
    ) {
        return paymentQueryService.searchHistory(
                customerReference, initiatedBy, status, serviceType, product, provider, q, from, to, page, size);
    }

    /**
     * Composition catalogue produit ({@code configuration.presentation}) + items.
     */
    public CatalogLayoutResponse productCatalog(
            String productCode,
            String channel,
            String category,
            String q,
            int page,
            int size,
            boolean hydrate
    ) {
        return catalogPresentationService.resolve(productCode, channel, category, q, page, size, hydrate);
    }

    public CatalogLayoutResponse productCatalogLayout(String productCode, String channel) {
        return catalogPresentationService.layoutOnly(productCode, channel);
    }

    /** Inventaire des sources de données (designer RapidFrame / Formio). */
    public List<DataSourceDescriptorDto> listDataSources() {
        return dataSourceResolveService.listDescriptors();
    }

    /**
     * Résolution d'une source par code — LOCAL (services payment-2) ou OMNICANAL (gateway FSE).
     */
    public DataSourceResponse resolveDataSource(
            String code,
            String origin,
            Map<String, Object> params,
            String category,
            String q,
            int page,
            int size,
            String productCode
    ) {
        return dataSourceResolveService.resolve(code, origin, params, category, q, page, size, productCode);
    }

    public List<BillerProductResponse> listBillerProducts(UUID billerId) {
        return billerService.listProducts(billerId, true);
    }

    public List<BillerProductResponse> listBillerProducts(String billerKey) {
        return billerService.listProductsByBillerKey(billerKey, true);
    }

    public List<BillerProductResponse> listBillerProductsByCode(String billerCode) {
        return billerService.listProductsByBillerCode(billerCode, true);
    }

    public BillSearchResponse searchBills(
            String billerCode,
            String productCode,
            String subscriberReference,
            String billReference
    ) {
        if (StringUtils.hasText(billReference)) {
            return billCatalogService.searchByBillReference(billReference, billerCode, productCode);
        }
        return billCatalogService.searchBySubscriber(subscriberReference, billerCode, productCode);
    }

    public List<ApprovalDtos.InboxItem> approvalInbox() {
        return approvalQueryService.inbox();
    }

    public ApprovalDtos.Detail approvalDetail(String paymentId) {
        return approvalQueryService.detailByPaymentId(paymentId);
    }

    public IntegrationScreenResponse decideApproval(String paymentId, String action, Map<String, Object> data) {
        String act = action != null ? action.trim().toUpperCase() : "";
        if (!"APPROVE".equals(act) && !"REJECT".equals(act)) {
            throw new BusinessException("INVALID_ACTION", "action doit être APPROVE ou REJECT");
        }
        Map<String, Object> payload = data != null ? data : Map.of();
        return IntegrationScreenMapper.fromPayment(paymentEngine.submitAction(paymentId, act, payload));
    }

    private IntegrationScreenResponse create(
            Map<String, Object> body,
            ServiceTypeCode serviceType,
            String catalogServiceCode,
            String idempotencyKey
    ) {
        String provider = str(body.get("provider"));
        BigDecimal amount = toDecimal(body.get("amount"));
        String currency = str(body.getOrDefault("currency", "XOF"));
        String customerReference = str(body.get("customerReference"));
        String merchantReference = str(body.get("merchantReference"));
        String phoneNumber = str(body.get("phoneNumber"));
        @SuppressWarnings("unchecked")
        Map<String, Object> data = body.get("data") instanceof Map<?, ?> m
                ? (Map<String, Object>) m
                : Map.of();

        PaymentResponse payment = paymentEngine.initiate(
                provider,
                serviceType,
                catalogServiceCode,
                null,
                amount,
                currency,
                customerReference,
                merchantReference,
                phoneNumber,
                data,
                idempotencyKey
        );
        return IntegrationScreenMapper.fromPayment(payment);
    }

    private Map<String, Object> basePaymentBody(Map<String, Object> request) {
        Map<String, Object> body = new LinkedHashMap<>();
        Object provider = request.get("provider");
        if (provider == null || String.valueOf(provider).isBlank()) {
            throw new BusinessException("PROVIDER_REQUIRED", "provider is required", HttpStatus.BAD_REQUEST);
        }
        body.put("provider", provider);
        Object amount = request.get("amount");
        if (amount != null) {
            body.put("amount", toDecimal(amount));
        }
        body.put("currency", request.getOrDefault("currency", "XOF"));
        putIfAbsent(body, "phoneNumber", request.get("phoneNumber"));
        putIfAbsent(body, "customerReference", request.get("customerReference"));
        Object merchant = request.get("merchantReference");
        if (merchant == null || String.valueOf(merchant).isBlank()) {
            merchant = "EXT-" + UUID.randomUUID().toString().substring(0, 8);
        }
        body.put("merchantReference", merchant);
        return body;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> dataMap(Map<String, Object> request) {
        Object data = request.get("data");
        if (data instanceof Map<?, ?> m) {
            return new LinkedHashMap<>((Map<String, Object>) m);
        }
        return new LinkedHashMap<>();
    }

    private static void putIfAbsent(Map<String, Object> map, String key, Object value) {
        if (value != null && !(value instanceof String s && s.isBlank())) {
            map.putIfAbsent(key, value);
        }
    }

    private static boolean isBillInquiryJourneyCode(String raw) {
        if (!StringUtils.hasText(raw)) {
            return false;
        }
        String n = raw.trim().toUpperCase(Locale.ROOT);
        return "OPEN_BILLS".equals(n) || "GET_INFOS".equals(n) || "PREPAID".equals(n)
                || "LIST".equals(n) || "INFOS".equals(n) || "DIRECT".equals(n);
    }

    private static boolean isReservedBillStartKey(String key) {
        return isReservedStartKey(key)
                || switch (key) {
            case "billerCode", "productCode", "billIds", "billReference",
                    "subscriberReference", "meterNumber", "amountDue", "billLines" -> true;
            default -> false;
        };
    }

    private static boolean isReservedStartKey(String key) {
        return switch (key) {
            case "provider", "destinationProvider", "amount", "currency", "phoneNumber",
                    "customerReference", "merchantReference", "idempotencyKey", "serviceType",
                    "catalogServiceCode", "data", "roleAssignments", "payProvider", "payProviderCode",
                    "channel", "accessChannel", "journeyCode",
                    "audience", "productAudience", "accessAudience" -> true;
            default -> false;
        };
    }

    static String normalizeProductCode(String raw) {
        if (raw == null) {
            return "";
        }
        String n = raw.trim().toUpperCase(Locale.ROOT).replace(' ', '_').replace('-', '_');
        return switch (n) {
            case "BILL_PAYMENT", "BILLPAYMENT", "PAIEMENT_FACTURE" -> "BILL_PAYMENT";
            case "ACCOUNT_TRANSFER", "ACCOUNTTRANSFER" -> "ACCOUNT_TRANSFER";
            case "CHECKOUT", "PAYMENT", "PAYEMENT" -> "CHECKOUT";
            case "COFFRE", "VAULT", "SAFE" -> "COFFRE";
            case "TONTINE" -> "TONTINE";
            case "CASH_OUT", "CASHOUT" -> "CASH_OUT";
            case "CASH_IN", "CASHIN" -> "CASH_IN";
            case "LINK", "REGISTER", "ACCOUNT_LINK", "ACCOUNTLINK" -> "LINK";
            case "UNLINK", "ACCOUNT_UNLINK", "ACCOUNTUNLINK" -> "UNLINK";
            default -> n;
        };
    }

    private static BigDecimal toDecimal(Object amount) {
        if (amount == null) {
            return null;
        }
        if (amount instanceof BigDecimal bd) {
            return bd;
        }
        if (amount instanceof Number n) {
            return BigDecimal.valueOf(n.doubleValue());
        }
        return new BigDecimal(String.valueOf(amount));
    }

    private static String str(Object v) {
        if (v == null) {
            return null;
        }
        String s = String.valueOf(v).trim();
        return s.isEmpty() || "null".equals(s) ? null : s;
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
