package com.gateway.payment.network.alias;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

/** Lecture d'un alias. L'étiquette dit le rôle du compte (CPTEOPERATION, ou une autre). */
public interface AliasLecture {

    AliasVu lire(UUID id, String authorization);

    /** Alias actif étiqueté CPTEOPERATION du point de vente. */
    AliasVu compteOperation(UUID pointVenteId, String authorization);

    @JsonIgnoreProperties(ignoreUnknown = true)
    record AliasVu(
            UUID id,
            String etiquette,
            String ownerKind,
            UUID ownerId,
            String currency,
            boolean active
    ) {
    }
}
