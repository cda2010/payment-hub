package com.gateway.payment.rules.source;

import com.gateway.payment.rules.domain.RuleVersion;
import com.gateway.payment.rules.moteur.DemandeEvaluation;

/**
 * SPI : résolution d'une source de données pour une règle.
 * Nouvelles sources = nouveau bean, sans toucher au moteur central.
 */
public interface RuleSourceResolver {
    boolean supports(RuleVersion version);

    SourceValue resolve(RuleVersion version, DemandeEvaluation demande, CumulSnapshot cumul);
}
