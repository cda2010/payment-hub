package com.gateway.payment.transfer.domain;

public enum RailScope {
    /** Routage produit interne (LOCAL, INTRA_GROUP) */
    INTERNAL,
    /** Réseau de place / international (STAR, RTGS, SWIFT) */
    EXTERNAL
}
