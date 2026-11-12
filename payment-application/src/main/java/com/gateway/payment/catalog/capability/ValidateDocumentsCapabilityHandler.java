package com.gateway.payment.catalog.capability;

import com.gateway.payment.document.service.DocumentEngine;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Capability {@code VALIDATE_DOCUMENTS} — vérifie que les pièces requises sont présentes.
 */
@Component
public class ValidateDocumentsCapabilityHandler implements CapabilityHandler {

    private final DocumentEngine documentEngine;

    public ValidateDocumentsCapabilityHandler(DocumentEngine documentEngine) {
        this.documentEngine = documentEngine;
    }

    @Override
    public String code() {
        return CapabilityCodes.VALIDATE_DOCUMENTS;
    }

    @Override
    public CapabilityResult execute(CapabilityExecutionContext context, Map<String, Object> configuration) {
        documentEngine.validate(context.context());
        return CapabilityResult.provider(
                List.of(new CapabilityResult.CapabilityEvent(
                        "DOCUMENTS_VALIDATED", "Pièces justificatives OK")),
                Map.of(),
                CapabilityResult.TerminalEffect.NONE,
                null);
    }
}
