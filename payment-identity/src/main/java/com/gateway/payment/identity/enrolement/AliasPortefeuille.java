package com.gateway.payment.identity.enrolement;

import java.util.UUID;

/** Crée l'alias du compte reconnu dans payment-alias. */
public interface AliasPortefeuille {

    void assurer(String tenantId, UUID partyId, String numero, String libelle);
}
