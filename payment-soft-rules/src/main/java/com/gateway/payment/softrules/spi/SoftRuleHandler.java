package com.gateway.payment.softrules.spi;

import java.util.Set;

/**
 * Handler d'un (ou plusieurs) type(s) softRule catalogue.
 * <p>
 * Beans Spring collectés par {@link SoftRuleRegistry}.
 * Nouveau type = nouveau bean — pas de switch dans le moteur.
 */
public interface SoftRuleHandler {

    /** Types supportés (ex. {@code REGEX_FIELD}, {@code BLOCKED_VALUES}). */
    Set<String> types();

    default String label() {
        return types().stream().findFirst().orElse("?");
    }

    SoftRuleApplyResult apply(SoftRuleApplyContext context);
}
