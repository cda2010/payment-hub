package com.gateway.payment.catalog.bpmn;

import org.flowable.bpmn.model.BaseElement;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.engine.delegate.DelegateExecution;
import org.springframework.util.StringUtils;

import java.util.Locale;

/**
 * Attributs {@code pay:*} portés par une tâche BPMN : ce que fait l'étape.
 * <pre>{@code
 * <serviceTask id="EXECUTE_SWIFT" flowable:delegateExpression="${catalogStepDelegate}"
 *              pay:capability="EXECUTE_TRANSFER" pay:actorRole="SOURCE"/>
 * }</pre>
 * Les paramètres métier (softRules, montants, frais, variantes canal) restent dans la
 * configuration catalogue, qui peut aussi surcharger ces attributs (workflow partagé).
 */
public record BpmnStepAttributes(String capability, String actorRole, String operation) {

    public static final String NAMESPACE = "http://payment-gateway/schema/bpmn/pay";
    public static final String PREFIX = "pay";
    public static final String ATTR_CAPABILITY = "capability";
    public static final String ATTR_ACTOR_ROLE = "actorRole";
    public static final String ATTR_OPERATION = "operation";

    public static final BpmnStepAttributes EMPTY = new BpmnStepAttributes(null, null, null);

    public BpmnStepAttributes {
        capability = normalize(capability);
        actorRole = normalize(actorRole);
        operation = normalize(operation);
    }

    public static BpmnStepAttributes of(BaseElement element) {
        if (element == null) {
            return EMPTY;
        }
        return new BpmnStepAttributes(
                element.getAttributeValue(NAMESPACE, ATTR_CAPABILITY),
                element.getAttributeValue(NAMESPACE, ATTR_ACTOR_ROLE),
                element.getAttributeValue(NAMESPACE, ATTR_OPERATION)
        );
    }

    public static BpmnStepAttributes of(DelegateExecution execution) {
        if (execution == null) {
            return EMPTY;
        }
        FlowElement element = execution.getCurrentFlowElement();
        return of(element);
    }

    public boolean isEmpty() {
        return capability == null && actorRole == null && operation == null;
    }

    private static String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim().toUpperCase(Locale.ROOT) : null;
    }
}
