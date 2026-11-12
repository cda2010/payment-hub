package com.gateway.payment.fidelite.domain;

public enum ParrainageStatut {
    OUVERT,
    /** Le filleul a saisi le code. Les points ne sont pas encore dus. */
    ASSOCIE,
    /** KYC et première transaction éligible réunis. La prime a été demandée une fois. */
    QUALIFIE,
    EXPIRE,
    REJETE,
    /** Ancien crédit immédiat, conservé pour les parrainages déjà payés. */
    UTILISE
}
