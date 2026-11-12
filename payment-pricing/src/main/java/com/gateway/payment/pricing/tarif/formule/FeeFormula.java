package com.gateway.payment.pricing.tarif.formule;

import java.math.BigDecimal;
import java.util.List;

/**
 * Implémentation approuvée d'une version de formule. Le moteur ne l'appelle qu'avec des paramètres
 * déjà validés contre le schéma en base ; le calcul se fait en BigDecimal, sans arrondi.
 */
public interface FeeFormula {

    String code();

    int version();

    /** Paramètres attendus ; doivent correspondre au schéma de la formule en base (vérifié au démarrage). */
    List<ParametreAttendu> parametres();

    ResultatFormule calculer(BigDecimal montant, ParametresValides parametres);
}
