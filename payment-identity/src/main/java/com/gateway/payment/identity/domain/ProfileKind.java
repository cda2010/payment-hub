package com.gateway.payment.identity.domain;

/**
 * Type de profil d'offre :
 * <ul>
 *   <li>{@code SERVICE} — produits / services catalogue autorisés</li>
 *   <li>{@code TARIFF} — grilles tarifaires de référence (produit → grille pricing)</li>
 *   <li>{@code LIMIT} — groupes de plafonds (produit + sens → groupe payment-rules)</li>
 * </ul>
 */
public enum ProfileKind {
    SERVICE,
    TARIFF,
    LIMIT
}
