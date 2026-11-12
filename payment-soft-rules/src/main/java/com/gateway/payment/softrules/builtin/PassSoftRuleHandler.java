package com.gateway.payment.softrules.builtin;

import com.gateway.payment.softrules.SoftRuleSupport;
import com.gateway.payment.softrules.spi.SoftRuleApplyContext;
import com.gateway.payment.softrules.spi.SoftRuleApplyResult;
import com.gateway.payment.softrules.spi.SoftRuleHandler;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class PassSoftRuleHandler implements SoftRuleHandler {

    @Override
    public Set<String> types() {
        return Set.of("PASS");
    }

    @Override
    public SoftRuleApplyResult apply(SoftRuleApplyContext ctx) {
        String msg = SoftRuleSupport.str(ctx.params().get("message"));
        return SoftRuleApplyResult.of(ctx.step() != null ? ctx.step() : "PASS", msg != null ? msg : "OK");
    }
}
