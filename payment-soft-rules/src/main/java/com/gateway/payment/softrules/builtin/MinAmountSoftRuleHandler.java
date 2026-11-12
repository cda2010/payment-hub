package com.gateway.payment.softrules.builtin;

import com.gateway.payment.common.exception.BusinessException;
import com.gateway.payment.softrules.SoftRuleSupport;
import com.gateway.payment.softrules.spi.SoftRuleApplyContext;
import com.gateway.payment.softrules.spi.SoftRuleApplyResult;
import com.gateway.payment.softrules.spi.SoftRuleHandler;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

@Component
public class MinAmountSoftRuleHandler implements SoftRuleHandler {

    @Override
    public Set<String> types() {
        return Set.of("MIN_AMOUNT");
    }

    @Override
    public SoftRuleApplyResult apply(SoftRuleApplyContext ctx) {
        Map<String, Object> params = ctx.params();
        Map<String, Object> context = ctx.context();
        BigDecimal amount = SoftRuleSupport.toDecimal(context.get("amount"));
        BigDecimal min = resolveMin(context, params);
        if (min == null) {
            return SoftRuleApplyResult.empty();
        }
        if (amount == null || amount.compareTo(min) < 0) {
            String code = SoftRuleSupport.str(params.get("errorCode"));
            if (code == null) {
                code = "MIN_AMOUNT";
            }
            throw new BusinessException(code,
                    "Montant inférieur au minimum (" + min.toPlainString() + ")");
        }
        return SoftRuleApplyResult.of("MIN_AMOUNT", "Montant ≥ " + min.toPlainString());
    }

    private static BigDecimal resolveMin(Map<String, Object> context, Map<String, Object> params) {
        BigDecimal direct = SoftRuleSupport.toDecimal(params.get("minAmount"));
        if (direct == null) {
            direct = SoftRuleSupport.toDecimal(params.get("min"));
        }
        if (direct != null) {
            return direct;
        }
        String key = SoftRuleSupport.str(params.get("minAmountKey"));
        if (key == null) {
            key = "minAmount";
        }
        Map<String, Object> cfg = SoftRuleSupport.catalogConfiguration(context);
        BigDecimal fromCfg = SoftRuleSupport.toDecimal(cfg.get(key));
        if (fromCfg == null && "minAmount".equals(key)) {
            fromCfg = SoftRuleSupport.toDecimal(cfg.get("min_amount"));
        }
        return fromCfg;
    }
}
