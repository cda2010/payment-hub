package com.gateway.payment.omnicanal.util;

import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

/** Helpers canal (otrParametre = k=v|k2=v2). */
public final class CanalParamParser {

    private CanalParamParser() {}

    public static Map<String, String> parseOtrParametre(String otrParametre) {
        Map<String, String> out = new LinkedHashMap<>();
        if (!StringUtils.hasText(otrParametre)) {
            return out;
        }
        for (String part : otrParametre.split("\\|")) {
            if (!StringUtils.hasText(part)) {
                continue;
            }
            int eq = part.indexOf('=');
            if (eq <= 0) {
                continue;
            }
            String key = part.substring(0, eq).trim();
            String value = part.substring(eq + 1).trim();
            if (StringUtils.hasText(key)) {
                out.put(key, value);
            }
        }
        return out;
    }
}
