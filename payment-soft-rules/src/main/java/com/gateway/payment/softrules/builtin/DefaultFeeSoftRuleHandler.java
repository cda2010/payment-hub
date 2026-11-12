package com.gateway.payment.softrules.builtin;

import com.gateway.payment.softrules.SoftRuleSupport;
import com.gateway.payment.softrules.spi.SoftRuleApplyContext;
import com.gateway.payment.softrules.spi.SoftRuleApplyResult;
import com.gateway.payment.softrules.spi.SoftRuleHandler;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Component
public class DefaultFeeSoftRuleHandler implements SoftRuleHandler {

    @Override
    public Set<String> types() {
        return Set.of("DEFAULT_FEE");
    }

    @Override
    public SoftRuleApplyResult apply(SoftRuleApplyContext ctx) {
        Map<String, Object> params = ctx.params();
        Map<String, Object> context = ctx.context();
        if (context.get("fees") != null) {
            return SoftRuleApplyResult.empty();
        }
        BigDecimal fee = SoftRuleSupport.toDecimal(params.get("amount"));
        if (fee == null) {
            fee = SoftRuleSupport.toDecimal(params.get("fee"));
        }
        if (fee == null) {
            Map<String, Object> cfg = SoftRuleSupport.catalogConfiguration(context);
            fee = SoftRuleSupport.toDecimal(cfg.get("defaultFee"));
            if (fee == null) {
                fee = SoftRuleSupport.toDecimal(cfg.get("default_fee"));
            }
        }
        int fees = fee != null ? fee.intValue() : 0;
        context.put("fees", fees);
        Map<String, Object> vars = new LinkedHashMap<>();
        vars.put("fees", fees);
        BigDecimal amount = SoftRuleSupport.toDecimal(context.get("amount"));
        if (amount != null) {
            BigDecimal total = amount.add(BigDecimal.valueOf(fees));
            context.put("totalDebit", total);
            vars.put("totalDebit", total);
        }
        return SoftRuleApplyResult.of("DEFAULT_FEE", "Frais=" + fees, vars);
    }
}
