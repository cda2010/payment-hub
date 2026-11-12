package com.gateway.payment.rules.source;

import java.math.BigDecimal;

/** Valeur résolue pour une source de règle (montant / compteur / attribut). */
public record SourceValue(
        Object raw,
        BigDecimal numeric,
        BigDecimal currentCumul,
        BigDecimal projected,
        boolean cumulSource
) {
    public static SourceValue of(Object raw) {
        BigDecimal n = null;
        if (raw instanceof BigDecimal bd) {
            n = bd;
        } else if (raw instanceof Number num) {
            n = BigDecimal.valueOf(num.doubleValue());
        }
        return new SourceValue(raw, n, null, null, false);
    }

    public static SourceValue cumul(BigDecimal current, BigDecimal projected) {
        return new SourceValue(projected, projected, current, projected, true);
    }
}
