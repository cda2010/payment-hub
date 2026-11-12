package com.gateway.payment.softrules.spi;

import java.util.Map;

/**
 * Contexte d'exécution d'une softRule JSON ({@code step}/{@code type}/{@code params}).
 * {@link #context()} est mutable (normalisation BIC, fees, …).
 */
public record SoftRuleApplyContext(
        String step,
        String type,
        Map<String, Object> params,
        Map<String, Object> context
) {}
