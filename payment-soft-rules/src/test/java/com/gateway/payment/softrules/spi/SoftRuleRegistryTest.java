package com.gateway.payment.softrules.spi;

import com.gateway.payment.softrules.builtin.RegexFieldSoftRuleHandler;
import com.gateway.payment.softrules.builtin.RequiredFieldSoftRuleHandler;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SoftRuleRegistryTest {

    @Test
    void registers_types() {
        SoftRuleRegistry registry = SoftRuleRegistry.of(List.of(
                new RegexFieldSoftRuleHandler(),
                new RequiredFieldSoftRuleHandler()
        ));
        assertTrue(registry.has("REGEX_FIELD"));
        assertTrue(registry.has("required-field"));
    }

    @Test
    void duplicate_type_fails() {
        assertThrows(IllegalStateException.class, () -> SoftRuleRegistry.of(List.of(
                new RegexFieldSoftRuleHandler(),
                new RegexFieldSoftRuleHandler()
        )));
    }
}
