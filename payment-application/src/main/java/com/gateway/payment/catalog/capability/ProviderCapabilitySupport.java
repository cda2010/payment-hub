package com.gateway.payment.catalog.capability;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.payment.bpm.CatalogProviderInvoker;
import com.gateway.payment.bpm.ProviderCallOperationRegistry;
import com.gateway.payment.bpm.ProviderCallOperationSpec;
import com.gateway.payment.bpm.ProviderResponseContextMerger;
import com.gateway.payment.common.domain.PaymentStatus;
import com.gateway.payment.common.exception.BusinessException;
import com.gateway.payment.payment.domain.PaymentTransaction;
import com.gateway.payment.provider.ProviderExecutionResult;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Exécution provider/Omnicanal partagée par les capabilities INVOKE
 * ({@code PAY_BILL}, {@code EXECUTE_TRANSFER}, {@code GENERATE_OTP}, …).
 */
@Component
public class ProviderCapabilitySupport {

    private final ProviderCallOperationRegistry operationRegistry;
    private final CatalogProviderInvoker catalogProviderInvoker;
    private final ProviderResponseContextMerger responseContextMerger;
    private final ObjectMapper objectMapper;

    public ProviderCapabilitySupport(
            ProviderCallOperationRegistry operationRegistry,
            CatalogProviderInvoker catalogProviderInvoker,
            ProviderResponseContextMerger responseContextMerger,
            ObjectMapper objectMapper
    ) {
        this.operationRegistry = operationRegistry;
        this.catalogProviderInvoker = catalogProviderInvoker;
        this.responseContextMerger = responseContextMerger;
        this.objectMapper = objectMapper;
    }

