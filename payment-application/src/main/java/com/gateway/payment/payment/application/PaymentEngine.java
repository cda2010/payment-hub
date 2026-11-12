package com.gateway.payment.payment.application;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.payment.billing.domain.BillInquiryMode;
import com.gateway.payment.billing.domain.BillerProduct;
import com.gateway.payment.billing.domain.JourneyCode;
import com.gateway.payment.billing.service.BillAmountPolicyService;
import com.gateway.payment.billing.service.BillerService;
import com.gateway.payment.bpm.BpmCheckoutNextActionService;
import com.gateway.payment.bpm.CatalogProviderInvoker;
import com.gateway.payment.bpm.FlowablePaymentRuntime;
import com.gateway.payment.bpm.GatewayOrchestrationProperties;
import com.gateway.payment.bpm.ProviderResponseContextMerger;
import com.gateway.payment.catalog.casting.CatalogProductCasting;
import com.gateway.payment.catalog.casting.CatalogProductCastingService;
import com.gateway.payment.catalog.casting.CatalogProductRoleCode;
import com.gateway.payment.catalog.journey.CatalogAmountPolicy;
import com.gateway.payment.catalog.journey.CatalogJourneySettings;
import com.gateway.payment.catalog.service.CatalogProductResolver;
import com.gateway.payment.common.config.GatewayPerformanceProperties;
import com.gateway.payment.common.domain.AccessChannel;
import com.gateway.payment.common.domain.NextActionType;
import com.gateway.payment.common.domain.PaymentStatus;
import com.gateway.payment.common.domain.PaymentStatusTransitionGuard;
import com.gateway.payment.common.domain.ProductAudience;
import com.gateway.payment.common.domain.ServiceTypeCode;
import com.gateway.payment.common.domain.StepType;
import com.gateway.payment.common.exception.BusinessException;
import com.gateway.payment.common.util.SensitiveDataMasker;
import com.gateway.payment.configuration.domain.FseParticipantKind;
import com.gateway.payment.configuration.domain.PaymentFlow;
import com.gateway.payment.omnicanal.external.RuntimeProviderResolver;
import com.gateway.payment.configuration.domain.PaymentFlowStep;
import com.gateway.payment.configuration.domain.PaymentProvider;
import com.gateway.payment.configuration.domain.ProviderOtpConfig;
import com.gateway.payment.configuration.domain.ProviderStatusMapping;
import com.gateway.payment.configuration.repository.PaymentFlowRepository;
import com.gateway.payment.configuration.repository.PaymentProviderRepository;
import com.gateway.payment.configuration.repository.ProviderOtpConfigRepository;
import com.gateway.payment.configuration.repository.ProviderStatusMappingRepository;
import com.gateway.payment.payment.api.dto.FormFieldDto;
import com.gateway.payment.payment.api.dto.NextActionDto;
import com.gateway.payment.payment.api.dto.PaymentResponse;
import com.gateway.payment.payment.domain.PaymentEvent;
import com.gateway.payment.payment.domain.PaymentTransaction;
import com.gateway.payment.payment.repository.PaymentEventRepository;
import com.gateway.payment.payment.repository.PaymentTransactionRepository;
import com.gateway.payment.events.DomainEventPublisher;
import com.gateway.payment.events.DomainEventTypes;
import com.gateway.payment.provider.ProviderExecutionResult;
import com.gateway.payment.security.TenantContext;
import com.gateway.payment.settlement.PaymentSettlementService;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class PaymentEngine {

    private static final AtomicLong SEQ = new AtomicLong(1);

    private final PaymentTransactionRepository transactionRepository;
    private final PaymentEventRepository eventRepository;
    private final PaymentProviderRepository providerRepository;
    private final RuntimeProviderResolver runtimeProviders;
    private final PaymentFlowRepository flowRepository;
    private final ProviderOtpConfigRepository otpConfigRepository;
    private final ProviderStatusMappingRepository statusMappingRepository;
    private final ObjectMapper objectMapper;
    private final TemplateMapper templateMapper;
    private final BillAmountPolicyService billAmountPolicyService;
    private final BillerService billerService;
    private final GatewayOrchestrationProperties orchestrationProperties;
    private final FlowablePaymentRuntime flowablePaymentRuntime;
    private final BpmCheckoutNextActionService bpmCheckoutNextActionService;
    private final CatalogProductResolver catalogProductResolver;
    private final CatalogProductCastingService castingService;
    private final PaymentEventAuditWriter eventAuditWriter;
    private final GatewayPerformanceProperties performanceProperties;
    private final ProviderResponseContextMerger responseContextMerger;
    private final CatalogProviderInvoker catalogProviderInvoker;
    private final DomainEventPublisher domainEvents;
    private final PaymentSettlementService settlement;
    private final org.springframework.beans.factory.ObjectProvider<
            com.gateway.payment.approval.repository.ApprovalInstanceRepository> approvalInstances;
    private final org.springframework.beans.factory.ObjectProvider<
            com.gateway.payment.inbox.PartyActionProducer> partyActionProducer;
    private final org.springframework.beans.factory.ObjectProvider<
            com.gateway.payment.spend.SpendRecorder> spendRecorder;
    private final HistoryLabelFormatter historyLabelFormatter;
    private final com.gateway.payment.catalog.i18n.CatalogConfigurationLocalizer configurationLocalizer;

    public PaymentEngine(
            PaymentTransactionRepository transactionRepository,
            PaymentEventRepository eventRepository,
            PaymentProviderRepository providerRepository,
            PaymentFlowRepository flowRepository,
            ProviderOtpConfigRepository otpConfigRepository,
            ProviderStatusMappingRepository statusMappingRepository,
            ObjectMapper objectMapper,
            TemplateMapper templateMapper,
            BillAmountPolicyService billAmountPolicyService,
            BillerService billerService,
            GatewayOrchestrationProperties orchestrationProperties,
            FlowablePaymentRuntime flowablePaymentRuntime,
            BpmCheckoutNextActionService bpmCheckoutNextActionService,
            CatalogProductResolver catalogProductResolver,
            CatalogProductCastingService castingService,
            PaymentEventAuditWriter eventAuditWriter,
            GatewayPerformanceProperties performanceProperties,
            ProviderResponseContextMerger responseContextMerger,
            CatalogProviderInvoker catalogProviderInvoker,
            DomainEventPublisher domainEvents,
            PaymentSettlementService settlement,
            org.springframework.beans.factory.ObjectProvider<
                    com.gateway.payment.approval.repository.ApprovalInstanceRepository> approvalInstances,
            org.springframework.beans.factory.ObjectProvider<
                    com.gateway.payment.inbox.PartyActionProducer> partyActionProducer,
            org.springframework.beans.factory.ObjectProvider<
                    com.gateway.payment.spend.SpendRecorder> spendRecorder,
            HistoryLabelFormatter historyLabelFormatter,
            com.gateway.payment.catalog.i18n.CatalogConfigurationLocalizer configurationLocalizer,
            RuntimeProviderResolver runtimeProviders
    ) {
        this.transactionRepository = transactionRepository;
        this.eventRepository = eventRepository;
        this.providerRepository = providerRepository;
        this.runtimeProviders = runtimeProviders;
        this.flowRepository = flowRepository;
        this.otpConfigRepository = otpConfigRepository;
        this.statusMappingRepository = statusMappingRepository;
        this.objectMapper = objectMapper;
        this.templateMapper = templateMapper;
        this.billAmountPolicyService = billAmountPolicyService;
        this.billerService = billerService;
        this.orchestrationProperties = orchestrationProperties;
        this.flowablePaymentRuntime = flowablePaymentRuntime;
        this.bpmCheckoutNextActionService = bpmCheckoutNextActionService;
        this.catalogProductResolver = catalogProductResolver;
        this.castingService = castingService;
        this.eventAuditWriter = eventAuditWriter;
        this.performanceProperties = performanceProperties;
        this.responseContextMerger = responseContextMerger;
        this.catalogProviderInvoker = catalogProviderInvoker;
        this.domainEvents = domainEvents;
        this.settlement = settlement;
        this.approvalInstances = approvalInstances;
        this.partyActionProducer = partyActionProducer;
        this.spendRecorder = spendRecorder;
        this.historyLabelFormatter = historyLabelFormatter;
        this.configurationLocalizer = configurationLocalizer;
    }

    @Transactional
    public PaymentResponse initiate(
            String providerCode,
            ServiceTypeCode serviceType,
            BigDecimal amount,
            String currency,
            String customerReference,
            String merchantReference,
            String phoneNumber,
            Map<String, Object> data,
            String idempotencyKey
    ) {
        return initiate(providerCode, serviceType, null, null, amount, currency,
                customerReference, merchantReference, phoneNumber, data, idempotencyKey);
    }

    /**
     * Initialise un paiement.
     * <ul>
     *   <li>Sans catalogue : parcours legacy {@code provider + serviceType → PaymentFlow}.</li>
     *   <li>Avec {@code catalogServiceCode} / version : résout le produit PUBLISHED,
     *       dérive le {@link ServiceTypeCode}, fige le snapshot, préfère {@code workflow_key}.</li>
     * </ul>
     */
    @Transactional
    public PaymentResponse initiate(
            String providerCode,
            ServiceTypeCode serviceType,
            String catalogServiceCode,
            UUID catalogServiceVersionId,
            BigDecimal amount,
            String currency,
            String customerReference,
            String merchantReference,
            String phoneNumber,
            Map<String, Object> data,
            String idempotencyKey
    ) {
        CatalogProductResolver.ResolvedCatalogProduct catalog =
                catalogProductResolver.resolve(catalogServiceCode, catalogServiceVersionId);

        ServiceTypeCode resolvedServiceType = resolveServiceType(serviceType, catalog);

        // Facture : montant souvent connu après LOOKUP/confirm — autorisé à 0 au start.
        // Idem produits où le montant arrive mid-flow (LOOKUP remittance, CAPTURE_AMOUNT…).
        if (resolvedServiceType.isBillPayment()) {
            if (amount == null) {
                amount = BigDecimal.ZERO;
            }
            amount = billAmountPolicyService.validateAndResolveTotal(data, amount);
        } else if (resolvedServiceType.requiresPositiveAmount()) {
            boolean amountDeferred = CatalogAmountPolicy.amountDeferredAtStart(
                    catalog != null ? catalog.configuration() : null);
            if (!amountDeferred && (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)) {
                throw new BusinessException("AMOUNT_REQUIRED", "Montant invalide");
            }
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                amount = BigDecimal.ZERO;
            }
        } else if (amount == null) {
            amount = BigDecimal.ZERO;
        }
        if (resolvedServiceType.isTransfer() && data == null) {
            throw new BusinessException("TRANSFER_DATA_REQUIRED", "Données transfert requises");
        }

        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            var existing = transactionRepository.findByIdempotencyKey(idempotencyKey);
            if (existing.isPresent()) {
                return toResponse(existing.get(), resolveNextAction(existing.get()));
            }
        }

        // Facture composée : rail FACTURIER (catalogue biller/produit) + rail wallet OTP.
        // Le provider sélectionné côté UI est le wallet, sauf s'il s'agit déjà d'un FACTURIER.
        // Option B : defaults catalogue (defaultBillProvider / defaultPayProvider / defaultProvider).
        // P1–P3 : roleAssignments (PAYER / BILLER / SOURCE…).
        CatalogProductCasting casting = catalog != null
                ? castingService.parse(catalog.configuration())
                : null;
        Map<String, Object> castingData = data != null ? new HashMap<>(data) : new HashMap<>();
        seedRoleAssignmentsFromProvider(castingData, casting, providerCode, resolvedServiceType);
        Map<String, String> roleAssignments = castingService.resolveRoleAssignments(casting, castingData);

        String catalogDefaultBill = catalog != null ? catalog.defaultBillProvider() : null;
        String catalogDefaultPay = catalog != null
                ? firstNonBlank(catalog.defaultPayProvider(), catalog.defaultProvider())
                : null;

        String billProviderCode = null;
        String payProviderCode = providerCode;
        if (resolvedServiceType.isBillPayment()) {
            String fromBillOverride = dataString(data, "billProvider", "billProviderCode");
            String fromPay = dataString(data, "payProvider", "payProviderCode");
            String fromCatalog = resolveBillProviderFromCatalog(data);
            String fromBillerRole = roleAssignments.get(CatalogProductRoleCode.BILLER.name());
            String fromPayerRole = roleAssignments.get(CatalogProductRoleCode.PAYER.name());
            boolean providerIsFacturier = isFacturierProvider(providerCode);
            boolean providerBlank = providerCode == null || providerCode.isBlank();

            if (providerBlank || providerIsFacturier) {
                // Collecte facture d'abord : wallet choisi en mid-flow (selectPayProvider).
                billProviderCode = firstNonBlank(
                        fromBillOverride, fromCatalog, fromBillerRole, catalogDefaultBill, providerCode);
                payProviderCode = firstNonBlank(fromPay, fromPayerRole, catalogDefaultPay);
            } else {
                // Rétrocompat : provider = wallet dès le start.
                payProviderCode = firstNonBlank(fromPay, fromPayerRole, providerCode, catalogDefaultPay);
                billProviderCode = firstNonBlank(fromBillOverride, fromCatalog, fromBillerRole, catalogDefaultBill);
            }
            if (billProviderCode == null || billProviderCode.isBlank()) {
                throw new BusinessException("BILL_PROVIDER_REQUIRED",
                        "Aucun fournisseur FACTURIER rattaché au facturier/produit "
                                + "(billerCode/productCode), ni billProvider / rôle BILLER / defaultBillProvider");
            }
            // payProvider optionnel ici — imposé au step selectPayProvider si absent.
        }

        final String resolvedBillProviderCode = billProviderCode;
        final String resolvedPayProviderCode = payProviderCode;

        // SOURCE explicite (client) vs hôte technique du BPMN catalogue.
        String explicitSource = firstNonBlank(
                providerCode,
                roleAssignments.get(CatalogProductRoleCode.SOURCE.name()));
        String explicitDestination = firstNonBlank(
                dataString(data, "destinationProvider", "destProvider", "destRail"),
                roleAssignments.get(CatalogProductRoleCode.DESTINATION.name()));

        String primaryProviderCode;
        PaymentFlow transferHostFlow = null;
        if (resolvedServiceType.isBillPayment()) {
            primaryProviderCode = resolvedBillProviderCode;
        } else if (resolvedServiceType.isTransfer()) {
            primaryProviderCode = firstNonBlank(
                    explicitSource,
                    catalog != null ? catalog.defaultProvider() : null,
                    catalog != null ? catalog.defaultPayProvider() : null);
            if (primaryProviderCode == null || primaryProviderCode.isBlank()) {
                String wf = catalog != null && StringUtils.hasText(catalog.workflowKey())
                        ? catalog.workflowKey().trim()
                        : "accountTransfer";
                transferHostFlow = flowRepository.findFirstByBpmProcessKeyAndActifTrue(wf).orElse(null);
                if (transferHostFlow != null && transferHostFlow.getProvider() != null) {
                    primaryProviderCode = transferHostFlow.getProvider().getCode();
                }
            }
        } else if (resolvedServiceType == ServiceTypeCode.LINK
                || resolvedServiceType == ServiceTypeCode.UNLINK) {
            // Hôte technique (provider LINK) — le wallet SOURCE est choisi mid-flow.
            // Ne pas prendre le participant default (WAVE) comme primary au start.
            String wf = catalog != null && StringUtils.hasText(catalog.workflowKey())
                    ? catalog.workflowKey().trim()
                    : resolvedServiceType.defaultBpmProcessKey();
            transferHostFlow = flowRepository.findFirstByBpmProcessKeyAndActifTrue(wf).orElse(null);
            primaryProviderCode = firstNonBlank(
                    providerCode,
                    catalog != null ? catalog.defaultProvider() : null,
                    transferHostFlow != null && transferHostFlow.getProvider() != null
                            ? transferHostFlow.getProvider().getCode()
                            : null);
        } else {
            primaryProviderCode = firstNonBlank(
                    providerCode,
                    roleAssignments.get(CatalogProductRoleCode.SOURCE.name()),
                    catalog != null ? catalog.defaultProvider() : null,
                    catalog != null ? catalog.defaultPayProvider() : null);
        }

        if (primaryProviderCode == null || primaryProviderCode.isBlank()) {
            throw new BusinessException("PROVIDER_REQUIRED",
                    resolvedServiceType.isTransfer()
                            ? "Aucun parcours TRANSFERT déployé (workflow_key catalogue) — seed providers / flow BPMN"
                            : "provider requis, ou defaultProvider / defaultPayProvider sur le produit catalogue");
        }
        final String resolvedPrimaryProviderCode = primaryProviderCode.trim();

        PaymentProvider provider = runtimeProviders.require(resolvedPrimaryProviderCode);
        if (!provider.isActif()) {
            throw new BusinessException("PROVIDER_INACTIVE", "Fournisseur inactif");
        }
        boolean supportsService = provider.getServiceTypes() == null || provider.getServiceTypes().isEmpty()
                || provider.getServiceTypes().stream().anyMatch(st -> st.getCode() == resolvedServiceType);
        if (!supportsService && transferHostFlow == null) {
            throw new BusinessException("SERVICE_TYPE_UNSUPPORTED",
                    "Le fournisseur " + resolvedPrimaryProviderCode
                            + " ne supporte pas le type de service " + resolvedServiceType);
        }

        PaymentProvider payProvider = null;
        if (resolvedServiceType.isBillPayment()
                && resolvedPayProviderCode != null
                && !resolvedPayProviderCode.isBlank()) {
            if (resolvedPayProviderCode.equalsIgnoreCase(resolvedBillProviderCode)) {
                throw new BusinessException("PAY_PROVIDER_INVALID",
                        "Le moyen de paiement wallet doit être distinct du rail facture");
            }
            payProvider = runtimeProviders.find(resolvedPayProviderCode)
                    .orElseThrow(() -> new BusinessException("PAY_PROVIDER_NOT_FOUND",
                            "Moyen de paiement introuvable: " + resolvedPayProviderCode, HttpStatus.NOT_FOUND));
            if (!payProvider.isActif()) {
                throw new BusinessException("PAY_PROVIDER_INACTIVE", "Moyen de paiement inactif");
            }
        }

        if (resolvedServiceType.isBillPayment()) {
            roleAssignments.putIfAbsent(CatalogProductRoleCode.BILLER.name(), provider.getCode());
            if (payProvider != null) {
                roleAssignments.putIfAbsent(CatalogProductRoleCode.PAYER.name(), payProvider.getCode());
            }
        } else if (resolvedServiceType.isTransfer()) {
            // Ne pas figer SOURCE sur l'hôte technique BPMN — choisi en mid-flow (sourceRail).
            if (explicitSource != null && !explicitSource.isBlank()) {
                roleAssignments.putIfAbsent(CatalogProductRoleCode.SOURCE.name(), explicitSource.trim());
            }
            if (explicitDestination != null && !explicitDestination.isBlank()) {
                roleAssignments.putIfAbsent(CatalogProductRoleCode.DESTINATION.name(), explicitDestination.trim());
            }
        } else if (resolvedServiceType == ServiceTypeCode.LINK
                || resolvedServiceType == ServiceTypeCode.UNLINK) {
            // Wallet SOURCE choisi mid-flow — ne pas figer l'hôte technique.
            if (providerCode != null && !providerCode.isBlank()) {
                roleAssignments.putIfAbsent(CatalogProductRoleCode.SOURCE.name(), providerCode.trim());
            }
        } else {
            roleAssignments.putIfAbsent(CatalogProductRoleCode.SOURCE.name(), provider.getCode());
        }

        final PaymentFlow flow;
        if (transferHostFlow != null
                && (explicitSource == null || explicitSource.isBlank())) {
            flow = preferCatalogWorkflow(transferHostFlow, provider, catalog);
        } else {
            flow = preferCatalogWorkflow(
                    resolveFlowForServiceType(provider, resolvedServiceType, catalog),
                    provider,
                    catalog);
        }

        String paymentId = generatePaymentId();
        Map<String, Object> context = new HashMap<>();
        if (data != null) {
            context.putAll(data);
        }
        context.put("amount", amount != null ? amount : BigDecimal.ZERO);
        context.put("currency", currency != null ? currency : "XOF");
        context.put("serviceType", resolvedServiceType.name());
        if (phoneNumber != null) {
            context.put("phoneNumber", phoneNumber);
        }
        context.put("flowId", flow.getId().toString());
        String initiator = currentUsername();
        if (initiator != null) {
            context.put(com.gateway.payment.bpm.dmn.DmnTransferApprovalConfig.CTX_INITIATED_BY, initiator);
        }
        if (flow.getBpmProcessKey() != null) {
            context.put("bpmProcessKey", flow.getBpmProcessKey());
            context.put("flowCode", flow.getCode());
            context.put("flowNom", flow.getNom());
        }
        if (catalog != null) {
            context.put("catalogServiceCode", catalog.productCode());
            context.put("catalogServiceVersionId", catalog.version().getId().toString());
            context.put("catalogServiceVersion", catalog.version().getVersion());
            if (catalog.workflowKey() != null) {
                context.put("catalogWorkflowKey", catalog.workflowKey());
            }
            if (catalog.configuration() != null && !catalog.configuration().isEmpty()) {
                CatalogJourneySettings journeySettings = CatalogJourneySettings.from(catalog.configuration());
                String requestedChannel = firstNonBlank(
                        dataString(data, "accessChannel", "channel"),
                        stringFromMap(castingData, "accessChannel"),
                        stringFromMap(castingData, "channel"));
                String requestedJourney = firstNonBlank(
                        dataString(data, "journeyCode"),
                        stringFromMap(castingData, "journeyCode"));
                AccessChannel channel = null;
                try {
                    channel = journeySettings.resolveChannel(requestedChannel);
                } catch (BusinessException ex) {
                    // Si le produit n'a pas de politique channel, resolveChannel peut renvoyer null ;
                    // si politique stricte, on propage.
                    if (journeySettings.hasChannelPolicy()) {
                        throw ex;
                    }
                }
                String requestedAudience = firstNonBlank(
                        dataString(data, "productAudience", "audience", "accessAudience"),
                        stringFromMap(castingData, "productAudience"),
                        stringFromMap(castingData, "audience"));
                ProductAudience audience = null;
                try {
                    audience = journeySettings.resolveAudience(requestedAudience);
                } catch (BusinessException ex) {
                    if (journeySettings.hasAudiencePolicy()) {
                        throw ex;
                    }
                }
                String resolvedJourney = journeySettings.resolveJourneyCode(requestedJourney);
                Map<String, Object> effectiveCfg = journeySettings.effectiveConfiguration(channel);
                context.put("catalogConfiguration", effectiveCfg);
                if (resolvedJourney != null) {
                    context.put("journeyCode", resolvedJourney);
                }
                if (channel != null) {
                    context.put("accessChannel", channel.name());
                    context.put("channel", channel.name());
                }
                if (audience != null) {
                    context.put("productAudience", audience.name());
                    context.put("audience", audience.name());
                }
                // Flag catalogue : false = étapes après CONFIRM (NOTIFY, PAY_BILL, …)
                if (!context.containsKey("terminalOnConfirm")) {
                    Object toc = effectiveCfg.get("terminalOnConfirm");
                    if (toc == null) {
                        toc = effectiveCfg.get("terminal_on_confirm");
                    }
                    if (toc != null) {
                        if (toc instanceof Boolean b) {
                            context.put("terminalOnConfirm", b);
                        } else if ("false".equalsIgnoreCase(String.valueOf(toc).trim())
                                || "0".equals(String.valueOf(toc).trim())) {
                            context.put("terminalOnConfirm", false);
                        } else if ("true".equalsIgnoreCase(String.valueOf(toc).trim())
                                || "1".equals(String.valueOf(toc).trim())) {
                            context.put("terminalOnConfirm", true);
                        }
                    }
                }
            }
            if (!roleAssignments.isEmpty()) {
                context.put("roleAssignments", roleAssignments);
            }
        }
        if (resolvedServiceType.isBillPayment()) {
            context.put("billProviderCode", provider.getCode());
            context.put("terminalOnConfirm", false);
            // Défaut callActivity ; écrasé quand le wallet est choisi (start ou selectPayProvider).
            context.putIfAbsent("walletCheckoutProcessKey", "providerOtpCheckout");
            if (payProvider != null) {
                context.put("payProviderCode", payProvider.getCode());
                resolveWalletCheckoutProcessKey(payProvider).ifPresent(k ->
                        context.put("walletCheckoutProcessKey", k));
            }
            // Prefill collect facture depuis l'intent catalogue
            if (context.get("billReference") == null && context.get("billReferences") instanceof java.util.List<?> refs
                    && !refs.isEmpty() && refs.get(0) != null) {
                context.put("billReference", String.valueOf(refs.get(0)));
            }
            if (context.get("billerCode") == null && context.get("billers") instanceof java.util.List<?> billers
                    && !billers.isEmpty() && billers.get(0) != null) {
                context.put("billerCode", String.valueOf(billers.get(0)));
            }
            // Alias meterNumber/… → billReference : IU LOOKUP_BILL + ParametreUtilisateur (BILL_REF)
            // via LegCompiler sur le chemin Omnicanal — plus de mapping Java.
            applyBillInquiryMode(context, data);
        }

        String initialStep;
        if (useFlowable(flow)) {
            initialStep = catalog != null && catalog.workflowKey() != null
                    ? catalog.workflowKey()
                    : flow.getBpmProcessKey();
        } else {
            PaymentFlowStep start = flow.getSteps().stream()
                    .filter(s -> s.getStepType() == StepType.START || "START".equalsIgnoreCase(s.getCode()))
                    .findFirst()
                    .orElseGet(() -> flow.getSteps().stream().findFirst()
                            .orElseThrow(() -> new BusinessException("FLOW_EMPTY", "Parcours sans étapes")));
            initialStep = start.getCode();
        }

        PaymentTransaction tx = PaymentTransaction.builder()
                .paymentId(paymentId)
                .tenantId(TenantContext.currentTenantId())
                .providerCode(provider.getCode())
                .serviceType(resolvedServiceType)
                .catalogServiceVersion(catalog != null ? catalog.version() : null)
                .merchantReference(merchantReference)
                .customerReference(customerReference)
                .phoneNumber(phoneNumber)
                .amount(amount != null ? amount : BigDecimal.ZERO)
                .currency(currency != null ? currency : "XOF")
                .status(PaymentStatus.INITIATED)
                .currentStep(initialStep)
                .idempotencyKey(idempotencyKey)
                .contextJson(writeJson(context))
                .build();
        transactionRepository.save(tx);
        addEvent(paymentId, "CREATED", resolvedServiceType.name() + " créé");
        if (catalog != null) {
            addEvent(paymentId, "CATALOG_PRODUCT_SELECTED",
                    "Produit " + catalog.productCode() + " v" + catalog.version().getVersion());
        }
        addEvent(paymentId, "PROVIDER_SELECTED", "Fournisseur " + provider.getCode() + " sélectionné");
        if (payProvider != null) {
            addEvent(paymentId, "PAY_PROVIDER_SELECTED",
                    "Moyen de paiement wallet " + payProvider.getCode() + " sélectionné");
        }

        MDC.put("paymentId", paymentId);

        if (useFlowable(flow)) {
            flowablePaymentRuntime.start(tx, flow, context);
            PaymentTransaction refreshed = transactionRepository.findByPaymentId(paymentId).orElse(tx);
            return toResponse(refreshed, resolveNextAction(refreshed));
        }

        PaymentFlowStep start = resolveStep(flow, initialStep);
        PaymentFlowStep next = start != null ? resolveStep(flow, start.getNextStepSuccess()) : null;
        if (next != null) {
            tx.setCurrentStep(next.getCode());
            transactionRepository.save(tx);
            if (next.getStepType() == StepType.API_CALL
                    || next.getStepType() == StepType.REDIRECT
                    || next.getStepType() == StepType.OTP
                    || next.getStepType() == StepType.CHECK_STATUS) {
                return executeStep(tx, flow, next, context);
            }
            tx.applyStatus(statusForStep(next));
            transactionRepository.save(tx);
        }

        return toResponse(tx, resolveNextAction(tx));
    }

    /**
     * Si le catalogue impose un {@code workflow_key}, préfère le PaymentFlow provider
     * qui porte cette clé BPMN (sinon conserve le flow serviceType).
     */
    private PaymentFlow preferCatalogWorkflow(
            PaymentFlow flow,
            PaymentProvider provider,
            CatalogProductResolver.ResolvedCatalogProduct catalog
    ) {
        if (catalog == null || catalog.workflowKey() == null || catalog.workflowKey().isBlank()) {
            return flow;
        }
        String key = catalog.workflowKey().trim();
        if (flow != null && key.equals(flow.getBpmProcessKey())) {
            return flow;
        }
        return flowRepository.findFirstByProviderIdAndBpmProcessKeyAndActifTrue(provider.getId(), key)
                .orElseGet(() -> catalogFlow(provider, key, flow != null ? flow.getServiceType() : null));
    }

    private static ServiceTypeCode resolveServiceType(
            ServiceTypeCode requested,
            CatalogProductResolver.ResolvedCatalogProduct catalog
    ) {
        if (catalog != null && catalog.serviceType() != null
                && requested != null
                && catalog.serviceType() != requested) {
            throw new BusinessException(
                    "CATALOG_SERVICE_TYPE_MISMATCH",
                    "serviceType=" + requested + " incompatible avec le produit catalogue "
                            + catalog.productCode() + " (attendu " + catalog.serviceType() + ")");
        }
        if (catalog != null && catalog.serviceType() != null) {
            return catalog.serviceType();
        }
        if (requested != null) {
            return requested;
        }
        return ServiceTypeCode.PAYEMENT;
    }

    /** Compatibilité appels existants (paiement). */
    @Transactional
    public PaymentResponse initiate(
            String providerCode,
            BigDecimal amount,
            String currency,
            String customerReference,
            String merchantReference,
            String phoneNumber,
            Map<String, Object> data,
            String idempotencyKey
    ) {
        return initiate(providerCode, ServiceTypeCode.PAYEMENT, amount, currency,
                customerReference, merchantReference, phoneNumber, data, idempotencyKey);
    }

    @Transactional
    public PaymentResponse submitAction(String paymentId, String action, Map<String, Object> data) {
        PaymentTransaction tx = loadAccessible(paymentId);
        assertNotTerminal(tx);
        MDC.put("paymentId", paymentId);

        Map<String, Object> context = readContext(tx);
        if (data != null) {
            data.forEach((k, v) -> {
                if (!SensitiveDataMasker.isSensitiveKey(k) || "otp".equalsIgnoreCase(k)) {
                    context.put(k, v);
                }
            });
            if (data.containsKey("phoneNumber")) {
                tx.setPhoneNumber(String.valueOf(data.get("phoneNumber")));
            }
            if (data.containsKey("amount")) {
                tx.setAmount(new BigDecimal(String.valueOf(data.get("amount"))));
                context.put("amount", tx.getAmount());
            }
        }
        // Never persist OTP in clear
        context.remove("otp");
        tx.setContextJson(writeJson(context));

        PaymentFlow flow = loadFlow(tx);
        if (useFlowable(flow)) {
            return switch (action) {
                case "SUBMIT_FORM" -> {
                    flowablePaymentRuntime.submitForm(tx, flow, data != null ? data : Map.of());
                    PaymentTransaction refreshed = transactionRepository.findByPaymentId(paymentId).orElse(tx);
                    yield toResponse(refreshed, resolveNextAction(refreshed));
                }
                case "SUBMIT_OTP" -> {
                    flowablePaymentRuntime.submitOtp(tx, flow, data != null ? data : Map.of());
                    PaymentTransaction refreshed = transactionRepository.findByPaymentId(paymentId).orElse(tx);
                    yield toResponse(refreshed, resolveNextAction(refreshed));
                }
                case "APPROVE", "REJECT" -> {
                    boolean approved = "APPROVE".equals(action);
                    String comment = data != null && data.get("approvalComment") != null
                            ? String.valueOf(data.get("approvalComment"))
                            : (data != null && data.get("comment") != null ? String.valueOf(data.get("comment")) : null);
                    String actor = currentUsername();
                    flowablePaymentRuntime.submitApproval(tx, approved, comment, actor);
                    PaymentTransaction refreshed = transactionRepository.findByPaymentId(paymentId).orElse(tx);
                    yield toResponse(refreshed, resolveNextAction(refreshed));
                }
                case "CONTINUE", "CONFIRM" -> {
                    // Pas de userTask générique : attendre / poll
                    PaymentTransaction refreshed = transactionRepository.findByPaymentId(paymentId).orElse(tx);
                    yield toResponse(refreshed, resolveNextAction(refreshed));
                }
                case "CHECK_STATUS" -> checkStatus(tx, flow);
                default -> throw new BusinessException("UNKNOWN_ACTION", "Action inconnue: " + action);
            };
        }

        PaymentFlowStep current = resolveStep(flow, tx.getCurrentStep());
        if (current == null) {
            throw new BusinessException("INVALID_STEP", "Étape courante invalide");
        }

        return switch (action) {
            case "SUBMIT_FORM" -> handleFormSubmit(tx, flow, current, data != null ? data : Map.of());
            case "SUBMIT_OTP" -> handleOtpSubmit(tx, flow, current, data != null ? data : Map.of());
            case "CONTINUE", "CONFIRM" -> advanceAndExecute(tx, flow, current, data != null ? data : Map.of());
            case "CHECK_STATUS" -> checkStatus(tx, flow);
            default -> throw new BusinessException("UNKNOWN_ACTION", "Action inconnue: " + action);
        };
    }

    @Transactional(readOnly = true)
    public PaymentResponse get(String paymentId) {
        PaymentTransaction tx = loadAccessible(paymentId);
        return toResponse(tx, resolveNextAction(tx));
    }

    @Transactional
    public PaymentResponse cancel(String paymentId) {
        PaymentTransaction tx = loadAccessible(paymentId);
        if (isTerminal(tx.getStatus())) {
            throw new BusinessException("PAYMENT_ALREADY_PROCESSED", "Paiement déjà finalisé");
        }
        tx.applyStatus(PaymentStatus.CANCELLED);
        tx.setCompletedAt(Instant.now());
        tx.setCurrentStep("FAILURE");
        transactionRepository.save(tx);
        domainEvents.paymentTerminal(tx, DomainEventTypes.PAYMENT_CANCELLED);
        settlement.onFailure(tx);
        try {
            PaymentFlow flow = loadFlow(tx);
            if (useFlowable(flow)) {
                flowablePaymentRuntime.cancel(tx);
            }
        } catch (Exception ignored) {
            // annulation locale prioritaire
        }
        addEvent(paymentId, "CANCELLED", "Paiement annulé");
        return toResponse(tx, failureAction("Paiement annulé"));
    }

    /**
     * Recon ops : CHECK_STATUS provider pour une txn en WAITING_CALLBACK.
     */
    @Transactional
    public PaymentResponse reconWaitingCallback(String paymentId) {
        PaymentTransaction tx = load(paymentId);
        if (tx.getStatus() != PaymentStatus.WAITING_CALLBACK && tx.getStatus() != PaymentStatus.PROCESSING) {
            throw new BusinessException("RECON_NOT_ALLOWED", "Recon réservé aux statuts d'attente");
        }
        PaymentFlow flow = loadFlow(tx);
        return checkStatus(tx, flow);
    }

    @Transactional
    public PaymentResponse retry(String paymentId) {
        PaymentTransaction tx = loadAccessible(paymentId);
        if (tx.getStatus() != PaymentStatus.UNKNOWN
                && tx.getStatus() != PaymentStatus.PENDING
                && tx.getStatus() != PaymentStatus.FAILED) {
            throw new BusinessException("RETRY_NOT_ALLOWED", "Reprise non autorisée pour ce statut");
        }
        // FAILED est terminal pour les callbacks — hydrate pour autoriser une reprise explicite
        if (tx.getStatus() == PaymentStatus.FAILED) {
            tx.hydrateStatus(PaymentStatus.UNKNOWN);
            tx.setCompletedAt(null);
            tx.setErrorCode(null);
            tx.setErrorMessage(null);
            transactionRepository.save(tx);
            addEvent(paymentId, "RETRY", "Reprise après FAILED → UNKNOWN");
        }
        PaymentFlow flow = loadFlow(tx);
        return checkStatus(tx, flow);
    }

    private PaymentResponse handleFormSubmit(PaymentTransaction tx, PaymentFlow flow, PaymentFlowStep current, Map<String, Object> data) {
        addEvent(tx.getPaymentId(), "FORM_SUBMITTED", "Formulaire soumis");
        if (current.getStepType() == StepType.FORM || current.getStepType() == StepType.START) {
            PaymentFlowStep next = resolveStep(flow, current.getNextStepSuccess());
            if (next == null) {
                throw new BusinessException("NO_NEXT_STEP", "Prochaine étape introuvable");
            }
            if (next.getStepType() == StepType.WAIT_CALLBACK) {
                Map<String, Object> ctx = readContext(tx);
                Object holder = data.get("accountHolder");
                Object iban = data.get("iban");
                if (holder != null || iban != null) {
                    ctx.put("waitMessage",
                            "Effectuez un virement de %s %s. Titulaire: %s — Compte: %s — Mentionnez la référence %s. La confirmation est automatique après réception du callback bancaire."
                                    .formatted(
                                            tx.getAmount(),
                                            tx.getCurrency(),
                                            holder != null ? holder : "—",
                                            iban != null ? iban : "—",
                                            tx.getPaymentId()
                                    ));
                    tx.setContextJson(writeJson(ctx));
                }
            }
            tx.setCurrentStep(next.getCode());
            transactionRepository.save(tx);
            return executeStep(tx, flow, next, data);
        }
        return executeStep(tx, flow, current, data);
    }

    private PaymentResponse handleOtpSubmit(PaymentTransaction tx, PaymentFlow flow, PaymentFlowStep current, Map<String, Object> data) {
        Object otp = data.get("otp");
        if (otp == null || String.valueOf(otp).isBlank()) {
            throw new BusinessException("OTP_INVALID", "OTP requis");
        }
        String otpValue = String.valueOf(otp).trim();

        ProviderOtpConfig otpConfig = otpConfigRepository.findByProviderId(
                providerRepository.findByCode(tx.getProviderCode()).orElseThrow().getId()
        ).orElse(null);

        if (otpConfig != null && otpConfig.getLength() != null && otpConfig.getLength() > 0) {
            if (!otpValue.matches("\\d{" + otpConfig.getLength() + "}")) {
                throw new BusinessException(
                        "OTP_INVALID",
                        "Le code OTP doit contenir " + otpConfig.getLength() + " chiffres"
                );
            }
        }

        addEvent(tx.getPaymentId(), "OTP_SUBMITTED", "OTP utilisateur soumis");

        Map<String, Object> execInput = new HashMap<>(data);
        execInput.put("otp", otpValue);

        PaymentFlowStep otpStep = current;
        if (current.getStepType() == StepType.OTP) {
            PaymentFlowStep enterStep = resolveStep(flow, current.getNextStepSuccess());
            if (enterStep != null && enterStep.getStepType() == StepType.ENTER_OTP) {
                tx.setCurrentStep(enterStep.getCode());
                transactionRepository.save(tx);
                otpStep = enterStep;
            }
        }

        if (otpStep.getStepType() == StepType.OTP || otpStep.getStepType() == StepType.ENTER_OTP) {
            String op = otpStep.getApiOperationCode();
            if (op == null || "GENERATE_OTP".equalsIgnoreCase(op)) {
                if (otpConfig != null && otpConfig.getValidateOperation() != null) {
                    op = otpConfig.getValidateOperation();
                } else {
                    op = "CONFIRM_PAYMENT";
                }
            }
            ProviderExecutionResult result = callProvider(tx, otpStep.getCode(), op, execInput);
            return interpretApiResult(tx, flow, otpStep, result);
        }
        return executeStep(tx, flow, otpStep, execInput);
    }

    private PaymentResponse advanceAndExecute(PaymentTransaction tx, PaymentFlow flow, PaymentFlowStep current, Map<String, Object> data) {
        return executeStep(tx, flow, current, data);
    }

    private PaymentResponse executeStep(PaymentTransaction tx, PaymentFlow flow, PaymentFlowStep step, Map<String, Object> data) {
        tx.setCurrentStep(step.getCode());

        return switch (step.getStepType()) {
            case FORM, ENTER_OTP, OTP -> {
                if (step.getStepType() == StepType.OTP && step.getApiOperationCode() != null
                        && (data == null || !data.containsKey("otp"))) {
                    // Generate OTP via API
                    tx.applyStatus(PaymentStatus.OTP_GENERATION_PENDING);
                    addEvent(tx.getPaymentId(), "OTP_GENERATION", "Génération OTP demandée");
                    ProviderExecutionResult result = callProvider(tx, step.getCode(), step.getApiOperationCode(), data);
                    if (result.isTimedOut()) {
                        yield markUnknown(tx, "Timeout lors de la génération OTP");
                    }
                    if (!result.isSuccess()) {
                        yield fail(tx, flow, step, result);
                    }
                    addEvent(tx.getPaymentId(), "OTP_GENERATED", "OTP généré");
                    PaymentFlowStep next = resolveStep(flow, step.getNextStepSuccess());
                    if (next != null) {
                        tx.setCurrentStep(next.getCode());
                    }
                    tx.applyStatus(PaymentStatus.OTP_REQUIRED);
                    transactionRepository.save(tx);
                    yield toResponse(tx, otpAction(tx));
                }
                tx.applyStatus(step.getStepType() == StepType.FORM ? PaymentStatus.AWAITING_USER_INPUT : PaymentStatus.OTP_REQUIRED);
                transactionRepository.save(tx);
                yield toResponse(tx, resolveNextAction(tx));
            }
            case API_CALL, CHECK_STATUS -> {
                String op = step.getApiOperationCode();
                if (op == null) {
                    throw new BusinessException("OPERATION_MISSING", "Opération API manquante pour l'étape " + step.getCode());
                }
                tx.applyStatus(PaymentStatus.PROCESSING);
                transactionRepository.save(tx);
                addEvent(tx.getPaymentId(), "API_CALL", "Appel " + op);
                ProviderExecutionResult result = callProvider(tx, step.getCode(), op, data);
                yield interpretApiResult(tx, flow, step, result);
            }
            case REDIRECT -> {
                tx.applyStatus(PaymentStatus.REDIRECT_REQUIRED);
                String op = step.getApiOperationCode();
                if (op != null) {
                    ProviderExecutionResult result = callProvider(tx, step.getCode(), op, data);
                    if (result.isTimedOut()) {
                        yield markUnknown(tx, "Timeout lors de l'initialisation redirect");
                    }
                    if (!result.isSuccess()) {
                        yield fail(tx, flow, step, result);
                    }
                    if (result.getProviderReference() != null) {
                        tx.setProviderReference(result.getProviderReference());
                    }
                    Map<String, Object> ctx = readContext(tx);
                    Object url = result.getResponseBody() != null ? result.getResponseBody().get("redirectUrl") : null;
                    if (url == null && result.getResponseBody() != null) {
                        url = result.getResponseBody().get("url");
                    }
                    if (url != null) {
                        ctx.put("redirectUrl", url);
                    }
                    tx.setContextJson(writeJson(ctx));
                }
                PaymentFlowStep next = resolveStep(flow, step.getNextStepSuccess());
                if (next != null) {
                    tx.setCurrentStep(next.getCode());
                    if (next.getStepType() == StepType.WAIT_CALLBACK) {
                        tx.applyStatus(PaymentStatus.WAITING_CALLBACK);
                    }
                }
                transactionRepository.save(tx);
                addEvent(tx.getPaymentId(), "REDIRECT", "Redirection fournisseur");
                Map<String, Object> ctxAfter = readContext(tx);
                Object redirectUrl = ctxAfter.get("redirectUrl");
                if (redirectUrl != null) {
                    yield toResponse(tx, NextActionDto.builder()
                            .type(NextActionType.REDIRECT)
                            .url(String.valueOf(redirectUrl))
                            .message("Vous allez être redirigé vers la page de paiement sécurisée")
                            .build());
                }
                yield toResponse(tx, resolveNextAction(tx));
            }
            case WAIT_CALLBACK -> {
                tx.applyStatus(PaymentStatus.WAITING_CALLBACK);
                transactionRepository.save(tx);
                yield toResponse(tx, waitAction(tx));
            }
            case SUCCESS -> {
                tx.applyStatus(PaymentStatus.SUCCESS);
                tx.setCompletedAt(Instant.now());
                transactionRepository.save(tx);
                recordSpend(tx);
                addEvent(tx.getPaymentId(), "SUCCESS", "Paiement réussi");
                yield toResponse(tx, successAction());
            }
            case FAILURE -> {
                tx.applyStatus(PaymentStatus.FAILED);
                tx.setCompletedAt(Instant.now());
                transactionRepository.save(tx);
                yield toResponse(tx, failureAction(tx.getErrorMessage()));
            }
            case START -> {
                PaymentFlowStep next = resolveStep(flow, step.getNextStepSuccess());
                yield next != null ? executeStep(tx, flow, next, data) : toResponse(tx, resolveNextAction(tx));
            }
        };
    }

    private PaymentResponse interpretApiResult(
            PaymentTransaction tx,
            PaymentFlow flow,
            PaymentFlowStep step,
            ProviderExecutionResult result
    ) {
        if (result.isTimedOut()) {
            return markUnknown(tx, "Timeout fournisseur — statut à vérifier");
        }

        PaymentStatus mapped = mapStatus(tx.getProviderCode(), result.getProviderStatus(), result.getGatewayStatus());
        if (result.getProviderReference() != null) {
            tx.setProviderReference(result.getProviderReference());
        }
        applyProviderResponseSideEffects(tx, step, result);

        if (result.isSuccess() && (mapped == PaymentStatus.SUCCESS || mapped == null
                || "LOOKUP_BILL".equals(step.getApiOperationCode()))) {
            PaymentFlowStep next = resolveStep(flow, step.getNextStepSuccess());
            if (next == null || next.getStepType() == StepType.SUCCESS) {
                tx.applyStatus(PaymentStatus.SUCCESS);
                tx.setCurrentStep(next != null ? next.getCode() : "SUCCESS");
                tx.setCompletedAt(Instant.now());
                transactionRepository.save(tx);
                recordSpend(tx);
                addEvent(tx.getPaymentId(), "SUCCESS", "Confirmation fournisseur");
                return toResponse(tx, successAction());
            }
            tx.setCurrentStep(next.getCode());
            transactionRepository.save(tx);
            // Auto-continue non-interactive steps
            if (next.getStepType() == StepType.API_CALL || next.getStepType() == StepType.CHECK_STATUS
                    || next.getStepType() == StepType.SUCCESS || next.getStepType() == StepType.REDIRECT
                    || next.getStepType() == StepType.OTP) {
                return executeStep(tx, flow, next, Map.of());
            }
            tx.applyStatus(statusForStep(next));
            transactionRepository.save(tx);
            return toResponse(tx, resolveNextAction(tx));
        }

        if (mapped == PaymentStatus.PENDING || mapped == PaymentStatus.UNKNOWN) {
            tx.applyStatus(mapped);
            transactionRepository.save(tx);
            addEvent(tx.getPaymentId(), mapped.name(), result.getProviderMessage());
            return toResponse(tx, waitAction());
        }

        return fail(tx, flow, step, result);
    }

    private void applyProviderResponseSideEffects(
            PaymentTransaction tx,
            PaymentFlowStep step,
            ProviderExecutionResult result
    ) {
        if (result.getResponseBody() == null) {
            return;
        }
        Map<String, Object> ctx = readContext(tx);
        ProviderResponseContextMerger.MergeResult merged = responseContextMerger.merge(
                tx, ctx, step.getApiOperationCode(), result.getResponseBody());
        if (merged.contextChanged()) {
            tx.setContextJson(writeJson(ctx));
        }
        merged.audit().ifPresent(hint ->
                addEvent(tx.getPaymentId(), hint.eventType(), hint.message()));
    }

    private PaymentResponse checkStatus(PaymentTransaction tx, PaymentFlow flow) {
        addEvent(tx.getPaymentId(), "STATUS_CHECK", "Consultation du statut");
        ProviderExecutionResult result = callProvider(tx, "CHECK_STATUS", "GET_STATUS", Map.of());
        if (result.isTimedOut()) {
            tx.applyStatus(PaymentStatus.UNKNOWN);
            transactionRepository.save(tx);
            return toResponse(tx, waitAction());
        }
        PaymentStatus mapped = mapStatus(tx.getProviderCode(), result.getProviderStatus(), result.getGatewayStatus());
        if (mapped == PaymentStatus.SUCCESS) {
            tx.applyStatus(PaymentStatus.SUCCESS);
            tx.setCompletedAt(Instant.now());
            tx.setCurrentStep("SUCCESS");
            if (result.getProviderReference() != null) {
                tx.setProviderReference(result.getProviderReference());
            }
            transactionRepository.save(tx);
            recordSpend(tx);
            addEvent(tx.getPaymentId(), "SUCCESS", "Statut confirmé");
            return toResponse(tx, successAction());
        }
        if (mapped == PaymentStatus.FAILED) {
            tx.applyStatus(PaymentStatus.FAILED);
            tx.setErrorMessage(result.getProviderMessage());
            tx.setCompletedAt(Instant.now());
            transactionRepository.save(tx);
            return toResponse(tx, failureAction(result.getProviderMessage()));
        }
        tx.applyStatus(mapped != null ? mapped : PaymentStatus.PENDING);
        transactionRepository.save(tx);
        return toResponse(tx, waitAction());
    }

    private PaymentResponse fail(PaymentTransaction tx, PaymentFlow flow, PaymentFlowStep step, ProviderExecutionResult result) {
        PaymentFlowStep failStep = resolveStep(flow, step.getNextStepFailure());
        tx.applyStatus(PaymentStatus.FAILED);
        tx.setErrorCode(result.getProviderStatus());
        tx.setErrorMessage(result.getProviderMessage() != null ? result.getProviderMessage() : "Échec fournisseur");
        tx.setCompletedAt(Instant.now());
        if (failStep != null) {
            tx.setCurrentStep(failStep.getCode());
        }
        transactionRepository.save(tx);
        addEvent(tx.getPaymentId(), "FAILED", tx.getErrorMessage());
        return toResponse(tx, failureAction(tx.getErrorMessage()));
    }

    private PaymentResponse markUnknown(PaymentTransaction tx, String message) {
        tx.applyStatus(PaymentStatus.UNKNOWN);
        tx.setErrorMessage(message);
        transactionRepository.save(tx);
        addEvent(tx.getPaymentId(), "UNKNOWN", message);
        return toResponse(tx, waitAction());
    }

    private ProviderExecutionResult callProvider(
            PaymentTransaction tx,
            String stepCode,
            String operationCode,
            Map<String, Object> input
    ) {
        return catalogProviderInvoker.invoke(
                tx, stepCode, operationCode, input != null ? input : Map.of(), readContext(tx));
    }

    private boolean useFlowable(PaymentFlow flow) {
        return orchestrationProperties.isFlowable() && FlowablePaymentRuntime.isFlowableEligible(flow);
    }

    @Transactional
    public void applyWebhookUpdate(String paymentId, String providerStatus, String providerReference, String message) {
        PaymentTransaction tx = load(paymentId);
        // Terminal = immutable (SUCCESS/FAILED/CANCELLED/EXPIRED) — idempotence callback
        if (PaymentStatusTransitionGuard.isTerminal(tx.getStatus())) {
            return;
        }
        PaymentStatus mapped = mapStatus(tx.getProviderCode(), providerStatus, null);
        if (providerReference != null) {
            tx.setProviderReference(providerReference);
        }
        if (mapped == PaymentStatus.SUCCESS) {
            if (tx.applyStatus(PaymentStatus.SUCCESS)) {
                tx.setCompletedAt(Instant.now());
                tx.setCurrentStep("SUCCESS");
                addEvent(paymentId, "CALLBACK_SUCCESS", message != null ? message : "Callback succès");
                recordSpend(tx);
                domainEvents.paymentTerminal(tx, DomainEventTypes.PAYMENT_SUCCEEDED);
                settlement.onSuccess(tx);
                notifyFirstOpDone(tx);
            }
            try {
                PaymentFlow flow = loadFlow(tx);
                if (useFlowable(flow)) {
                    flowablePaymentRuntime.cancel(tx);
                }
            } catch (Exception ignored) {
                // paiement déjà finalisé côté métier
            }
        } else if (mapped == PaymentStatus.FAILED) {
            if (tx.applyStatus(PaymentStatus.FAILED)) {
                tx.setErrorMessage(message);
                tx.setCompletedAt(Instant.now());
                addEvent(paymentId, "CALLBACK_FAILED", message);
                domainEvents.paymentTerminal(tx, DomainEventTypes.PAYMENT_FAILED);
                settlement.onFailure(tx);
            }
        } else {
            tx.applyStatus(mapped != null ? mapped : PaymentStatus.PENDING);
            addEvent(paymentId, "CALLBACK", message);
        }
        transactionRepository.save(tx);
    }

    private NextActionDto resolveNextAction(PaymentTransaction tx) {
        try {
            PaymentFlow flow = loadFlow(tx);
            if (useFlowable(flow)) {
                return bpmCheckoutNextActionService.resolve(tx);
            }
        } catch (Exception ignored) {
            // fallback step-based below
        }
        if (tx.getStatus() == PaymentStatus.SUCCESS) {
            return successAction();
        }
        if (tx.getStatus() == PaymentStatus.FAILED || tx.getStatus() == PaymentStatus.CANCELLED || tx.getStatus() == PaymentStatus.EXPIRED) {
            return failureAction(tx.getErrorMessage());
        }
        if (tx.getStatus() == PaymentStatus.PENDING || tx.getStatus() == PaymentStatus.UNKNOWN
                || tx.getStatus() == PaymentStatus.WAITING_CALLBACK || tx.getStatus() == PaymentStatus.PROCESSING) {
            return waitAction(tx);
        }
        if (tx.getStatus() == PaymentStatus.REDIRECT_REQUIRED || tx.getStatus() == PaymentStatus.WAITING_CALLBACK) {
            Map<String, Object> ctx = readContext(tx);
            Object url = ctx.get("redirectUrl");
            if (url != null) {
                return NextActionDto.builder()
                        .type(NextActionType.REDIRECT)
                        .url(String.valueOf(url))
                        .build();
            }
            return waitAction();
        }

        try {
            PaymentFlow flow = loadFlow(tx);
            PaymentFlowStep step = resolveStep(flow, tx.getCurrentStep());
            if (step == null) {
                return waitAction();
            }
            return switch (step.getStepType()) {
                case FORM -> formAction(step, tx);
                case OTP, ENTER_OTP -> otpAction(tx);
                case REDIRECT -> {
                    Map<String, Object> ctx = readContext(tx);
                    yield NextActionDto.builder()
                            .type(NextActionType.REDIRECT)
                            .url(ctx.get("redirectUrl") != null ? String.valueOf(ctx.get("redirectUrl")) : null)
                            .build();
                }
                case WAIT_CALLBACK, CHECK_STATUS -> waitAction(tx);
                case SUCCESS -> successAction();
                case FAILURE -> failureAction(tx.getErrorMessage());
                default -> waitAction();
            };
        } catch (Exception e) {
            return waitAction();
        }
    }

    private NextActionDto formAction(PaymentFlowStep step, PaymentTransaction tx) {
        Map<String, Object> formSchema = parseAndFilterFormio(step.getConfigurationJson(), tx);
        List<FormFieldDto> fields;
        if (formSchema != null) {
            fields = fieldsFromFormio(formSchema);
        } else {
            fields = parseFields(step.getConfigurationJson()).stream()
                    .filter(f -> !isAlreadyCollected(f.getName(), tx))
                    .toList();
        }
        Map<String, Object> ctx = readContext(tx);
        String message = null;
        if (ctx.get("billLabel") != null) {
            message = "Facture trouvée : " + ctx.get("billLabel")
                    + " — Montant dû : " + tx.getAmount() + " " + tx.getCurrency()
                    + (ctx.get("customerName") != null ? " (" + ctx.get("customerName") + ")" : "");
        }
        return NextActionDto.builder()
                .type(NextActionType.FORM)
                .formKey(step.getCode())
                .fields(fields)
                .formSchema(formSchema)
                .message(message)
                .build();
    }

    private NextActionDto otpAction(PaymentTransaction tx) {
        var otpConfig = providerRepository.findByCode(tx.getProviderCode())
                .flatMap(p -> otpConfigRepository.findByProviderId(p.getId()));
        Integer expires = otpConfig.map(ProviderOtpConfig::getValiditySeconds).orElse(120);
        Integer length = otpConfig.map(ProviderOtpConfig::getLength).orElse(6);
        return NextActionDto.builder()
                .type(NextActionType.ENTER_OTP)
                .expiresIn(expires)
                .otpLength(length)
                .message("Saisissez le code OTP reçu (" + length + " chiffres)")
                .build();
    }

    private NextActionDto waitAction() {
        return waitAction(null);
    }

    private NextActionDto waitAction(PaymentTransaction tx) {
        String message = "Paiement en cours de traitement";
        if (tx != null) {
            Object custom = readContext(tx).get("waitMessage");
            if (custom != null && !String.valueOf(custom).isBlank()) {
                message = String.valueOf(custom);
            }
        }
        return NextActionDto.builder()
                .type(NextActionType.WAIT)
                .pollAfterSeconds(3)
                .message(message)
                .build();
    }

    private NextActionDto successAction() {
        return NextActionDto.builder().type(NextActionType.SUCCESS).message("Paiement réussi").build();
    }

    private NextActionDto failureAction(String message) {
        return NextActionDto.builder()
                .type(NextActionType.FAILURE)
                .message(message != null ? message : "Paiement échoué")
                .build();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseAndFilterFormio(String configurationJson, PaymentTransaction tx) {
        if (configurationJson == null || configurationJson.isBlank()) {
            return null;
        }
        try {
            Map<String, Object> cfg = objectMapper.readValue(configurationJson, new TypeReference<>() {});
            Object raw = cfg.get("formio");
            if (!(raw instanceof Map<?, ?>)) {
                return null;
            }
            Map<String, Object> schema = objectMapper.convertValue(raw, new TypeReference<>() {});
            Object components = schema.get("components");
            if (!(components instanceof List<?> list)) {
                return schema;
            }
            List<Map<String, Object>> filtered = list.stream()
                    .filter(item -> item instanceof Map<?, ?>)
                    .map(item -> (Map<String, Object>) item)
                    .filter(comp -> {
                        Object key = comp.get("key");
                        if (key == null) {
                            return true;
                        }
                        String k = String.valueOf(key);
                        if ("submit".equals(k)) {
                            return true;
                        }
                        return !isAlreadyCollected(k, tx);
                    })
                    .map(comp -> objectMapper.convertValue(comp, new TypeReference<Map<String, Object>>() {}))
                    .toList();
            schema.put("components", filtered);
            if (schema.get("display") == null) {
                schema.put("display", "form");
            }
            return schema;
        } catch (Exception ignored) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private List<FormFieldDto> fieldsFromFormio(Map<String, Object> formSchema) {
        Object components = formSchema.get("components");
        if (!(components instanceof List<?> list)) {
            return List.of();
        }
        return list.stream()
                .filter(item -> item instanceof Map<?, ?>)
                .map(item -> (Map<String, Object>) item)
                .filter(comp -> {
                    Object type = comp.get("type");
                    Object key = comp.get("key");
                    return key != null && !"submit".equals(String.valueOf(key))
                            && !"button".equals(String.valueOf(type));
                })
                .map(comp -> {
                    String type = String.valueOf(comp.getOrDefault("type", "textfield"));
                    String mappedType = switch (type) {
                        case "phoneNumber" -> "tel";
                        case "number" -> "number";
                        case "email" -> "email";
                        case "password" -> "password";
                        case "textarea" -> "textarea";
                        case "select", "radio" -> "select";
                        case "checkbox" -> "checkbox";
                        case "currency" -> "money";
                        default -> "text";
                    };
                    boolean required = false;
                    Object validate = comp.get("validate");
                    if (validate instanceof Map<?, ?> v) {
                        required = Boolean.TRUE.equals(v.get("required"))
                                || "true".equals(String.valueOf(v.get("required")));
                    }
                    List<Map<String, String>> options = formioSelectOptions(comp);
                    return FormFieldDto.builder()
                            .name(String.valueOf(comp.get("key")))
                            .label(String.valueOf(comp.getOrDefault("label", comp.get("key"))))
                            .type(mappedType)
                            .required(required)
                            .placeholder(comp.get("placeholder") != null ? String.valueOf(comp.get("placeholder")) : null)
                            .options(options.isEmpty() ? null : options)
                            .build();
                })
                .toList();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, String>> formioSelectOptions(Map<String, Object> comp) {
        List<Map<String, String>> out = new ArrayList<>();
        Object values = comp.get("values");
        if (values == null && comp.get("data") instanceof Map<?, ?> data) {
            values = ((Map<?, ?>) data).get("values");
        }
        if (!(values instanceof List<?> list)) {
            return out;
        }
        for (Object item : list) {
            if (item instanceof Map<?, ?> m) {
                Object value = m.get("value");
                Object label = m.get("label");
                if (value == null && label == null) {
                    continue;
                }
                String v = String.valueOf(value != null ? value : label);
                String l = String.valueOf(label != null ? label : value);
                Map<String, String> opt = new LinkedHashMap<>();
                opt.put("label", l);
                opt.put("value", v);
                out.add(opt);
            } else if (item != null) {
                String v = String.valueOf(item);
                Map<String, String> opt = new LinkedHashMap<>();
                opt.put("label", v);
                opt.put("value", v);
                out.add(opt);
            }
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    private List<FormFieldDto> parseFields(String configurationJson) {
        if (configurationJson == null || configurationJson.isBlank()) {
            return List.of(
                    FormFieldDto.builder().name("phoneNumber").label("Numéro de téléphone").type("tel").required(true).build()
            );
        }
        try {
            Map<String, Object> cfg = objectMapper.readValue(configurationJson, new TypeReference<>() {});
            Object fields = cfg.get("fields");
            if (fields instanceof List<?> list) {
                return list.stream().map(item -> {
                    Map<String, Object> m = (Map<String, Object>) item;
                    return FormFieldDto.builder()
                            .name(String.valueOf(m.get("name")))
                            .label(String.valueOf(m.getOrDefault("label", m.get("name"))))
                            .type(String.valueOf(m.getOrDefault("type", "text")))
                            .required(Boolean.TRUE.equals(m.get("required")) || "true".equals(String.valueOf(m.get("required"))))
                            .placeholder(m.get("placeholder") != null ? String.valueOf(m.get("placeholder")) : null)
                            .pattern(m.get("pattern") != null ? String.valueOf(m.get("pattern")) : null)
                            .build();
                }).toList();
            }
        } catch (Exception ignored) {
        }
        return List.of();
    }

    private boolean isAlreadyCollected(String fieldName, PaymentTransaction tx) {
        if (fieldName == null) {
            return false;
        }
        return switch (fieldName) {
            // Un montant différé (facture, CAPTURE_AMOUNT) est initialisé à zéro :
            // le retirer du formulaire empêcherait l'étape qui doit le collecter.
            case "amount" -> tx.getAmount() != null && tx.getAmount().compareTo(BigDecimal.ZERO) > 0;
            case "currency" -> tx.getCurrency() != null && !tx.getCurrency().isBlank();
            case "phoneNumber", "phone" -> tx.getPhoneNumber() != null && !tx.getPhoneNumber().isBlank();
            case "customerReference" -> tx.getCustomerReference() != null && !tx.getCustomerReference().isBlank();
            case "merchantReference" -> tx.getMerchantReference() != null && !tx.getMerchantReference().isBlank();
            default -> false;
        };
    }

    private PaymentStatus mapStatus(String providerCode, String providerStatus, PaymentStatus fallback) {
        if (providerStatus == null) {
            return fallback;
        }
        return providerRepository.findByCode(providerCode)
                .flatMap(p -> statusMappingRepository.findByProviderIdAndProviderStatus(p.getId(), providerStatus))
                .map(ProviderStatusMapping::getGatewayStatus)
                .orElse(fallback);
    }

    private PaymentStatus statusForStep(PaymentFlowStep step) {
        return switch (step.getStepType()) {
            case FORM -> PaymentStatus.AWAITING_USER_INPUT;
            case OTP, ENTER_OTP -> PaymentStatus.OTP_REQUIRED;
            case REDIRECT -> PaymentStatus.REDIRECT_REQUIRED;
            case WAIT_CALLBACK -> PaymentStatus.WAITING_CALLBACK;
            case SUCCESS -> PaymentStatus.SUCCESS;
            case FAILURE -> PaymentStatus.FAILED;
            default -> PaymentStatus.PROCESSING;
        };
    }

    private PaymentTransaction load(String paymentId) {
        return transactionRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new BusinessException("PAYMENT_NOT_FOUND", "Paiement introuvable", HttpStatus.NOT_FOUND));
    }

    private PaymentTransaction loadAccessible(String paymentId) {
        PaymentTransaction tx = load(paymentId);
        if (!TenantContext.canAccess(tx.getTenantId())) {
            throw new BusinessException("PAYMENT_FORBIDDEN", "Accès refusé à ce paiement", HttpStatus.FORBIDDEN);
        }
        assertPaymentOwnership(tx);
        return tx;
    }

    /**
     * Non-plateforme : seul l'initiateur (context.initiatedBy) ou un approbateur
     * (candidateGroups) peut lire / agir — sauf rôles plateforme.
     */
    private void assertPaymentOwnership(PaymentTransaction tx) {
        if (TenantContext.isPlatformUser()) {
            return;
        }
        String me = TenantContext.currentUsername();
        if (!StringUtils.hasText(me)) {
            return;
        }
        Map<String, Object> ctx = readContext(tx);
        Object initiated = ctx.get(com.gateway.payment.bpm.dmn.DmnTransferApprovalConfig.CTX_INITIATED_BY);
        if (initiated != null && me.equalsIgnoreCase(String.valueOf(initiated).trim())) {
            return;
        }
        var repo = approvalInstances.getIfAvailable();
        if (repo != null) {
            var open = repo.findByPaymentId(tx.getPaymentId());
            if (open.isPresent()) {
                var st = open.get().getStatus();
                if (st == com.gateway.payment.approval.domain.ApprovalInstanceStatus.IN_PROGRESS
                        || st == com.gateway.payment.approval.domain.ApprovalInstanceStatus.PENDING) {
                    // Lecteur potentiel / valideur — candidateGroups imposé au decide
                    return;
                }
            }
        }
        if (initiated == null || String.valueOf(initiated).isBlank()) {
            // Legacy sans initiatedBy : tenant déjà vérifié
            return;
        }
        throw new BusinessException("PAYMENT_FORBIDDEN", "Accès refusé à ce paiement", HttpStatus.FORBIDDEN);
    }

    private void recordSpend(PaymentTransaction tx) {
        var recorder = spendRecorder.getIfAvailable();
        if (recorder != null) {
            recorder.record(tx, readContext(tx));
        }
    }

    private void notifyFirstOpDone(PaymentTransaction tx) {
        var producer = partyActionProducer.getIfAvailable();
        if (producer == null) {
            return;
        }
        java.util.UUID partyId = com.gateway.payment.inbox.PartyActionProducer.partyIdFromContext(readContext(tx));
        if (partyId != null) {
            producer.firstOpComplete(partyId);
        }
    }

    private PaymentFlow resolveFlowForServiceType(PaymentProvider provider, ServiceTypeCode serviceType) {
        return flowRepository.findByProviderIdAndServiceType(provider.getId(), serviceType)
                .filter(PaymentFlow::isActif)
                .orElseThrow(() -> new BusinessException("FLOW_NOT_FOUND",
                        "Aucun parcours actif pour " + serviceType + " / " + provider.getCode()));
    }

    private PaymentFlow resolveFlowForServiceType(
            PaymentProvider provider,
            ServiceTypeCode serviceType,
            CatalogProductResolver.ResolvedCatalogProduct catalog
    ) {
        return flowRepository.findByProviderIdAndServiceType(provider.getId(), serviceType)
                .filter(PaymentFlow::isActif)
                .orElseGet(() -> {
                    if (catalog != null && StringUtils.hasText(catalog.workflowKey())) {
                        return catalogFlow(provider, catalog.workflowKey(), serviceType);
                    }
                    throw new BusinessException("FLOW_NOT_FOUND",
                            "Aucun parcours actif pour " + serviceType + " / " + provider.getCode());
                });
    }

    private PaymentFlow catalogFlow(PaymentProvider provider, String workflowKey, ServiceTypeCode serviceType) {
        PaymentFlow flow = new PaymentFlow();
        String key = workflowKey.trim();
        flow.setId(UUID.nameUUIDFromBytes(
                ("catalog-flow-" + provider.getId() + "-" + key).getBytes(StandardCharsets.UTF_8)));
        flow.setProvider(provider);
        flow.setCode(key);
        flow.setNom(provider.getLibelle() != null ? provider.getLibelle() : key);
        flow.setBpmProcessKey(key);
        flow.setActif(true);
        flow.setServiceType(serviceType != null ? serviceType : ServiceTypeCode.TRANSFERT);
        flow.setSteps(new java.util.ArrayList<>());
        return flow;
    }

    private PaymentFlow loadFlow(PaymentTransaction tx) {
        Map<String, Object> ctx = readContext(tx);
        PaymentProvider provider = runtimeProviders.find(tx.getProviderCode())
                .orElseThrow(() -> new BusinessException("PROVIDER_NOT_FOUND", "Fournisseur introuvable"));
        if (ctx.get("flowId") != null) {
            java.util.UUID flowId = java.util.UUID.fromString(String.valueOf(ctx.get("flowId")));
            java.util.Optional<PaymentFlow> stored = flowRepository.findById(flowId);
            if (stored.isPresent()) {
                return stored.get();
            }
            String workflow = firstNonBlank(
                    ctx.get("catalogWorkflowKey") != null ? String.valueOf(ctx.get("catalogWorkflowKey")) : null,
                    ctx.get("bpmProcessKey") != null ? String.valueOf(ctx.get("bpmProcessKey")) : null);
            if (workflow != null) {
                return catalogFlow(provider, workflow, tx.getServiceType());
            }
            throw new BusinessException("FLOW_NOT_FOUND", "Parcours introuvable");
        }
        ServiceTypeCode serviceType = tx.getServiceType() != null ? tx.getServiceType() : ServiceTypeCode.PAYEMENT;
        return resolveFlowForServiceType(provider, serviceType);
    }

    private PaymentFlowStep resolveStep(PaymentFlow flow, String code) {
        if (code == null) {
            return null;
        }
        return flow.getSteps().stream().filter(s -> code.equals(s.getCode())).findFirst().orElse(null);
    }

    private void assertNotTerminal(PaymentTransaction tx) {
        if (isTerminal(tx.getStatus())) {
            throw new BusinessException("PAYMENT_ALREADY_PROCESSED", "Paiement déjà traité");
        }
    }

    private static String currentUsername() {
        try {
            var auth = org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return null;
            }
            String name = auth.getName();
            if (name == null || name.isBlank() || "anonymousUser".equalsIgnoreCase(name)) {
                return null;
            }
            return name.trim();
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isTerminal(PaymentStatus status) {
        return PaymentStatusTransitionGuard.isTerminal(status);
    }

    private void addEvent(String paymentId, String type, String message) {
        PaymentEvent event = PaymentEvent.builder()
                .paymentId(paymentId)
                .eventType(type)
                .message(message)
                .build();
        if (performanceProperties != null && performanceProperties.isAsyncAudit()) {
            eventAuditWriter.persistAsync(event);
        } else {
            eventRepository.save(event);
        }
    }

    private static String dataString(Map<String, Object> data, String... keys) {
        if (data == null || keys == null) {
            return null;
        }
        for (String key : keys) {
            Object v = data.get(key);
            if (v != null && !String.valueOf(v).isBlank()) {
                return String.valueOf(v).trim();
            }
        }
        return null;
    }

    private static String stringFromMap(Map<String, Object> map, String key) {
        if (map == null || key == null) {
            return null;
        }
        Object v = map.get(key);
        if (v == null || String.valueOf(v).isBlank()) {
            return null;
        }
        return String.valueOf(v).trim();
    }

    /**
     * Pose {@code billInquiryMode} (LIST|INFOS|DIRECT) d'après le produit facturier
     * ou une valeur explicite dans le payload.
     */
    private void applyBillInquiryMode(Map<String, Object> context, Map<String, Object> data) {
        String explicit = BillInquiryMode.normalize(firstNonBlank(
                dataString(data, "billInquiryMode"),
                stringFromMap(context, "billInquiryMode")));
        if (explicit != null) {
            context.put("billInquiryMode", explicit);
            return;
        }
        String billerCode = firstNonBlank(
                stringFromMap(context, "billerCode"),
                dataString(data, "billerCode"));
        String productCode = firstNonBlank(
                stringFromMap(context, "productCode"),
                stringFromMap(context, "billerProductCode"),
                dataString(data, "productCode"),
                dataString(data, "billerProductCode"));
        if (StringUtils.hasText(billerCode) && StringUtils.hasText(productCode)) {
            try {
                BillerProduct product = billerService.requireProductByCodes(billerCode, productCode);
                JourneyCode journey = product.getJourneyCode();
                context.put("billInquiryMode", BillInquiryMode.fromJourney(journey));
                if (journey != null && context.get("journeyCode") == null) {
                    context.put("journeyCode", journey.name());
                }
                return;
            } catch (BusinessException ignored) {
                // fallback INFOS
            }
        }
        // Compat : sans produit → ancienne voie LOOKUP
        context.put("billInquiryMode", BillInquiryMode.INFOS);
    }

    private String resolveBillProviderFromCatalog(Map<String, Object> data) {
        String billerCode = dataString(data, "billerCode");
        if (billerCode == null && data != null && data.get("billers") instanceof List<?> billers
                && !billers.isEmpty() && billers.get(0) != null) {
            billerCode = String.valueOf(billers.get(0)).trim();
        }
        String productCode = dataString(data, "productCode");
        if (productCode == null && data != null && data.get("products") instanceof List<?> products
                && !products.isEmpty() && products.get(0) != null) {
            productCode = String.valueOf(products.get(0)).trim();
        }
        return billerService.resolveFacturierProviderCode(billerCode, productCode).orElse(null);
    }

    private boolean isFacturierProvider(String providerCode) {
        if (providerCode == null || providerCode.isBlank()) {
            return false;
        }
        return providerRepository.findByCode(providerCode.trim())
                .map(p -> p.getParticipantKind() == FseParticipantKind.FACTURIER)
                .orElse(false);
    }

    /**
     * Clé BPMN du parcours wallet (OTP / redirect) pour le callActivity facture.
     */
    private Optional<String> resolveWalletCheckoutProcessKey(PaymentProvider payProvider) {
        if (payProvider == null) {
            return Optional.empty();
        }
        return flowRepository.findByProviderIdAndServiceType(payProvider.getId(), ServiceTypeCode.PAYEMENT)
                .filter(PaymentFlow::isActif)
                .map(PaymentFlow::getBpmProcessKey)
                .filter(k -> k != null && !k.isBlank());
    }

    /**
     * Rétrocompat : un seul {@code provider} dans la requête peuplé le rôle principal
     * (SOURCE / PAYER) si roleAssignments absents.
     */
    @SuppressWarnings("unchecked")
    private void seedRoleAssignmentsFromProvider(
            Map<String, Object> data,
            CatalogProductCasting casting,
            String providerCode,
            ServiceTypeCode serviceType
    ) {
        if (data == null || casting == null || casting.roles().isEmpty()
                || providerCode == null || providerCode.isBlank()) {
            return;
        }
        Object existing = data.get("roleAssignments");
        if (existing instanceof Map<?, ?> map && !map.isEmpty()) {
            return;
        }
        Map<String, String> seeded = new HashMap<>();
        if (serviceType != null && serviceType.isBillPayment()) {
            if (isFacturierProvider(providerCode)) {
                seeded.put("BILLER", providerCode.trim().toUpperCase());
            } else {
                seeded.put("PAYER", providerCode.trim().toUpperCase());
            }
        } else {
            seeded.put("SOURCE", providerCode.trim().toUpperCase());
        }
        data.put("roleAssignments", seeded);
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String v : values) {
            if (v != null && !v.isBlank()) {
                return v.trim();
            }
        }
        return null;
    }

    private Map<String, Object> readContext(PaymentTransaction tx) {
        if (tx.getContextJson() == null || tx.getContextJson().isBlank()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(tx.getContextJson(), new TypeReference<>() {});
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return "{}";
        }
    }

    private Map<String, Object> sanitizeForLog(Map<String, Object> input) {
        Map<String, Object> copy = new HashMap<>();
        input.forEach((k, v) -> copy.put(k, SensitiveDataMasker.isSensitiveKey(k) ? "***" : v));
        return copy;
    }

    private String generatePaymentId() {
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        long seq = SEQ.getAndIncrement();
        String suffix = java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "PAY-%s-%06d-%s".formatted(date, seq, suffix);
    }

    private PaymentResponse toResponse(PaymentTransaction tx, NextActionDto nextAction) {
        PaymentResponse.PaymentResponseBuilder builder = PaymentResponse.builder()
                .paymentId(tx.getPaymentId())
                .provider(tx.getProviderCode())
                .serviceType(tx.getServiceType() != null ? tx.getServiceType() : ServiceTypeCode.PAYEMENT)
                .amount(tx.getAmount())
                .currency(tx.getCurrency())
                .status(tx.getStatus())
                .currentStep(tx.getCurrentStep())
                .providerReference(tx.getProviderReference())
                .customerReference(tx.getCustomerReference())
                .phoneNumber(tx.getPhoneNumber())
                .nextAction(nextAction)
                .errorCode(tx.getErrorCode())
                .errorMessage(tx.getErrorMessage())
                .createdAt(tx.getCreatedAt())
                .updatedAt(tx.getUpdatedAt());
        applyCatalogSnapshot(builder, tx);
        Map<String, Object> ctx = readContext(tx);
        Object journey = ctx.get("journeyCode");
        if (journey != null && !String.valueOf(journey).isBlank()) {
            builder.journeyCode(String.valueOf(journey));
        }
        Object channel = ctx.get("accessChannel");
        if (channel == null) {
            channel = ctx.get("channel");
        }
        if (channel != null && !String.valueOf(channel).isBlank()) {
            builder.accessChannel(String.valueOf(channel));
        }
        Object withdrawalChannel = ctx.get("withdrawalChannel");
        if (withdrawalChannel != null && !String.valueOf(withdrawalChannel).isBlank()) {
            builder.withdrawalChannel(String.valueOf(withdrawalChannel).trim());
        }
        String catalogCode = null;
        String catalogName = null;
        try {
            if (tx.getCatalogServiceVersion() != null
                    && tx.getCatalogServiceVersion().getCatalogDeService() != null) {
                catalogCode = tx.getCatalogServiceVersion().getCatalogDeService().getCode();
                catalogName = tx.getCatalogServiceVersion().getCatalogDeService().getName();
            }
        } catch (RuntimeException ignored) {
            // lazy hors session
        }
        if (!StringUtils.hasText(catalogCode) && ctx.get("catalogServiceCode") != null) {
            catalogCode = String.valueOf(ctx.get("catalogServiceCode"));
        }
        String status = tx.getStatus() != null ? tx.getStatus().name() : null;
        String serviceType = tx.getServiceType() != null ? tx.getServiceType().name() : null;
        Map<String, Object> historyConfig = null;
        try {
            if (tx.getCatalogServiceVersion() != null && tx.getCatalogServiceVersion().getId() != null) {
                historyConfig = configurationLocalizer.historyOf(
                        tx.getCatalogServiceVersion().getId(),
                        tx.getCatalogServiceVersion().getConfiguration());
            }
        } catch (RuntimeException ignored) {
            // lazy hors session
        }
        HistoryLabelFormatter.Labels labels = historyLabelFormatter.format(
                catalogCode,
                serviceType,
                status,
                tx.getProviderCode(),
                tx.getCreatedAt(),
                ctx,
                catalogName,
                historyConfig
        );
        builder.title(labels.title())
                .subtitle(labels.subtitle())
                .productLabel(labels.productLabel())
                .statusLabel(labels.statusLabel())
                .agentDisplay(historyLabelFormatter.agentDisplay(ctx));
        return builder.build();
    }

    private void applyCatalogSnapshot(PaymentResponse.PaymentResponseBuilder builder, PaymentTransaction tx) {
        try {
            if (tx.getCatalogServiceVersion() != null) {
                var version = tx.getCatalogServiceVersion();
                builder.catalogServiceVersionId(version.getId())
                        .catalogServiceVersion(version.getVersion());
                if (version.getCatalogDeService() != null) {
                    builder.catalogServiceCode(version.getCatalogDeService().getCode());
                }
                return;
            }
        } catch (RuntimeException ignored) {
            // Association lazy hors session : repli sur le context_json figé à l'init.
        }
        Map<String, Object> ctx = readContext(tx);
        Object code = ctx.get("catalogServiceCode");
        if (code != null && !String.valueOf(code).isBlank()) {
            builder.catalogServiceCode(String.valueOf(code));
        }
        Object versionLabel = ctx.get("catalogServiceVersion");
        if (versionLabel != null && !String.valueOf(versionLabel).isBlank()) {
            builder.catalogServiceVersion(String.valueOf(versionLabel));
        }
        Object versionId = ctx.get("catalogServiceVersionId");
        if (versionId != null && !String.valueOf(versionId).isBlank()) {
            try {
                builder.catalogServiceVersionId(UUID.fromString(String.valueOf(versionId)));
            } catch (IllegalArgumentException ignored) {
                // ignore malformed snapshot
            }
        }
    }

}
