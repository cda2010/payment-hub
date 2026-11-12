package com.gateway.payment.network;

import com.gateway.payment.network.domain.StatutUnite;
import com.gateway.payment.network.domain.TypeUnite;
import com.gateway.payment.network.domain.UniteOrganisation;

import java.util.UUID;

/** Unités construites en mémoire, avec des chemins cohérents, pour les tests unitaires. */
public final class Reseau {

    private Reseau() {
    }

    public static UniteOrganisation pays(String code, String zone) {
        UUID id = UUID.randomUUID();
        return base(id, code, TypeUnite.PAYS, zone).niveau(1).chemin("/" + id + "/").build();
    }

    public static UniteOrganisation sous(UniteOrganisation parent, TypeUnite type, String code) {
        UUID id = UUID.randomUUID();
        return base(id, code, type, parent.getZone())
                .parentId(parent.getId())
                .niveau(parent.getNiveau() + 1)
                .chemin(parent.getChemin() + id + "/")
                .build();
    }

    public static UniteOrganisation regroupement(String code, UniteOrganisation parent) {
        return sous(parent, TypeUnite.REGROUPEMENT, code);
    }

    private static UniteOrganisation.UniteOrganisationBuilder base(UUID id, String code, TypeUnite type, String zone) {
        return UniteOrganisation.builder()
                .id(id)
                .code(code)
                .libelle(code)
                .type(type)
                .zone(zone)
                .statut(StatutUnite.ACTIF)
                .version(0L)
                .createdBy("test")
                .updatedBy("test");
    }
}
