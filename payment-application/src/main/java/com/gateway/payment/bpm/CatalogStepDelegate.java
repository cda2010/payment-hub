package com.gateway.payment.bpm;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.payment.catalog.bpmn.BpmnStepAttributes;
import com.gateway.payment.catalog.bpmn.CatalogStepBindings;
import com.gateway.payment.catalog.capability.CapabilityCodes;
import com.gateway.payment.catalog.capability.CapabilityHandler;
import com.gateway.payment.catalog.capability.CapabilityRegistry;
import com.gateway.payment.catalog.capability.CapabilityResolver;
import com.gateway.payment.catalog.casting.CatalogBindingDefinition;
import com.gateway.payment.catalog.casting.CatalogUiBindingResolver;
import com.gateway.payment.common.exception.BusinessException;
import com.gateway.payment.payment.domain.PaymentTransaction;
import com.gateway.payment.payment.repository.PaymentTransactionRepository;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Délégué unique des serviceTasks produit : {@code flowable:delegateExpression="${catalogStepDelegate}"}.
 * <p>
 * Résout la capability de l'étape (binding catalogue, sinon {@code pay:capability}) puis exécute
 * le chemin adapté : appel fournisseur, évaluation d'approbation ou étape locale.
 */
@Component("catalogStepDelegate")
public class CatalogStepDelegate implements JavaDelegate {

    private final PaymentTransactionRepository transactionRepository;
    private final ObjectMapper objectMapper;
    private final CatalogUiBindingResolver uiBindingResolver;
    private final CapabilityResolver capabilityResolver;
    private final CapabilityRegistry capabilityRegistry;
    private final CatalogProductStepDelegate localStepDelegate;
    private final GatewayProviderCallDelegate providerCallDelegate;
    private final TransferApprovalEvaluateDelegate approvalDelegate;

    public CatalogStepDelegate(
            PaymentTransactionRepository transactionRepository,
            ObjectMapper objectMapper,
            CatalogUiBindingResolver uiBindingResolver,
            CapabilityResolver capabilityResolver,
            CapabilityRegistry capabilityRegistry,
            CatalogProductStepDelegate localStepDelegate,
            GatewayProviderCallDelegate providerCallDelegate,
            TransferApprovalEvaluateDelegate approvalDelegate
    ) {
        this.transactionRepository = transactionRepository;
        this.objectMapper = objectMapper;
        this.uiBindingResolver = uiBindingResolver;
        this.capabilityResolver = capabilityResolver;
        this.capabilityRegistry = capabilityRegistry;
        this.localStepDelegate = localStepDelegate;
        this.providerCallDelegate = providerCallDelegate;
        this.approvalDelegate = approvalDelegate;
    }

    @Override
    public void execute(DelegateExecution execution) {
        switch (route(execution)) {
            case PROVIDER -> providerCallDelegate.execute(execution);
            case APPROVAL -> approvalDelegate.execute(execution);
            case LOCAL -> localStepDelegate.execute(execution);
        }
    }

    enum Route { LOCAL, PROVIDER, APPROVAL }

    Route route(DelegateExecution execution) {
        String step = execution.getCurrentActivityId();
        BpmnStepAttributes attributes = BpmnStepAttributes.of(execution);
        if (attributes.capability() != null && !capabilityRegistry.has(attributes.capability())) {
            throw new BusinessException(
                    "CATALOG_CAPABILITY_UNKNOWN",
                    "pay:capability inconnue: " + attributes.capability() + " (step=" + step + ")");
        }

        CatalogBindingDefinition binding = CatalogStepBindings.merge(
                uiBindingResolver.findInvokeBinding(readContext(execution), step).orElse(null),
                attributes,
                step);
        Optional<String> code = capabilityResolver.resolve(step, binding);
        if (code.isEmpty()) {
            return Route.LOCAL;
        }
        if (CapabilityCodes.EVALUATE_APPROVAL.equals(code.get())) {
            return Route.APPROVAL;
        }
        boolean callsProvider = capabilityRegistry.get(code.get())
                .map(CapabilityHandler::defaultOperation)
                .isPresent();
        return callsProvider ? Route.PROVIDER : Route.LOCAL;
    }

    private Map<String, Object> readContext(DelegateExecution execution) {
        Map<String, Object> context = new HashMap<>();
        Object paymentId = execution.getVariable(PaymentProcessSupport.VAR_PAYMENT_ID);
        if (paymentId != null) {
            Optional<PaymentTransaction> tx = transactionRepository.findByPaymentId(String.valueOf(paymentId));
            String json = tx.map(PaymentTransaction::getContextJson).orElse(null);
            if (json != null && !json.isBlank()) {
                try {
                    context.putAll(objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {}));
                } catch (Exception ignored) {
                    // contexte illisible : on route sur le BPMN seul
                }
            }
        }
        execution.getVariables().forEach((k, v) -> {
            if (v != null && !"otp".equalsIgnoreCase(k)) {
                context.put(k, v);
            }
        });
        return context;
    }
}
