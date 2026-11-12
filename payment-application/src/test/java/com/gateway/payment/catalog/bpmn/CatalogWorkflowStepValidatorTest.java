package com.gateway.payment.catalog.bpmn;

import com.gateway.payment.bpm.ProviderCallOperationRegistry;
import com.gateway.payment.catalog.api.dto.CatalogWorkflowStepReport;
import com.gateway.payment.catalog.api.dto.CatalogWorkflowStepReport.Issue;
import com.gateway.payment.catalog.api.dto.CatalogWorkflowStepReport.StepView;
import com.gateway.payment.catalog.capability.CapabilityExecutionContext;
import com.gateway.payment.catalog.capability.CapabilityHandler;
import com.gateway.payment.catalog.capability.CapabilityRegistry;
import com.gateway.payment.catalog.capability.CapabilityResolver;
import com.gateway.payment.catalog.capability.CapabilityResult;
import com.gateway.payment.catalog.casting.CatalogProductCastingService;
import org.flowable.bpmn.model.Process;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CatalogWorkflowStepValidatorTest {

    private static final String LINK_PROCESS = """
            <serviceTask id="VALIDATE_ACCOUNT" flowable:delegateExpression="${catalogStepDelegate}"
                         pay:capability="VALIDATE_ACCOUNT"/>
            <serviceTask id="generateOtp" flowable:delegateExpression="${catalogStepDelegate}"
                         pay:capability="GENERATE_OTP" pay:actorRole="SOURCE"/>
            <serviceTask id="EXECUTE_LINK" flowable:delegateExpression="${catalogStepDelegate}"
                         pay:capability="EXECUTE_LINK" pay:actorRole="SOURCE" pay:operation="LINK"/>
            <serviceTask id="NOTIFY" flowable:delegateExpression="${catalogStepDelegate}"
                         pay:capability="NOTIFY"/>
            <userTask id="CONFIRM"/>
            """;

    private CatalogWorkflowStepValidator validator;

    @BeforeEach
    void setUp() {
        CapabilityRegistry registry = new CapabilityRegistry(List.of(
                handler("VALIDATE_ACCOUNT", null),
                handler("NOTIFY", null),
                handler("GENERATE_OTP", "GENERATE_OTP"),
                handler("EXECUTE_LINK", "LINK")
        ));
        validator = new CatalogWorkflowStepValidator(
                null,
                new CatalogProductCastingService(),
                new CapabilityResolver(),
                registry,
                new ProviderCallOperationRegistry());
    }

    @Test
    void link_process_with_rules_is_valid() {
        Map<String, Object> config = Map.of(
                "softRules", List.of(Map.of("step", "VALIDATE_ACCOUNT", "type", "REQUIRED_FIELD",
                        "params", Map.of("field", "phoneNumber"))),
                "bindings", List.of(Map.of("step", "CONFIRM", "kind", "UI", "formKey", "product-recap")));

        CatalogWorkflowStepReport report = validator.check("accountLink", BpmnTestModels.parse(LINK_PROCESS), config);

        assertTrue(report.valid(), () -> report.errors().toString());
        assertTrue(report.warnings().isEmpty(), () -> report.warnings().toString());
        StepView link = step(report, "EXECUTE_LINK");
        assertEquals("EXECUTE_LINK", link.capability());
        assertEquals("BPMN", link.capabilitySource());
        assertEquals("PROVIDER", link.category());
        assertEquals("SOURCE", link.actorRole());
        assertEquals("LINK", link.operation());
        assertEquals("LOCAL", step(report, "NOTIFY").category());
    }

    @Test
    void unknown_capability_and_role_are_errors() {
        Process process = BpmnTestModels.parse("""
                <serviceTask id="X" flowable:delegateExpression="${catalogStepDelegate}"
                             pay:capability="DOES_NOT_EXIST" pay:actorRole="NOBODY"/>
                """);

        CatalogWorkflowStepReport report = validator.check("p", process, Map.of());

        assertFalse(report.valid());
        assertTrue(codes(report.errors()).containsAll(List.of("CATALOG_CAPABILITY_UNKNOWN", "CATALOG_ACTOR_ROLE_UNKNOWN")));
    }

    @Test
    void catalog_binding_overrides_bpmn_capability() {
        Map<String, Object> config = Map.of("bindings", List.of(
                Map.of("step", "VALIDATE_ACCOUNT", "kind", "CAPABILITY", "capability", "NOTIFY")));

        CatalogWorkflowStepReport report = validator.check("accountLink", BpmnTestModels.parse(LINK_PROCESS), config);

        StepView validate = step(report, "VALIDATE_ACCOUNT");
        assertEquals("NOTIFY", validate.capability());
        assertEquals("CATALOG", validate.capabilitySource());
    }

    @Test
    void warns_on_ineffective_steps_and_unknown_targets() {
        Process process = BpmnTestModels.parse("""
                <serviceTask id="VALIDATE_ACCOUNT" flowable:delegateExpression="${catalogStepDelegate}"
                             pay:capability="VALIDATE_ACCOUNT"/>
                <serviceTask id="DO_SOMETHING" flowable:delegateExpression="${catalogStepDelegate}"/>
                """);
        Map<String, Object> config = Map.of(
                "channelDefaults", Map.of("MOBILE", Map.of(
                        "softRules", List.of(Map.of("step", "GHOST", "type", "REQUIRED_FIELD")))));

        CatalogWorkflowStepReport report = validator.check("p", process, config);

        assertTrue(report.valid());
        assertTrue(codes(report.warnings()).containsAll(List.of(
                "CATALOG_CONTROL_WITHOUT_RULES", "CATALOG_STEP_NO_EFFECT", "CATALOG_STEP_UNKNOWN")));
    }

    private static StepView step(CatalogWorkflowStepReport report, String id) {
        return report.steps().stream().filter(s -> s.id().equals(id)).findFirst().orElseThrow();
    }

    private static List<String> codes(List<Issue> issues) {
        return issues.stream().map(Issue::code).toList();
    }

    private static CapabilityHandler handler(String code, String defaultOperation) {
        return new CapabilityHandler() {
            @Override
            public String code() {
                return code;
            }

            @Override
            public String defaultOperation() {
                return defaultOperation;
            }

            @Override
            public CapabilityResult execute(CapabilityExecutionContext context, Map<String, Object> configuration) {
                throw new UnsupportedOperationException();
            }
        };
    }
}
