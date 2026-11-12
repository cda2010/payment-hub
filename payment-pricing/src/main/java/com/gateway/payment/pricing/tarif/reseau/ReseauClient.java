package com.gateway.payment.pricing.tarif.reseau;

import java.util.UUID;

/** Lecture des positions dans le réseau de distribution (payment-network). */
public interface ReseauClient {

    /** @throws ReseauException unité introuvable ou réseau indisponible */
    PositionReseau unite(UUID id);

    /** @param code code ISO 3166 alpha-2 */
    PositionReseau pays(String code);

    PositionReseau zone(String code);
}
