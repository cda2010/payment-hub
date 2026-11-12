package com.gateway.payment.omnicanal.policy;

import com.gateway.payment.orchestration.domain.StepErrorPolicy;
import com.gateway.payment.orchestration.repository.StepErrorPolicyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * Résolution d'erreur (miroir Omnicanal) :
 * règle step ({@code error_policy_code}) → règle schéma → défauts workflow / criticité.
 */
@Service
public class ErrorPolicyResolver {

    private static final Logger log = LoggerFactory.getLogger(ErrorPolicyResolver.class);

    private final StepErrorPolicyRepository stepErrorPolicyRepository;

    public ErrorPolicyResolver(StepErrorPolicyRepository stepErrorPolicyRepository) {
        this.stepErrorPolicyRepository = stepErrorPolicyRepository;
    }

    public ErrorDecision resolve(ErrorResolveRequest request) {
        ErrorResolveRequest req = request != null ? request : ErrorResolveRequest.builder().build();
        ErrorType type = ErrorClassifier.classifyType(
                req.orchErrorCode(), req.causeErrCode(), req.message());
        StepCriticality criticality = req.criticality() != null
                ? req.criticality()
                : StepCriticality.REQUIRED;
        WorkflowErrorPolicy workflow = req.workflowPolicy() != null
                ? req.workflowPolicy()
                : WorkflowErrorPolicy.globalDefault();

        ErrorDecision decision = null;

        if (StringUtils.hasText(req.stepErrorPolicyCode())) {
            Optional<StepErrorPolicy> stepRule = stepErrorPolicyRepository
                    .findActiveStepRule(req.stepErrorPolicyCode().trim(), type.name());
            if (stepRule.isPresent()) {
                decision = fromEntity(stepRule.get(), type, criticality, "STEP",
                        "Règle step code=" + req.stepErrorPolicyCode());
            }
        }

        if (decision == null && StringUtils.hasText(req.schemaErrorPolicyCode())
                && req.schemaEcritureId() != null) {
            Optional<StepErrorPolicy> schemaRule = stepErrorPolicyRepository.findActiveSchemaRule(
                    req.schemaErrorPolicyCode().trim(), type.name(), req.schemaEcritureId());
            if (schemaRule.isPresent()) {
                decision = fromEntity(schemaRule.get(), type, criticality, "SCHEMA",
                        "Règle schéma code=" + req.schemaErrorPolicyCode());
            }
        }

        if (decision == null && StringUtils.hasText(req.stepErrorPolicyCode())
                && req.schemaEcritureId() != null) {
            Optional<StepErrorPolicy> schemaScoped = stepErrorPolicyRepository.findActiveSchemaRule(
                    req.stepErrorPolicyCode().trim(), type.name(), req.schemaEcritureId());
            if (schemaScoped.isPresent()) {
                decision = fromEntity(schemaScoped.get(), type, criticality, "SCHEMA",
                        "Règle schéma scoped stepCode");
            }
        }

        if (decision == null) {
            decision = fromGlobal(type, criticality, req.stepDefaultAction(), workflow);
        }

        decision = enforceCriticality(decision, criticality, workflow);

        if (type == ErrorType.UNKNOWN && workflow.requireManualReviewOnUnknown()
                && decision.action() != ErrorAction.MANUAL_REVIEW
                && decision.action() != ErrorAction.ESCALATE) {
            decision = ErrorDecision.of(
                    type,
                    ErrorAction.MANUAL_REVIEW,
                    decision.maxRetries(),
                    decision.retryDelayMs(),
                    decision.targetStepCode(),
                    criticality,
                    true,
                    false,
                    decision.ruleSource() + "+UNKNOWN_OVERRIDE",
                    decision.ruleCode(),
                    "UNKNOWN → MANUAL_REVIEW"
            );
        }

        log.info(
                "ErrorDecision stepKey={} type={} action={} source={} criticality={} compensate={}",
                req.stepKey(), decision.errorType(), decision.action(),
                decision.ruleSource(), decision.criticality(), decision.compensate()
        );
        return decision;
    }

