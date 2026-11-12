package com.m2i.pti.domain.util;

import com.m2i.pti.domain.LigneGroupeProfilTemplate;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class JsonLigneGroupeProfilTemplate implements AttributeConverter<LigneGroupeProfilTemplate, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(LigneGroupeProfilTemplate attribute) {
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Erreur lors de la conversion en JSON", e);
        }
    }

    @Override
    public LigneGroupeProfilTemplate convertToEntityAttribute(String dbData) {
        try {
            return objectMapper.readValue(dbData, LigneGroupeProfilTemplate.class);
        } catch (IOException e) {
            throw new IllegalArgumentException("Erreur lors de la lecture JSON", e);
        }
    }
}
