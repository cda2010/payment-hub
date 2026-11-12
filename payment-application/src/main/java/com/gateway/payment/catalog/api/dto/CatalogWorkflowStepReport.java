package com.gateway.payment.catalog.api.dto;

import java.util.List;

/**
 * Contrôle des serviceTasks d'un process face à la configuration catalogue.
 * Les {@code errors} bloquent la publication ; les {@code warnings} sont informatifs.
 */
public record CatalogWorkflowStepReport(
        String workflowKey,
        List<StepView> steps,
        List<Issue> errors,
        List<Issue> warnings
) {

    public boolean valid() {
        return errors.isEmpty();
    }

    /**
     * @param capabilitySource {@code CATALOG} (surcharge binding), {@code BPMN} ({@code pay:capability}),
     *                         {@code INFERRED} (opération ou nom d'étape) ou {@code NONE}
     */
    public record StepView(
            String id,
            String name,
            String delegateExpression,
            String capability,
            String capabilitySource,
            String category,
            String actorRole,
            String operation,
            boolean hasSoftRules
    ) {}

    public record Issue(String code, String step, String message) {}
}
