package com.gateway.payment.fidelite.domain;

/** Qui peut bénéficier du programme. */
public enum ProgrammePortee {
    /** Tous les clients du tenant. */
    TOUS,
    /** Seulement les clients qui portent l'un des profils listés. */
    PROFIL,
    /** Créé par une unité d'organisation, en pratique un marchand, pour ses clients. */
    UNITE
}
