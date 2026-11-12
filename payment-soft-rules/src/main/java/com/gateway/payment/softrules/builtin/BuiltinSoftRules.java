package com.gateway.payment.softrules.builtin;

import com.gateway.payment.softrules.spi.SoftRuleHandler;
import com.gateway.payment.softrules.spi.SoftRuleRegistry;

import java.util.List;

/** Liste des handlers builtins — tests hors Spring. */
public final class BuiltinSoftRules {

    private BuiltinSoftRules() {}

    public static List<SoftRuleHandler> handlers() {
        return List.of(
                new MinAmountSoftRuleHandler(),
                new BusinessHoursSoftRuleHandler(),
                new RegexFieldSoftRuleHandler(),
                new RequiredFieldSoftRuleHandler(),
                new BlockedValuesSoftRuleHandler(),
                new DefaultFeeSoftRuleHandler(),
                new GenerateCodeSoftRuleHandler(),
                new PassSoftRuleHandler()
        );
    }

    public static SoftRuleRegistry registry() {
        return SoftRuleRegistry.of(handlers());
    }
}
