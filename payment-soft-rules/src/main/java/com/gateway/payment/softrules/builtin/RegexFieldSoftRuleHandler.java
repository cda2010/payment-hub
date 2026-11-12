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
public class RegexFieldSoftRuleHandler implements SoftRuleHandler {

    @Override
    public Set<String> types() {
        return Set.of("REGEX_FIELD");
    }

    @Override
    public SoftRuleApplyResult apply(SoftRuleApplyContext ctx) {
        Map<String, Object> params = ctx.params();
        Map<String, Object> context = ctx.context();
        String field = SoftRuleSupport.str(params.get("field"));
        String pattern = SoftRuleSupport.str(params.get("pattern"));
        if (field == null || pattern == null) {
            throw new BusinessException("SOFT_RULE_INVALID", "REGEX_FIELD exige field + pattern");
        }
        Object raw = context.get(field);
        String value = raw == null ? "" : String.valueOf(raw).trim();
        if (SoftRuleSupport.isTruthy(params.get("uppercase"))) {
            value = value.toUpperCase(Locale.ROOT);
            context.put(field, value);
        }
        if (!value.matches(pattern)) {
            String code = SoftRuleSupport.str(params.get("errorCode"));
            if (code == null) {
                code = "FIELD_INVALID";
            }
            String msg = SoftRuleSupport.str(params.get("message"));
            throw new BusinessException(code, msg != null ? msg : "Champ invalide: " + field);
        }
        return SoftRuleApplyResult.of("REGEX_FIELD", field + " OK");
    }
}
