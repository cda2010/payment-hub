package com.m2i.omnicanal.model.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.persistence.AttributeConverter;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Même correctif que {@link JpaJsonDocumentsConverter}, pour le JSON d'une ligne.
 */
public class JpaJsonLigneDocumentConverter implements AttributeConverter<JsonLigneTransaction, String> {

    @Override
    public String convertToDatabaseColumn(JsonLigneTransaction meta) {
        String jsonString = "";
        try {
            jsonString = JsonSnapshotMapper.MAPPER.writeValueAsString(meta);
        } catch (JsonProcessingException ex) {
            Logger.getLogger(JpaJsonLigneDocumentConverter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jsonString;
    }

    @Override
    public JsonLigneTransaction convertToEntityAttribute(String dbData) {
        JsonLigneTransaction jst = new JsonLigneTransaction();
        try {
            jst = JsonSnapshotMapper.MAPPER.readValue(dbData, JsonLigneTransaction.class);
        } catch (IOException ex) {
            Logger.getLogger(JpaJsonLigneDocumentConverter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jst;
    }
}
