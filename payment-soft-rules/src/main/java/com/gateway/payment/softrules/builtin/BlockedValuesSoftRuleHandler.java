package com.gateway.payment.softrules.builtin;

import com.gateway.payment.common.exception.BusinessException;
import com.gateway.payment.softrules.SoftRuleSupport;
import com.gateway.payment.softrules.spi.SoftRuleApplyContext;
import com.gateway.payment.softrules.spi.SoftRuleApplyResult;
import com.gateway.payment.softrules.spi.SoftRuleHandler;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Component
public class BlockedValuesSoftRuleHandler implements SoftRuleHandler {

    @Override
    public Set<String> types() {
        return Set.of("BLOCKED_VALUES");
    }

    @Override
    public SoftRuleApplyResult apply(SoftRuleApplyContext ctx) {
        Map<String, Object> params = ctx.params();
        Map<String, Object> context = ctx.context();
        String skipFlag = SoftRuleSupport.str(params.get("skipFlag"));
        if (skipFlag != null && SoftRuleSupport.isTruthy(context.get(skipFlag))) {
            return SoftRuleApplyResult.of("BLOCKED_VALUES", "Contournement " + skipFlag);
        }
        String field = SoftRuleSupport.str(params.get("field"));
        if (field == null) {
            throw new BusinessException("SOFT_RULE_INVALID", "BLOCKED_VALUES exige field");
        }
        String value = context.get(field) != null
                ? String.valueOf(context.get(field)).trim().toUpperCase(Locale.ROOT)
                : "";
        Set<String> blocked = SoftRuleSupport.blockedSet(params.get("blocked"));
        if (blocked.contains(value)) {
            String code = SoftRuleSupport.str(params.get("errorCode"));
            if (code == null) {
                code = "VALUE_BLOCKED";
            }
            throw new BusinessException(code, "Valeur non autorisée pour " + field + ": " + value);
        }
        return SoftRuleApplyResult.of("BLOCKED_VALUES", field + " autorisé");
    }
}
