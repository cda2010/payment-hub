package com.gateway.payment.pricing.tarif.domain;

public enum TypeFormule {
    /** Implémentation Java approuvée, enregistrée dans le FormulaRegistry. */
    PREDEFINIE,
    /** Expression exécutée par un interpréteur à liste blanche (non disponible en phase 1). */
    EXPRESSION
}
