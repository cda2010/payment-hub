package com.gateway.payment.catalog.casting;

import java.util.List;
import java.util.Optional;

/**
 * Casting + bindings d'une version produit.
 */
public record CatalogProductCasting(
        List<CatalogRoleDefinition> roles,
        List<CatalogParticipantDefinition> participants,
        List<CatalogBindingDefinition> bindings
) {
    public CatalogProductCasting {
        roles = roles == null ? List.of() : List.copyOf(roles);
        participants = participants == null ? List.of() : List.copyOf(participants);
        bindings = bindings == null ? List.of() : List.copyOf(bindings);
    }

    public boolean isEmpty() {
        return roles.isEmpty() && participants.isEmpty() && bindings.isEmpty();
    }

    public Optional<CatalogBindingDefinition> findBinding(String step, String assignedProvider) {
        if (step == null || step.isBlank() || bindings.isEmpty()) {
            return Optional.empty();
        }
        String stepNorm = step.trim();
        CatalogBindingDefinition fallback = null;
        for (CatalogBindingDefinition b : bindings) {
            if (!stepNorm.equalsIgnoreCase(b.step())) {
                continue;
            }
            if (b.providerCode() != null && assignedProvider != null
                    && b.providerCode().equalsIgnoreCase(assignedProvider)) {
                return Optional.of(b);
            }
            if (b.providerCode() == null && fallback == null) {
                fallback = b;
            }
        }
        return Optional.ofNullable(fallback);
    }
}
