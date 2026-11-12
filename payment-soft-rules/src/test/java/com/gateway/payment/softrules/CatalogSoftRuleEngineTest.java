package com.gateway.payment.softrules;

import com.gateway.payment.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CatalogSoftRuleEngineTest {

    private final CatalogSoftRuleEngine engine = CatalogSoftRuleEngine.withBuiltins();

    @Test
    void minAmount_rejectsBelowThreshold() {
        Map<String, Object> ctx = baseContext(List.of(Map.of(
                "step", "CHECK_LIMITS",
                "type", "MIN_AMOUNT",
                "params", Map.of("minAmount", 1000, "errorCode", "TOO_LOW")
        )));
        ctx.put("amount", 100);
        BusinessException ex = assertThrows(BusinessException.class,
                () -> engine.apply("CHECK_LIMITS", ctx));
        assertEquals("TOO_LOW", ex.getCode());
    }

    @Test
    void regexField_normalizesAndValidatesBic() {
        Map<String, Object> ctx = baseContext(List.of(Map.of(
                "step", "VALIDATE_BIC",
                "type", "REGEX_FIELD",
                "params", Map.of(
                        "field", "bic",
                        "pattern", "^[A-Z0-9]{8}([A-Z0-9]{3})?$",
                        "uppercase", true
                )
        )));
        ctx.put("bic", "bnpafrppxxx");
        var outcome = engine.apply("VALIDATE_BIC", ctx);
        assertTrue(outcome.matched());
        assertEquals("BNPAFRPPXXX", ctx.get("bic"));
    }

    @Test
    void blockedValues_honorsSkipFlag() {
        Map<String, Object> ctx = baseContext(List.of(Map.of(
                "step", "CHECK_COMPLIANCE",
                "type", "BLOCKED_VALUES",
                "params", Map.of(
                        "field", "countryCode",
                        "blocked", List.of("KP"),
                        "skipFlag", "skipComplianceCheck"
                )
        )));
        ctx.put("countryCode", "KP");
        ctx.put("skipComplianceCheck", true);
        assertTrue(engine.apply("CHECK_COMPLIANCE", ctx).matched());
    }

    @Test
    void defaultFee_setsFeesVar() {
        Map<String, Object> ctx = baseContext(List.of(Map.of(
                "step", "CALCULATE_FEES",
                "type", "DEFAULT_FEE",
                "params", Map.of("amount", 25000)
        )));
        var outcome = engine.apply("CALCULATE_FEES", ctx);
        assertEquals(25000, ctx.get("fees"));
        assertEquals(25000, outcome.executionVars().get("fees"));
    }

    @Test
    void unknownType_throws() {
        Map<String, Object> ctx = baseContext(List.of(Map.of(
                "step", "FOO",
                "type", "NOT_A_REAL_TYPE",
                "params", Map.of()
        )));
        BusinessException ex = assertThrows(BusinessException.class, () -> engine.apply("FOO", ctx));
        assertEquals("SOFT_RULE_UNKNOWN", ex.getCode());
    }

    private static Map<String, Object> baseContext(List<Map<String, Object>> softRules) {
        Map<String, Object> cfg = new HashMap<>();
        cfg.put("softRules", softRules);
        cfg.put("minAmount", new BigDecimal("1000000"));
        Map<String, Object> ctx = new HashMap<>();
        ctx.put("catalogConfiguration", cfg);
        ctx.put("catalogServiceCode", "TEST");
        return ctx;
    }
}
