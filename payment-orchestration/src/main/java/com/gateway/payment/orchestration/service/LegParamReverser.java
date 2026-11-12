package com.gateway.payment.orchestration.service;

import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Inverse les paramètres résolus d'une jambe (miroir Omnicanal {@code LegCompilerImpl#getReverse}).
 */
final class LegParamReverser {

    private LegParamReverser() {}

    static Map<String, String> reverse(Map<String, String> resolved) {
        if (resolved == null || resolved.isEmpty()) {
            return Map.of();
        }
        Map<String, String> out = new LinkedHashMap<>(resolved);
        swap(out, "CPTSOURCE", "CPTDEST");
        swap(out, "compteDebiteur", "compteCredit");
        swap(out, "compteSource", "compteDest");
        swap(out, "CPTELORO", "CPTEMIROIR");
        return out;
    }

    private static void swap(Map<String, String> params, String keyA, String keyB) {
        if (!params.containsKey(keyA) && !params.containsKey(keyB)) {
            return;
        }
        String a = params.get(keyA);
        String b = params.get(keyB);
        if (StringUtils.hasText(a) || StringUtils.hasText(b)) {
            params.put(keyA, b != null ? b : "");
            params.put(keyB, a != null ? a : "");
        }
    }
}
