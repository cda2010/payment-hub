package com.gateway.payment.alias.domain;

/**
 * Propriétaire de l'alias :
 * <ul>
 *   <li>{@code PARTY} / {@code ORG} — payment-identity</li>
 *   <li>{@code UNITE} — unité d'organisation payment-network</li>
 *   <li>{@code POINT_VENTE} — point de vente d'une unité</li>
 * </ul>
 */
public enum AliasOwnerKind {
    PARTY,
    ORG,
    UNITE,
    POINT_VENTE
}
