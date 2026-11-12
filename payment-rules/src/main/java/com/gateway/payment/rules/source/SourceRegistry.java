package com.gateway.payment.rules.source;

import com.gateway.payment.rules.domain.RuleVersion;
import com.gateway.payment.rules.moteur.DemandeEvaluation;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SourceRegistry {

    private final List<RuleSourceResolver> resolvers;

    public SourceRegistry(List<RuleSourceResolver> resolvers) {
        this.resolvers = List.copyOf(resolvers);
    }

    public SourceValue resolve(RuleVersion version, DemandeEvaluation d, CumulSnapshot cumul) {
        return resolvers.stream()
                .filter(r -> r.supports(version))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Aucune source pour " + version.getSourceType()))
                .resolve(version, d, cumul);
    }
}
