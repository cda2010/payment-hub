package com.gateway.payment.bpm;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.payment.billing.connector.BillResponseSupport;
import com.gateway.payment.billing.connector.dto.Bill;
import com.gateway.payment.common.domain.ServiceTypeCode;
import com.gateway.payment.common.exception.BusinessException;
import com.gateway.payment.configuration.domain.PaymentFlow;
import com.gateway.payment.configuration.domain.PaymentProvider;
import com.gateway.payment.configuration.repository.PaymentFlowRepository;
import com.gateway.payment.omnicanal.external.RuntimeProviderResolver;
import com.gateway.payment.payment.domain.PaymentEvent;
import com.gateway.payment.payment.domain.PaymentTransaction;
import com.gateway.payment.payment.repository.PaymentEventRepository;
import org.flowable.task.api.Task;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Sélection mid-flow des providers (facture wallet, transfert SOURCE/DEST)
 * extraite de {@link FlowablePaymentRuntime}.
 */
@Service
public class FlowableRoleSelectionService {

    private static final String SELECT_PAY_TASK = "selectPayProvider";
    private static final String SELECT_PAY_FORM = "provider-bill-select-pay";
    private static final String SELECT_BILL_TASK = "selectBill";
    private static final String SELECT_BILL_FORM = "provider-bill-select";
    private static final String DEFAULT_WALLET_CHECKOUT = "providerOtpCheckout";

    private final RuntimeProviderResolver runtimeProviders;
    private final PaymentFlowRepository flowRepository;
    private final PaymentEventRepository eventRepository;
    private final ObjectMapper objectMapper;
    private final BillResponseSupport billResponseSupport;
    private final AgentNetworkUniteService agentNetworkUniteService;

    public FlowableRoleSelectionService(
            RuntimeProviderResolver runtimeProviders,
            PaymentFlowRepository flowRepository,
            PaymentEventRepository eventRepository,
            ObjectMapper objectMapper,
            BillResponseSupport billResponseSupport,
            AgentNetworkUniteService agentNetworkUniteService
    ) {
        this.runtimeProviders = runtimeProviders;
        this.flowRepository = flowRepository;
        this.eventRepository = eventRepository;
        this.objectMapper = objectMapper;
        this.billResponseSupport = billResponseSupport;
        this.agentNetworkUniteService = agentNetworkUniteService;
    }

    public void applyFormSelections(
            PaymentTransaction tx,
            Task active,
            Map<String, Object> data,
            Map<String, Object> vars
    ) {
        applyBillSelection(tx, active, data, vars);
        applyPayProviderSelection(tx, active, data, vars);
        applyTransferRoleSelection(tx, data, vars);
    }

    /**
     * Step {@code selectBill} : pose montant / libellés depuis {@link BillResponseSupport}
     * (facture choisie dans {@code context.bills} / {@code billResponse}).
     */
    public void applyBillSelection(
            PaymentTransaction tx,
            Task active,
            Map<String, Object> data,
            Map<String, Object> vars
    ) {
        if (data == null) {
            return;
        }
        String billRef = firstNonBlank(
                stringVal(data.get("billReference")),
                stringVal(data.get("selectedBillReference")),
                stringVal(data.get("reference")));
        if (!StringUtils.hasText(billRef)) {
            return;
        }
        boolean selectStep = active != null && (
                SELECT_BILL_TASK.equals(active.getTaskDefinitionKey())
                        || SELECT_BILL_FORM.equalsIgnoreCase(active.getFormKey()));
        Map<String, Object> ctx = readContext(tx);
        var selected = billResponseSupport.findByReference(ctx, billRef);
        if (selected.isEmpty()) {
            if (selectStep) {
                throw new BusinessException("BILL_NOT_FOUND",
                        "Facture introuvable dans la liste: " + billRef.trim());
            }
            vars.put("billReference", billRef.trim());
            return;
        }
        Bill bill = selected.get();
        billResponseSupport.applySelectedBill(bill, tx, vars);
        addEvent(tx.getPaymentId(), "BILL_SELECTED",
                "Facture " + bill.reference()
                        + (tx.getAmount() != null ? " — " + tx.getAmount() + " " + tx.getCurrency() : ""));
    }

