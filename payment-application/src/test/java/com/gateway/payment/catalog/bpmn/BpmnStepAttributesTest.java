package com.gateway.payment.catalog.bpmn;

import com.gateway.payment.catalog.casting.CatalogBindingDefinition;
import com.gateway.payment.catalog.casting.CatalogBindingKind;
import com.gateway.payment.catalog.casting.CatalogFormSource;
import org.flowable.bpmn.model.Process;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BpmnStepAttributesTest {

    @Test
    void reads_pay_attributes_from_parsed_service_task() {
        Process process = BpmnTestModels.parse("""
                <serviceTask id="EXECUTE_LINK" flowable:delegateExpression="${catalogStepDelegate}"
                             pay:capability="execute_link" pay:actorRole=" source " pay:operation="LINK"/>
                <serviceTask id="PLAIN" flowable:delegateExpression="${catalogStepDelegate}"/>
                """);

        BpmnStepAttributes link = BpmnStepAttributes.of(process.getFlowElement("EXECUTE_LINK"));
        assertEquals("EXECUTE_LINK", link.capability());
        assertEquals("SOURCE", link.actorRole());
        assertEquals("LINK", link.operation());

        assertTrue(BpmnStepAttributes.of(process.getFlowElement("PLAIN")).isEmpty());
    }

    @Test
    void blank_values_are_ignored() {
        assertTrue(new BpmnStepAttributes(" ", "", null).isEmpty());
    }
}

class CatalogStepBindingsTest {

    private static CatalogBindingDefinition binding(String actorRole, String operation, String capability) {
        return new CatalogBindingDefinition("STEP", CatalogBindingKind.INVOKE, actorRole, "WAVE", null,
                CatalogFormSource.FORM_KEY, null, operation, capability, Map.of("k", "v"));
    }

    @Test
    void empty_bpmn_keeps_catalog_binding() {
        CatalogBindingDefinition catalog = binding("SOURCE", "LINK", null);
        assertSame(catalog, CatalogStepBindings.merge(catalog, BpmnStepAttributes.EMPTY, "STEP"));
        assertNull(CatalogStepBindings.merge(null, BpmnStepAttributes.EMPTY, "STEP"));
    }

    @Test
    void bpmn_alone_builds_capability_binding() {
        CatalogBindingDefinition merged = CatalogStepBindings.merge(
                null, new BpmnStepAttributes("GENERATE_OTP", "SOURCE", null), "generateOtp");
        assertEquals("generateOtp", merged.step());
        assertEquals(CatalogBindingKind.CAPABILITY, merged.kind());
        assertEquals("GENERATE_OTP", merged.capability());
        assertEquals("SOURCE", merged.actorRole());
        assertNull(merged.operation());
    }

    @Test
    void catalog_capability_overrides_bpmn() {
        CatalogBindingDefinition merged = CatalogStepBindings.merge(
                binding(null, null, "GET_INFOS"),
                new BpmnStepAttributes("VALIDATE_ACCOUNT", "DESTINATION", "GETINFOS"),
                "STEP");
        assertEquals("GET_INFOS", merged.capability());
        assertEquals("DESTINATION", merged.actorRole());
        assertEquals("GETINFOS", merged.operation());
        assertEquals("WAVE", merged.providerCode());
        assertEquals(Map.of("k", "v"), merged.params());
    }

    @Test
    void bpmn_capability_replaces_legacy_catalog_operation() {
        CatalogBindingDefinition merged = CatalogStepBindings.merge(
                binding("DESTINATION", "VALIDATE_ACCOUNT", null),
                new BpmnStepAttributes("VALIDATE_ACCOUNT", null, null),
                "STEP");
        assertEquals("VALIDATE_ACCOUNT", merged.capability());
        assertNull(merged.operation());
        assertEquals("DESTINATION", merged.actorRole());
    }

    @Test
    void bpmn_without_capability_fills_blanks_only() {
        CatalogBindingDefinition merged = CatalogStepBindings.merge(
                binding(null, "TRANSFERT", null),
                new BpmnStepAttributes(null, "SOURCE", "LINK"),
                "STEP");
        assertNull(merged.capability());
        assertEquals("SOURCE", merged.actorRole());
        assertEquals("TRANSFERT", merged.operation());
    }
}