    private static ErrorDecision fromEntity(
            StepErrorPolicy rule,
            ErrorType type,
            StepCriticality criticality,
            String source,
            String rationale
    ) {
        ErrorAction action = ErrorAction.fromCode(rule.getAction());
        boolean compensate = action == ErrorAction.COMPENSATE
                || (criticality.prefersStopOrCompensate() && action.triggersCompensation());
        return ErrorDecision.of(
                type,
                action,
                rule.getMaxRetries(),
                rule.getRetryDelayMs(),
                rule.getTargetStepCode(),
                criticality,
                action != ErrorAction.CONTINUE,
                compensate,
                source,
                rule.getCode(),
                rationale
        );
    }

    private static ErrorDecision fromGlobal(
            ErrorType type,
            StepCriticality criticality,
            ErrorAction stepDefault,
            WorkflowErrorPolicy workflow
    ) {
        ErrorAction action;
        String rationale;
        if (stepDefault != null) {
            action = stepDefault.normalized();
            rationale = "Défaut step defaultErrorAction=" + action;
        } else if (type == ErrorType.BUSINESS_REJECTED && workflow.stopOnBusinessError()) {
            action = workflow.compensateCompletedCriticalSteps() && criticality.prefersStopOrCompensate()
                    ? ErrorAction.COMPENSATE
                    : ErrorAction.STOP_WORKFLOW;
            rationale = "Workflow stopOnBusinessError";
        } else if (type == ErrorType.TRANSIENT || type == ErrorType.TIMEOUT) {
            if (workflow.compensateCompletedCriticalSteps() && criticality.prefersStopOrCompensate()) {
                action = ErrorAction.COMPENSATE;
                rationale = "Timeout/transient on critical step → compensate completed legs";
            } else {
                action = ErrorAction.RETRY;
                rationale = "Transient/timeout → RETRY";
            }
        } else if (type == ErrorType.TECHNICAL
                && workflow.compensateCompletedCriticalSteps()
                && criticality.prefersStopOrCompensate()) {
            action = ErrorAction.COMPENSATE;
            rationale = "Technical error on critical step → compensate completed legs";
        } else {
            action = workflow.defaultAction().normalized();
            rationale = "Workflow defaultAction=" + action;
        }
        boolean compensate = action == ErrorAction.COMPENSATE
                || (workflow.compensateCompletedCriticalSteps()
                && criticality == StepCriticality.CRITICAL
                && (type == ErrorType.BUSINESS_REJECTED
                        || type == ErrorType.TIMEOUT
                        || type == ErrorType.TRANSIENT
                        || type == ErrorType.TECHNICAL));
        return ErrorDecision.of(
                type, action, null, null, null, criticality,
                action != ErrorAction.CONTINUE, compensate, "GLOBAL", "workflow", rationale
        );
    }

    private static ErrorDecision enforceCriticality(
            ErrorDecision decision,
            StepCriticality criticality,
            WorkflowErrorPolicy workflow
    ) {
        if (decision.action() != ErrorAction.CONTINUE) {
            return decision;
        }
        if (criticality.allowsContinue() && workflow.allowPartialSuccess()) {
            return decision;
        }
        ErrorAction replacement = criticality.prefersStopOrCompensate()
                && workflow.compensateCompletedCriticalSteps()
                ? ErrorAction.COMPENSATE
                : ErrorAction.STOP_WORKFLOW;
        return ErrorDecision.of(
                decision.errorType(), replacement, decision.maxRetries(), decision.retryDelayMs(),
                decision.targetStepCode(), criticality, true,
                replacement == ErrorAction.COMPENSATE,
                decision.ruleSource() + "+CRITICALITY", decision.ruleCode(),
                "CONTINUE interdit pour criticality=" + criticality + " → " + replacement
        );
    }
}
