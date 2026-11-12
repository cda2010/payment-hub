package com.gateway.payment.catalog.capability;

import com.gateway.payment.bc.PricingBcClient;
import com.gateway.payment.catalog.casting.CatalogBindingDefinition;
import com.gateway.payment.catalog.casting.CatalogBindingKind;
import com.gateway.payment.catalog.casting.CatalogFormSource;
import com.gateway.payment.softrules.CatalogSoftRuleEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CapabilityResolverTest {

    private CapabilityResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new CapabilityResolver();
    }

    @Test
    void prefers_binding_capability() {
        CatalogBindingDefinition binding = new CatalogBindingDefinition(
                "FOO", CatalogBindingKind.CAPABILITY, null, null, null,
                CatalogFormSource.FORM_KEY, null, null, "NOTIFY", Map.of());
        assertEquals("NOTIFY", resolver.resolve("CALCULATE_FEES", binding).orElseThrow());
    }

    @Test
    void infers_provider_capabilities() {
        assertEquals("GET_BALANCE", resolver.inferFromStep("CHECK_BALANCE").orElseThrow());
        assertEquals("GENERATE_OTP", resolver.inferFromStep("generateOtp").orElseThrow());
        assertEquals("GET_BILLS", resolver.inferFromStep("lookupBill").orElseThrow());
        assertEquals("EXECUTE_TRANSFER", resolver.inferFromStep("EXECUTE_RTGS").orElseThrow());
        assertEquals("EXECUTE_CASHOUT", resolver.inferFromStep("EXECUTE_CASHOUT").orElseThrow());
        assertEquals("EXECUTE_CASHIN", resolver.inferFromStep("EXECUTE_CASHIN").orElseThrow());
        assertEquals("EVALUATE_APPROVAL", resolver.inferFromStep("evaluateApproval").orElseThrow());
    }

    @Test
    void resolve_from_binding_operation() {
        CatalogBindingDefinition binding = new CatalogBindingDefinition(
                "payBill", CatalogBindingKind.INVOKE, "BILLER", null, null,
                CatalogFormSource.FORM_KEY, null, "PAY_BILL", null, Map.of());
        assertEquals("PAY_BILL", resolver.resolve("payBill", binding).orElseThrow());
    }

    /** Un contrôle local ne doit pas partir chez le fournisseur (GETINFOS). */
    @Test
    void soft_binding_stays_local() {
        CatalogBindingDefinition binding = new CatalogBindingDefinition(
                "VALIDATE_ACCOUNT", CatalogBindingKind.SOFT, "DESTINATION", null, null,
                CatalogFormSource.FORM_KEY, null, "VALIDATE_ACCOUNT", null, Map.of());
        assertEquals("VALIDATE_ACCOUNT", resolver.resolve("VALIDATE_ACCOUNT", binding).orElseThrow());
    }

    @Test
    void invoke_binding_still_calls_provider() {
        CatalogBindingDefinition binding = new CatalogBindingDefinition(
                "VALIDATE_ACCOUNT", CatalogBindingKind.INVOKE, "DESTINATION", null, null,
                CatalogFormSource.FORM_KEY, null, "VALIDATE_ACCOUNT", null, Map.of());
        assertEquals("GET_INFOS", resolver.resolve("VALIDATE_ACCOUNT", binding).orElseThrow());
    }
}

class CalculateFeesCapabilityHandlerTest {

    @Test
    void defaults_fees_to_zero_when_missing() {
        SoftRuleCapabilitySupport support = new SoftRuleCapabilitySupport(CatalogSoftRuleEngine.withBuiltins());
        PricingBcClient pricing = mock(PricingBcClient.class);
        when(pricing.enabled()).thenReturn(false);
        CalculateFeesCapabilityHandler handler = new CalculateFeesCapabilityHandler(support, pricing);
        Map<String, Object> context = new HashMap<>();
        context.put("amount", 1000);
        CapabilityResult result = handler.execute(
                CapabilityExecutionContext.local("p1", "CALCULATE_FEES", null, context, Map.of()),
                Map.of());
        assertTrue(result.handled());
        assertEquals(0, context.get("fees"));
        assertEquals(0, result.executionVars().get("fees"));
    }

    @Test
    void applies_default_fee_soft_rule() {
        SoftRuleCapabilitySupport support = new SoftRuleCapabilitySupport(CatalogSoftRuleEngine.withBuiltins());
        PricingBcClient pricing = mock(PricingBcClient.class);
        when(pricing.enabled()).thenReturn(false);
        CalculateFeesCapabilityHandler handler = new CalculateFeesCapabilityHandler(support, pricing);
        Map<String, Object> context = new HashMap<>();
        context.put("amount", 5000);
        context.put("catalogConfiguration", Map.of(
                "defaultFee", 100,
                "softRules", List.of(Map.of(
                        "step", "CALCULATE_FEES",
                        "type", "DEFAULT_FEE",
                        "params", Map.of()
                ))
        ));
        CapabilityResult result = handler.execute(
                CapabilityExecutionContext.local("p1", "CALCULATE_FEES", null, context, Map.of()),
                Map.of());
        assertTrue(result.softRulesMatched());
        assertEquals(100, context.get("fees"));
        assertFalse(result.executionVars().isEmpty());
    }
}

class CapabilityRegistryTest {

    @Test
    void registers_handlers_by_code() {
        SoftRuleCapabilitySupport support = new SoftRuleCapabilitySupport(CatalogSoftRuleEngine.withBuiltins());
        PricingBcClient pricing = mock(PricingBcClient.class);
        when(pricing.enabled()).thenReturn(false);
        CapabilityRegistry registry = new CapabilityRegistry(List.of(
                new NotifyCapabilityHandler(
                        support,
                        mock(com.gateway.payment.events.DomainEventPublisher.class),
                        mock(com.gateway.payment.catalog.i18n.CatalogConfigurationLocalizer.class),
                        mock(com.gateway.payment.payment.application.TemplateMapper.class)),
                new CalculateFeesCapabilityHandler(support, pricing)
        ));
        assertTrue(registry.has("NOTIFY"));
        assertTrue(registry.has("calculate_fees"));
        assertFalse(registry.has("UNKNOWN_CAP"));
    }
}

class CapabilityOperationMappingTest {

    @Test
    void maps_product_safe_names() {
        assertEquals("TRANSFERT",
                CapabilityOperationMapping.operationForCapability("EXECUTE_TRANSFER").orElseThrow());
        assertEquals("TRANSFERT",
                CapabilityOperationMapping.operationForCapability("EXECUTE_CASHOUT").orElseThrow());
        assertEquals("TRANSFERT",
                CapabilityOperationMapping.operationForCapability("EXECUTE_CASHIN").orElseThrow());
        assertEquals("EXECUTE_TRANSFER",
                CapabilityOperationMapping.capabilityForOperation("EXECUTE_RTGS").orElseThrow());
        assertEquals("EXECUTE_CASHOUT",
                CapabilityOperationMapping.capabilityForOperation("EXECUTE_CASHOUT").orElseThrow());
        assertEquals("LOOKUP_BILL",
                CapabilityOperationMapping.operationForCapability("GET_BILLS").orElseThrow());
        assertTrue(CapabilityOperationMapping.capabilityForOperation("ACCOUNT_TRANSFER").isEmpty());
    }
}