    public CapabilityResult invoke(CapabilityExecutionContext exec, String defaultOperation) {
        PaymentTransaction tx = exec.transaction();
        if (tx == null) {
            throw new BusinessException("PAYMENT_NOT_FOUND", "Transaction requise pour capability provider");
        }
        Map<String, Object> context = exec.context();
        String operationCode = resolveOperation(exec, defaultOperation);
        ProviderCallOperationSpec spec = operationRegistry.resolve(operationCode);

        Map<String, Object> input = new HashMap<>();
        if (exec.input() != null) {
            input.putAll(exec.input());
        }
        Object otp = input.get("otp");
        if (otp == null) {
            otp = context.get("otp");
        }
        if (otp != null) {
            input.put("otp", otp);
            context.put("otp", otp);
        }

        String invokeProvider = catalogProviderInvoker.resolveProvider(
                tx, context, spec, exec.binding() != null ? exec.binding().actorRole() : null);
        context.put("invokeProviderCode", invokeProvider);
        context.put("catalogOperation", operationCode);

        String stepCode = exec.step() != null ? exec.step() : "SERVICE";
        tx.setCurrentStep(stepCode);
        List<CapabilityResult.CapabilityEvent> events = new ArrayList<>();
        if (operationRegistry.isOtpGeneration(operationCode)) {
            tx.applyStatus(PaymentStatus.OTP_GENERATION_PENDING);
            events.add(new CapabilityResult.CapabilityEvent(
                    "OTP_GENERATION", "Génération OTP via " + invokeProvider));
        } else {
            tx.applyStatus(PaymentStatus.PROCESSING);
            events.add(new CapabilityResult.CapabilityEvent(
                    "API_CALL", "Appel " + operationCode + " via " + invokeProvider));
        }

        ProviderExecutionResult result = catalogProviderInvoker.invokeForProvider(
                tx, invokeProvider, stepCode, operationCode, input, context);
        String rail = stringFromContext(context, "invocationRail");
        if ("OMNICANAL_TRANSFER".equals(rail)) {
            events.add(new CapabilityResult.CapabilityEvent(
                    "OMNICANAL_TRANSFER", "EXECUTE_TRANSFER via Omnicanal SpecDriven"));
        } else if ("OMNICANAL_OPERATION".equals(rail)) {
            events.add(new CapabilityResult.CapabilityEvent(
                    "OMNICANAL_OPERATION",
                    operationCode + " via InterfaceUtilisateur (" + invokeProvider + ")"));
        }

        if (result.isTimedOut()) {
            tx.applyStatus(PaymentStatus.UNKNOWN);
            tx.setErrorMessage("Timeout fournisseur — statut à vérifier");
            tx.setErrorCode("PROVIDER_TIMEOUT");
            throw new BusinessException("PROVIDER_TIMEOUT", tx.getErrorMessage());
        }
        if (result.getGatewayStatus() == PaymentStatus.OTP_REQUIRED
                || result.getGatewayStatus() == PaymentStatus.WAITING_CALLBACK) {
            tx.applyStatus(result.getGatewayStatus());
            String msg = result.getProviderMessage() != null ? result.getProviderMessage() : result.getGatewayStatus().name();
            events.add(new CapabilityResult.CapabilityEvent(result.getGatewayStatus().name(), msg));
            CapabilityResult.TerminalEffect effect = result.getGatewayStatus() == PaymentStatus.OTP_REQUIRED
                    ? CapabilityResult.TerminalEffect.OTP_REQUIRED
                    : CapabilityResult.TerminalEffect.WAITING_CALLBACK;
            return CapabilityResult.provider(events, Map.of(), effect, msg);
        }
        if (!result.isSuccess()) {
            tx.setErrorCode(result.getProviderStatus());
            String msg = result.getProviderMessage() != null ? result.getProviderMessage() : "Échec fournisseur";
            tx.setErrorMessage(msg);
            tx.setCompletedAt(Instant.now());
            tx.setCurrentStep("FAILURE");
            tx.applyStatus(PaymentStatus.FAILED);
            events.add(new CapabilityResult.CapabilityEvent("FAILED", msg));
            throw new BusinessException("PROVIDER_FAILED", msg);
        }

        Map<String, Object> vars = new LinkedHashMap<>();
        applySuccessResult(exec, tx, context, spec, result, otp, invokeProvider, events, vars);
        vars.put("providerSuccess", true);

        CapabilityResult.TerminalEffect effect = mapSuccessEffect(spec, context);
        return CapabilityResult.provider(events, vars, effect, null);
    }

    private String resolveOperation(CapabilityExecutionContext exec, String defaultOperation) {
        if (exec.bindingParams() != null) {
            Object fromParams = exec.bindingParams().get("operation");
            if (fromParams == null) {
                fromParams = exec.bindingParams().get("operationCode");
            }
            if (fromParams != null && StringUtils.hasText(String.valueOf(fromParams))) {
                return String.valueOf(fromParams).trim();
            }
        }
        if (exec.binding() != null && StringUtils.hasText(exec.binding().operation())) {
            return exec.binding().operation();
        }
        if (StringUtils.hasText(defaultOperation)) {
            return defaultOperation;
        }
        return operationRegistry.defaultOperationForActivity(exec.step());
    }

    private void applySuccessResult(
            CapabilityExecutionContext exec,
            PaymentTransaction tx,
            Map<String, Object> context,
            ProviderCallOperationSpec spec,
            ProviderExecutionResult result,
            Object otp,
            String invokeProvider,
            List<CapabilityResult.CapabilityEvent> events,
            Map<String, Object> vars
    ) {
        if (result.getProviderReference() != null) {
            if (spec.providerReferenceContextKey() != null) {
                context.put(spec.providerReferenceContextKey(), result.getProviderReference());
            }
            tx.setProviderReference(result.getProviderReference());
        }

        if (result.getResponseBody() != null) {
            responseContextMerger.merge(tx, context, spec.operation(), result.getResponseBody(), vars);
            if (vars.containsKey("amount")) {
                exec.setVar("amount", vars.get("amount"));
            }
        }

        boolean terminal = isTerminalOnConfirm(context);
        if (spec.retainOtpWhenNonTerminal() && !terminal) {
            if (otp != null) {
                context.put("otp", otp);
                vars.put("otp", otp);
                exec.setVar("otp", otp);
            }
        } else {
            context.remove("otp");
        }

        applySuccessHandler(tx, context, spec, invokeProvider, terminal, events);
        try {
            tx.setContextJson(objectMapper.writeValueAsString(context));
        } catch (Exception e) {
            tx.setContextJson("{}");
        }
    }

