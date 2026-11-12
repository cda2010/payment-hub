package com.gateway.payment.softrules.builtin;

import com.gateway.payment.common.exception.BusinessException;
import com.gateway.payment.softrules.SoftRuleSupport;
import com.gateway.payment.softrules.spi.SoftRuleApplyContext;
import com.gateway.payment.softrules.spi.SoftRuleApplyResult;
import com.gateway.payment.softrules.spi.SoftRuleHandler;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class RequiredFieldSoftRuleHandler implements SoftRuleHandler {

    @Override
    public Set<String> types() {
        return Set.of("REQUIRED_FIELD");
    }

    @Override
    public SoftRuleApplyResult apply(SoftRuleApplyContext ctx) {
        Map<String, Object> params = ctx.params();
        Map<String, Object> context = ctx.context();
        String field = SoftRuleSupport.str(params.get("field"));
        if (field == null) {
            throw new BusinessException("SOFT_RULE_INVALID", "REQUIRED_FIELD exige field");
        }
        Object raw = context.get(field);
        if (raw == null || String.valueOf(raw).isBlank()) {
            String code = SoftRuleSupport.str(params.get("errorCode"));
            if (code == null) {
                code = "FIELD_REQUIRED";
            }
            String msg = SoftRuleSupport.str(params.get("message"));
            throw new BusinessException(code, msg != null ? msg : "Champ requis: " + field);
        }
        return SoftRuleApplyResult.of("REQUIRED_FIELD", field + " présent");
    }
}
