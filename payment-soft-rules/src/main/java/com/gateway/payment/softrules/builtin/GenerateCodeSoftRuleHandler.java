package com.gateway.payment.softrules.builtin;

import com.gateway.payment.softrules.SoftRuleSupport;
import com.gateway.payment.softrules.spi.SoftRuleApplyContext;
import com.gateway.payment.softrules.spi.SoftRuleApplyResult;
import com.gateway.payment.softrules.spi.SoftRuleHandler;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class GenerateCodeSoftRuleHandler implements SoftRuleHandler {

    @Override
    public Set<String> types() {
        return Set.of("GENERATE_CODE");
    }

    @Override
    public SoftRuleApplyResult apply(SoftRuleApplyContext ctx) {
        Map<String, Object> params = ctx.params();
        Map<String, Object> context = ctx.context();
        String field = SoftRuleSupport.str(params.get("field"));
        if (field == null) {
            field = "cashOutCode";
        }
        if (context.get(field) != null && !String.valueOf(context.get(field)).isBlank()) {
            return SoftRuleApplyResult.of("GENERATE_CODE", field + " déjà présent");
        }
        String prefix = SoftRuleSupport.str(params.get("prefix"));
        if (prefix == null) {
            prefix = "CO";
        }
        int length = 6;
        Object len = params.get("length");
        if (len instanceof Number n) {
            length = Math.max(4, Math.min(12, n.intValue()));
        }
        StringBuilder sb = new StringBuilder(prefix).append("-");
        String alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        for (int i = 0; i < length; i++) {
            sb.append(alphabet.charAt(rnd.nextInt(alphabet.length())));
        }
        String code = sb.toString();
        context.put(field, code);
        Map<String, Object> vars = new LinkedHashMap<>();
        vars.put(field, code);
        String wait = "Présentez le code " + code + " à l'agent avec une pièce d'identité";
        context.put("waitMessage", wait);
        vars.put("waitMessage", wait);
        return SoftRuleApplyResult.of("GENERATE_CODE", field + "=" + code, vars);
    }
}