    /**
     * Step {@code selectPayProvider} : rail wallet + clé BPMN checkout (OTP / redirect).
     */
    @SuppressWarnings("unchecked")
    public void applyPayProviderSelection(
            PaymentTransaction tx,
            Task active,
            Map<String, Object> data,
            Map<String, Object> vars
    ) {
        if (active == null || data == null) {
            return;
        }
        String taskKey = active.getTaskDefinitionKey();
        boolean selectStep = SELECT_PAY_TASK.equals(taskKey)
                || SELECT_PAY_FORM.equalsIgnoreCase(active.getFormKey());
        if (!selectStep && !data.containsKey("payProvider") && !data.containsKey("payProviderCode")) {
            return;
        }
        String payCode = firstNonBlank(
                stringVal(data.get("payProvider")),
                stringVal(data.get("payProviderCode")),
                stringVal(data.get("provider")));
        if (!StringUtils.hasText(payCode)) {
            if (selectStep) {
                throw new BusinessException("PAY_PROVIDER_REQUIRED",
                        "Choisissez un moyen de paiement (Orange Money, Wave, …)");
            }
            return;
        }
        final String resolvedPayCode = payCode.trim();
        PaymentProvider pay = runtimeProviders.find(resolvedPayCode)
                .orElseThrow(() -> new BusinessException("PAY_PROVIDER_NOT_FOUND",
                        "Moyen de paiement introuvable: " + resolvedPayCode));
        if (!pay.isActif()) {
            throw new BusinessException("PAY_PROVIDER_INACTIVE", "Moyen de paiement inactif");
        }
        String billCode = firstNonBlank(
                stringVal(readContext(tx).get("billProviderCode")),
                tx.getProviderCode());
        if (billCode != null && billCode.equalsIgnoreCase(pay.getCode())) {
            throw new BusinessException("PAY_PROVIDER_INVALID",
                    "Le moyen de paiement wallet doit être distinct du rail facture");
        }
        vars.put("payProviderCode", pay.getCode());
        vars.put("providerCode", pay.getCode());
        String checkoutKey = flowRepository
                .findByProviderIdAndServiceType(pay.getId(), ServiceTypeCode.PAYEMENT)
                .filter(PaymentFlow::isActif)
                .map(PaymentFlow::getBpmProcessKey)
                .filter(StringUtils::hasText)
                .orElse(DEFAULT_WALLET_CHECKOUT);
        vars.put("walletCheckoutProcessKey", checkoutKey);

        Map<String, Object> roles = copyRoleAssignments(readContext(tx));
        roles.put("PAYER", pay.getCode());
        if (billCode != null) {
            roles.putIfAbsent("BILLER", billCode);
        }
        vars.put("roleAssignments", roles);
        addEvent(tx.getPaymentId(), "PAY_PROVIDER_SELECTED",
                "Moyen de paiement wallet " + pay.getCode() + " (" + checkoutKey + ")");
    }

