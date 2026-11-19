package com.m2i.omnicanal.model.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.persistence.AttributeConverter;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Remplace le convertisseur du jar {@code omnicanalmodel} : le classpath
 * de l'application est chargé avant la dépendance.
 */
public class JpaJsonDocumentsConverter implements AttributeConverter<JsonTransaction, String> {

    @Override
    public String convertToDatabaseColumn(JsonTransaction meta) {
        String jsonString = "";
        try {
            jsonString = JsonSnapshotMapper.MAPPER.writeValueAsString(meta);
        } catch (JsonProcessingException ex) {
            Logger.getLogger(JpaJsonDocumentsConverter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jsonString;
    }

    @Override
    public JsonTransaction convertToEntityAttribute(String dbData) {
        JsonTransaction jst = new JsonTransaction();
        try {
            jst = JsonSnapshotMapper.MAPPER.readValue(dbData, JsonTransaction.class);
        } catch (IOException ex) {
            Logger.getLogger(JpaJsonDocumentsConverter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jst;
    }
}
