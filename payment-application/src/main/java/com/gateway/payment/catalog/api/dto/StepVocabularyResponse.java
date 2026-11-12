package com.gateway.payment.catalog.api.dto;

import java.util.List;

/**
 * Vocabulaire des attributs {@code pay:*} pour le concepteur BPMN.
 */
public record StepVocabularyResponse(
        String namespace,
        String prefix,
        String delegateExpression,
        List<CapabilityView> capabilities,
        List<OperationView> operations,
        List<ActorRoleView> actorRoles
) {

    /**
     * @param category {@code LOCAL} (contrôle / calcul interne), {@code PROVIDER} (appel partenaire)
     *                 ou {@code APPROVAL} (circuit de validation)
     */
    public record CapabilityView(
            String code,
            String label,
            String description,
            String category,
            String defaultOperation
    ) {}

    public record OperationView(String code, String rail, String onSuccess) {}

    public record ActorRoleView(String code, String label) {}
}