    /**
     * ACCOUNT_TRANSFER : {@code sourceRail} / {@code destRail} → roleAssignments.
     */
    @SuppressWarnings("unchecked")
    public void applyTransferRoleSelection(
            PaymentTransaction tx,
            Map<String, Object> data,
            Map<String, Object> vars
    ) {
        if (data == null || data.isEmpty()) {
            return;
        }
        String source = firstNonBlank(
                stringVal(data.get("sourceRail")),
                stringVal(data.get("sourceProvider")),
                stringVal(data.get("providerRail")),
                stringVal(data.get("SOURCE")));
        String dest = firstNonBlank(
                stringVal(data.get("destRail")),
                stringVal(data.get("destinationRail")),
                stringVal(data.get("destinationProvider")),
                stringVal(data.get("destProvider")),
                stringVal(data.get("DESTINATION")));
        String agent = firstNonBlank(
                stringVal(data.get("agentNetwork")),
                stringVal(data.get("agentRail")),
                stringVal(data.get("AGENT_NETWORK")));
        String remittanceProvider = firstNonBlank(
                stringVal(data.get("providerCode")),
                stringVal(data.get("remittanceProvider")),
                stringVal(data.get("PROVIDER")));
        if (!StringUtils.hasText(source) && !StringUtils.hasText(dest)
                && !StringUtils.hasText(agent) && !StringUtils.hasText(remittanceProvider)) {
            return;
        }
        Map<String, Object> roles = copyRoleAssignments(readContext(tx));
        if (StringUtils.hasText(source)) {
            String code = source.trim();
            requireProvider(code, "SOURCE_PROVIDER_NOT_FOUND", "Fournisseur source introuvable: ");
            roles.put("SOURCE", code);
            vars.put("sourceRail", code);
            vars.put("providerRail", code);
            addEvent(tx.getPaymentId(), "SOURCE_PROVIDER_SELECTED", "SOURCE=" + code);
        }
        if (StringUtils.hasText(dest)) {
            String code = dest.trim();
            requireProvider(code, "DEST_PROVIDER_NOT_FOUND", "Fournisseur destination introuvable: ");
            roles.put("DESTINATION", code);
            vars.put("destRail", code);
            vars.put("destinationRail", code);
            addEvent(tx.getPaymentId(), "DEST_PROVIDER_SELECTED", "DESTINATION=" + code);
        }
        if (StringUtils.hasText(agent)) {
            String code = agent.trim();
            PaymentProvider agentProvider = runtimeProviders.find(code).orElseThrow(() -> new BusinessException(
                    "AGENT_NETWORK_NOT_FOUND", "Réseau agent introuvable: " + code));
            roles.put("AGENT_NETWORK", code);
            vars.put("agentNetwork", code);
            // Si pas de source encore, l'agent réseau sert souvent aussi de wallet source (Wave→Wave).
            roles.putIfAbsent("SOURCE", code);
            Map<String, Object> ctx = readContext(tx);
            boolean cashIn = isCashIn(ctx);
            if (cashIn) {
                // Le compte destinataire a déjà été choisi : le réseau agent ne le remplace pas.
                roles.putIfAbsent("DESTINATION", code);
            } else {
                // Cash-out Omnicanal : DEST = réseau de remise (évite le défaut WAVE sans codeFse).
                roles.put("DESTINATION", code);
            }

            String agentCode = firstNonBlank(
                    stringVal(data.get("agentCode")),
                    stringVal(data.get("agentUniteCode")),
                    stringVal(data.get("uniteCode")));
            Map<String, Object> uniteCtx = agentNetworkUniteService.resolveAndValidate(code, agentCode, agentProvider);
            vars.putAll(uniteCtx);
            if (cashIn) {
                String kept = stringVal(ctx.get("destinationAccount"));
                Object agentAccount = uniteCtx.get("agentAccount");
                if (StringUtils.hasText(kept)
                        && (agentAccount == null || !kept.equals(String.valueOf(agentAccount)))) {
                    vars.put("destinationAccount", kept);
                    if (ctx.get("destinationAccountLabel") != null) {
                        vars.put("destinationAccountLabel", ctx.get("destinationAccountLabel"));
                    } else {
                        vars.remove("destinationAccountLabel");
                    }
                }
            }

            String detail = "AGENT_NETWORK=" + code
                    + (uniteCtx.get("agentCode") != null ? " unite=" + uniteCtx.get("agentCode") : "");
            addEvent(tx.getPaymentId(), "AGENT_NETWORK_SELECTED", detail);
        }
        if (StringUtils.hasText(remittanceProvider)) {
            String code = remittanceProvider.trim();
            requireProvider(code, "REMITTANCE_PROVIDER_NOT_FOUND", "Opérateur remittance introuvable: ");
            roles.put("PROVIDER", code);
            vars.put("providerCode", code);
            vars.put("remittanceProvider", code);
            addEvent(tx.getPaymentId(), "REMITTANCE_PROVIDER_SELECTED", "PROVIDER=" + code);
        }
        vars.put("roleAssignments", roles);
    }

    /**
     * Le rail (CORIS_MONEY, WAVE, …) reste le code de rôle. La fiche locale
     * n'existe plus : on vérifie qu'un fournisseur omnicanal correspond.
     */
    private void requireProvider(String code, String errorCode, String messagePrefix) {
        if (runtimeProviders.find(code).isEmpty()) {
            throw new BusinessException(errorCode, messagePrefix + code);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> copyRoleAssignments(Map<String, Object> ctx) {
        Object rolesObj = ctx.get("roleAssignments");
        if (rolesObj instanceof Map<?, ?> m) {
            return new LinkedHashMap<>((Map<String, Object>) m);
        }
        return new LinkedHashMap<>();
    }

    private Map<String, Object> readContext(PaymentTransaction tx) {
        if (tx.getContextJson() == null || tx.getContextJson().isBlank()) {
            return new LinkedHashMap<>();
        }
        try {
            return objectMapper.readValue(tx.getContextJson(), new TypeReference<>() {});
        } catch (Exception e) {
            return new LinkedHashMap<>();
        }
    }

    private void addEvent(String paymentId, String type, String message) {
        eventRepository.save(PaymentEvent.builder()
                .paymentId(paymentId)
                .eventType(type)
                .message(message)
                .createdAt(Instant.now())
                .build());
    }

    private static boolean isCashIn(Map<String, Object> ctx) {
        String product = firstNonBlank(
                stringVal(ctx.get("productCode")),
                stringVal(ctx.get("catalogServiceCode")));
        return "CASH_IN".equalsIgnoreCase(product);
    }

    private static String stringVal(Object v) {
        return v != null ? String.valueOf(v) : null;
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
