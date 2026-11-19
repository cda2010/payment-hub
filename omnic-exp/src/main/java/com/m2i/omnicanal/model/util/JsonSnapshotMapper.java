package com.m2i.omnicanal.model.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Instantané JSON d'une transaction. Le graphe embarque des entités JPA
 * ({@code FournisseurFSE}) qui ajoutent des colonnes absentes du modèle,
 * par exemple {@code typeMoyen}. La relecture ne doit pas échouer pour autant.
 */
final class JsonSnapshotMapper {

    static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private JsonSnapshotMapper() {
    }
}
