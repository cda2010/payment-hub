package com.gateway.payment.catalog.capability;

import org.springframework.stereotype.Component;

/**
 * Capability {@code EXECUTE_CASHIN} — crédit destination cash-in via SpecDriven {@code TRANSFERT}.
 * <p>
 * Distinct de {@link ExecuteTransferCapabilityHandler} pour le routing BPMN / vocabulaire
 * concepteur ; même opération provider Omnicanal.
 */
@Component
public class ExecuteCashinCapabilityHandler extends AbstractProviderCapabilityHandler {

    public ExecuteCashinCapabilityHandler(ProviderCapabilitySupport support) {
        super(CapabilityCodes.EXECUTE_CASHIN, "TRANSFERT", support);
    }
}
