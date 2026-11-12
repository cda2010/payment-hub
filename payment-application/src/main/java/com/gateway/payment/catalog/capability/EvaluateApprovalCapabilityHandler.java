package com.gateway.payment.catalog.capability;

import com.gateway.payment.approval.service.ApprovalEngine;
import com.gateway.payment.common.domain.PaymentStatus;
import com.gateway.payment.common.exception.BusinessException;
import com.gateway.payment.payment.domain.PaymentTransaction;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Capability {@code EVALUATE_APPROVAL} — démarre le circuit ApprovalEngine (DMN / niveaux).
 */
@Component
public class EvaluateApprovalCapabilityHandler implements CapabilityHandler {

    private final ApprovalEngine approvalEngine;

    public EvaluateApprovalCapabilityHandler(ApprovalEngine approvalEngine) {
        this.approvalEngine = approvalEngine;
    }

    @Override
    public String code() {
        return CapabilityCodes.EVALUATE_APPROVAL;
    }

    @Override
    public CapabilityResult execute(CapabilityExecutionContext context, Map<String, Object> configuration) {
        PaymentTransaction tx = context.transaction();
        if (tx == null) {
            throw new BusinessException("PAYMENT_NOT_FOUND", "Transaction requise pour EVALUATE_APPROVAL");
        }
        Map<String, Object> processVars = new HashMap<>();
        ApprovalEngine.ResolvedApproval resolved = approvalEngine.start(tx, context.context(), processVars);

        tx.setCurrentStep("EVALUATE_APPROVAL");
        tx.applyStatus(PaymentStatus.PROCESSING);

        Map<String, Object> vars = new LinkedHashMap<>(processVars);
        processVars.forEach(context::setVar);

        int levels = resolved.profile().levelCount();
        List<CapabilityResult.CapabilityEvent> events = new ArrayList<>();
        events.add(new CapabilityResult.CapabilityEvent(
                "APPROVAL_EVALUATED",
                "Profile " + resolved.profile().code() + " — " + levels + " niveau(x)"));

        return CapabilityResult.provider(events, vars, CapabilityResult.TerminalEffect.PROCESSING, null);
    }
}
