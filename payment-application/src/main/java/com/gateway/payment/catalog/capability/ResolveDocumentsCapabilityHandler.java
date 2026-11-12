package com.gateway.payment.catalog.capability;

import com.gateway.payment.common.domain.PaymentStatus;
import com.gateway.payment.document.service.DocumentEngine;
import com.gateway.payment.payment.domain.PaymentTransaction;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Capability {@code RESOLVE_DOCUMENTS} — DMN montant/provider → profil de pièces.
 */
@Component
public class ResolveDocumentsCapabilityHandler implements CapabilityHandler {

    private final DocumentEngine documentEngine;

    public ResolveDocumentsCapabilityHandler(DocumentEngine documentEngine) {
        this.documentEngine = documentEngine;
    }

    @Override
    public String code() {
        return CapabilityCodes.RESOLVE_DOCUMENTS;
    }

    @Override
    public CapabilityResult execute(CapabilityExecutionContext context, Map<String, Object> configuration) {
        PaymentTransaction tx = context.transaction();
        Map<String, Object> processVars = new HashMap<>();
        DocumentEngine.ResolvedDocuments resolved = documentEngine.resolve(tx, context.context(), processVars);

        if (tx != null) {
            tx.setCurrentStep("RESOLVE_DOCUMENTS");
            tx.applyStatus(PaymentStatus.PROCESSING);
        }

        Map<String, Object> vars = new LinkedHashMap<>(processVars);
        processVars.forEach(context::setVar);

        boolean required = resolved.profile().requiresDocuments();
        List<CapabilityResult.CapabilityEvent> events = new ArrayList<>();
        events.add(new CapabilityResult.CapabilityEvent(
                "DOCUMENTS_RESOLVED",
                "Profile " + resolved.profile().code()
                        + (required ? " — documents requis" : " — aucun document")));

        return CapabilityResult.provider(events, vars, CapabilityResult.TerminalEffect.PROCESSING, null);
    }
}