    private void applySuccessHandler(
            PaymentTransaction tx,
            Map<String, Object> context,
            ProviderCallOperationSpec spec,
            String invokeProvider,
            boolean terminalOnConfirm,
            List<CapabilityResult.CapabilityEvent> events
    ) {
        switch (spec.onSuccess()) {
            case OTP_REQUIRED -> events.add(new CapabilityResult.CapabilityEvent(
                    "OTP_GENERATED", "OTP généré"));
            case SUCCESS_TERMINAL -> {
                context.remove("otp");
                events.add(new CapabilityResult.CapabilityEvent(
                        "SUCCESS",
                        "PAY_BILL".equalsIgnoreCase(spec.operation())
                                ? "Facture acquittée"
                                : "Confirmation fournisseur"));
            }
            case CONFIRM_OR_CONTINUE -> {
                if (terminalOnConfirm) {
                    events.add(new CapabilityResult.CapabilityEvent(
                            "SUCCESS", "Confirmation fournisseur"));
                } else {
                    events.add(new CapabilityResult.CapabilityEvent(
                            "WALLET_DEBITED",
                            "Débit wallet OK — suite en attente (" + invokeProvider + ")"));
                }
            }
            case REDIRECT_OR_WAIT -> {
                if (context.get("redirectUrl") != null) {
                    events.add(new CapabilityResult.CapabilityEvent(
                            "REDIRECT", "Redirection fournisseur"));
                }
            }
            case BILL_LOOKED_UP -> events.add(new CapabilityResult.CapabilityEvent(
                    "BILL_LOOKED_UP", "Facture consultée"));
            case BILL_LISTED -> events.add(new CapabilityResult.CapabilityEvent(
                    "BILL_LISTED", "Liste de factures chargée"));
            case NONE -> {
                // no-op
            }
        }
    }

    private CapabilityResult.TerminalEffect mapSuccessEffect(
            ProviderCallOperationSpec spec,
            Map<String, Object> context
    ) {
        return switch (spec.onSuccess()) {
            case OTP_REQUIRED -> CapabilityResult.TerminalEffect.OTP_REQUIRED;
            case SUCCESS_TERMINAL -> CapabilityResult.TerminalEffect.SUCCESS;
            case CONFIRM_OR_CONTINUE -> isTerminalOnConfirm(context)
                    ? CapabilityResult.TerminalEffect.SUCCESS
                    : CapabilityResult.TerminalEffect.PROCESSING;
            case REDIRECT_OR_WAIT -> context.get("redirectUrl") != null
                    ? CapabilityResult.TerminalEffect.REDIRECT_REQUIRED
                    : CapabilityResult.TerminalEffect.WAITING_CALLBACK;
            case BILL_LOOKED_UP, BILL_LISTED, NONE -> CapabilityResult.TerminalEffect.NONE;
        };
    }

    private static boolean isTerminalOnConfirm(Map<String, Object> context) {
        Object v = context.get("terminalOnConfirm");
        if (v == null) {
            return true;
        }
        if (v instanceof Boolean b) {
            return b;
        }
        return !"false".equalsIgnoreCase(String.valueOf(v).trim());
    }

    private static String stringFromContext(Map<String, Object> context, String key) {
        if (context == null || context.get(key) == null) {
            return null;
        }
        return String.valueOf(context.get(key));
    }
}
