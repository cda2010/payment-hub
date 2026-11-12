package com.gateway.payment.catalog.bpmn;

import com.gateway.payment.bpm.ProviderCallOperationRegistry;
import com.gateway.payment.catalog.api.dto.CatalogWorkflowStepReport;
import com.gateway.payment.catalog.api.dto.CatalogWorkflowStepReport.Issue;
import com.gateway.payment.catalog.api.dto.CatalogWorkflowStepReport.StepView;
import com.gateway.payment.catalog.capability.CapabilityCatalogService;
import com.gateway.payment.catalog.capability.CapabilityCodes;
import com.gateway.payment.catalog.capability.CapabilityRegistry;
import com.gateway.payment.catalog.capability.CapabilityResolver;
import com.gateway.payment.catalog.casting.CatalogBindingDefinition;
import com.gateway.payment.catalog.casting.CatalogProductCasting;
import com.gateway.payment.catalog.casting.CatalogProductCastingService;
import com.gateway.payment.catalog.casting.CatalogProductRoleCode;
import com.gateway.payment.common.exception.BusinessException;
import org.flowable.bpmn.model.FlowNode;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.ServiceTask;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Vérifie que chaque serviceTask d'un process se résout vers une capability connue
 * et que la configuration catalogue ne vise que des étapes existantes.
 */
@Component
public class CatalogWorkflowStepValidator {

    private static final Set<String> CATALOG_DELEGATES = Set.of(
            CapabilityCatalogService.DELEGATE_EXPRESSION,
            "${gatewayCatalogStepDelegate}",
            "${gatewayProviderCallDelegate}",
            "${gatewayTransferApprovalEvaluateDelegate}"
    );

    private static final Set<String> RULE_ONLY_CAPABILITIES = Set.of(
            CapabilityCodes.VALIDATE_ACCOUNT,
            CapabilityCodes.VALIDATE_AMOUNT,
            CapabilityCodes.SOFT_RULES
    );

    private final CatalogWorkflowBindingService workflowBindingService;
    private final CatalogProductCastingService castingService;
    private final CapabilityResolver capabilityResolver;
    private final CapabilityRegistry capabilityRegistry;
    private final ProviderCallOperationRegistry operationRegistry;

    public CatalogWorkflowStepValidator(
            CatalogWorkflowBindingService workflowBindingService,
            CatalogProductCastingService castingService,
            CapabilityResolver capabilityResolver,
            CapabilityRegistry capabilityRegistry,
            ProviderCallOperationRegistry operationRegistry
    ) {
        this.workflowBindingService = workflowBindingService;
        this.castingService = castingService;
        this.capabilityResolver = capabilityResolver;
        this.capabilityRegistry = capabilityRegistry;
        this.operationRegistry = operationRegistry;
    }

    public CatalogWorkflowStepReport check(String workflowKey, Map<String, Object> configuration) {
        return check(workflowKey, workflowBindingService.requireProcessModel(workflowKey), configuration);
    }

    public void requireValid(String workflowKey, Map<String, Object> configuration) {
        CatalogWorkflowStepReport report = check(workflowKey, configuration);
        if (!report.valid()) {
            String detail = report.errors().stream()
                    .map(i -> i.step() + " : " + i.message())
                    .collect(Collectors.joining(" ; "));
            throw new BusinessException("CATALOG_WORKFLOW_INVALID",
                    "Process " + workflowKey + " invalide — " + detail);
        }
    }

