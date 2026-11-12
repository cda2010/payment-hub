package com.gateway.payment.orchestration.service;

import com.gateway.payment.orchestration.domain.CompteFse;

/**
 * Extracteur typé d'un champ {@link CompteFse}.
 * Implémentations natives : {@link CompteFieldExtractors}.
 * Extensions : bean Spring {@link CompteFieldExtractor} + {@link CompteFieldExtractorRegistry}.
 */
@FunctionalInterface
public interface CompteFieldExtractor {

    String extract(CompteFse compte);

    /** Code BDD ({@code parametre_utilisateur.attribut}). */
    default String code() {
        return getClass().getSimpleName();
    }

    /** Si true, la valeur user/schéma n'est pas dérivée via un compte catalogue. */
    default boolean passthrough() {
        return false;
    }
}
