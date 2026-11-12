package com.gateway.payment.identity.enrolement;

import java.util.List;
import java.util.Map;

/** Workflow publié et formulaires que le mobile doit afficher. */
public record WorkflowEnrolement(
        String workflow,
        List<EcranEnrolement> ecrans,
        Map<String, Map<String, EcranEnrolement>> variantes
) {
}