    public CatalogWorkflowStepReport check(String workflowKey, Process process, Map<String, Object> configuration) {
        Map<String, Object> config = configuration != null ? configuration : Map.of();
        CatalogProductCasting casting = castingService.parse(config);
        Set<String> softRuleSteps = upper(stepsOf(config, "softRules"));
        Set<String> activityIds = upper(process.findFlowElementsOfType(FlowNode.class, true).stream()
                .map(FlowNode::getId)
                .toList());

        List<StepView> steps = new ArrayList<>();
        List<Issue> errors = new ArrayList<>();
        List<Issue> warnings = new ArrayList<>();

        for (ServiceTask task : process.findFlowElementsOfType(ServiceTask.class, true)) {
            String id = task.getId();
            String delegate = task.getImplementation();
            BpmnStepAttributes attributes = BpmnStepAttributes.of(task);
            CatalogBindingDefinition catalogBinding = casting.findBinding(id, null).orElse(null);
            CatalogBindingDefinition binding = CatalogStepBindings.merge(catalogBinding, attributes, id);
            boolean hasRules = softRuleSteps.contains(id.toUpperCase(Locale.ROOT));

            String declared = binding != null && StringUtils.hasText(binding.capability())
                    ? binding.capability().trim().toUpperCase(Locale.ROOT)
                    : null;
            if (declared != null && !capabilityRegistry.has(declared)) {
                errors.add(new Issue("CATALOG_CAPABILITY_UNKNOWN", id, "capability inconnue : " + declared));
            }
            if (attributes.actorRole() != null && !CatalogProductRoleCode.isKnown(attributes.actorRole())) {
                errors.add(new Issue("CATALOG_ACTOR_ROLE_UNKNOWN", id, "pay:actorRole inconnu : " + attributes.actorRole()));
            }
            String operation = binding != null ? binding.operation() : null;
            if (attributes.operation() != null && !operationRegistry.isRegistered(attributes.operation())) {
                warnings.add(new Issue("CATALOG_OPERATION_UNKNOWN", id,
                        "pay:operation absente du registre des opérations : " + attributes.operation()));
            }

            Optional<String> resolved = capabilityResolver.resolve(id, binding).filter(capabilityRegistry::has);
            String source = declared == null
                    ? (resolved.isPresent() ? "INFERRED" : "NONE")
                    : (catalogBinding != null && StringUtils.hasText(catalogBinding.capability()) ? "CATALOG" : "BPMN");
            String category = resolved.flatMap(capabilityRegistry::get)
                    .map(CapabilityCatalogService::category)
                    .orElse(null);

            if (delegate != null && CATALOG_DELEGATES.contains(delegate.trim())) {
                if (resolved.isEmpty() && !hasRules) {
                    warnings.add(new Issue("CATALOG_STEP_NO_EFFECT", id,
                            "aucune capability ni softRule : l'étape ne fait rien"));
                } else if (resolved.isPresent() && RULE_ONLY_CAPABILITIES.contains(resolved.get()) && !hasRules) {
                    warnings.add(new Issue("CATALOG_CONTROL_WITHOUT_RULES", id,
                            resolved.get() + " sans softRule pour cette étape : aucun contrôle effectué"));
                }
            }

            steps.add(new StepView(
                    id,
                    task.getName(),
                    delegate,
                    resolved.orElse(declared),
                    source,
                    category,
                    binding != null ? binding.actorRole() : null,
                    operation,
                    hasRules
            ));
        }

        for (CatalogBindingDefinition b : casting.bindings()) {
            warnIfUnknownStep(b.step(), "binding", activityIds, warnings);
        }
        for (String step : stepsOf(config, "softRules")) {
            warnIfUnknownStep(step, "softRule", activityIds, warnings);
        }
        for (String step : stepsOf(config, "bindings")) {
            if (casting.bindings().stream().noneMatch(b -> step.equals(b.step()))) {
                warnIfUnknownStep(step, "binding (canal)", activityIds, warnings);
            }
        }

        return new CatalogWorkflowStepReport(workflowKey, steps, List.copyOf(errors), dedupe(warnings));
    }

    private static void warnIfUnknownStep(String step, String origin, Set<String> activityIds, List<Issue> warnings) {
        if (StringUtils.hasText(step) && !activityIds.contains(step.trim().toUpperCase(Locale.ROOT))) {
            warnings.add(new Issue("CATALOG_STEP_UNKNOWN", step,
                    origin + " vise une étape absente du process"));
        }
    }

    /** Étapes visées par {@code key} à la racine et dans {@code channelDefaults.<CANAL>}. */
    @SuppressWarnings("unchecked")
    private static List<String> stepsOf(Map<String, Object> config, String key) {
        List<String> out = new ArrayList<>();
        collectSteps(config.get(key), out);
        if (config.get("channelDefaults") instanceof Map<?, ?> channels) {
            for (Object overlay : channels.values()) {
                if (overlay instanceof Map<?, ?> m) {
                    collectSteps(((Map<String, Object>) m).get(key), out);
                }
            }
        }
        return out;
    }

    private static void collectSteps(Object raw, List<String> out) {
        if (raw instanceof List<?> list) {
            for (Object item : list) {
                if (item instanceof Map<?, ?> m && m.get("step") != null) {
                    out.add(String.valueOf(m.get("step")).trim());
                }
            }
        }
    }

    private static Set<String> upper(List<String> values) {
        Set<String> out = new LinkedHashSet<>();
        for (String v : values) {
            if (v != null) {
                out.add(v.trim().toUpperCase(Locale.ROOT));
            }
        }
        return out;
    }

    private static List<Issue> dedupe(List<Issue> issues) {
        return List.copyOf(new LinkedHashSet<>(issues));
    }
}
