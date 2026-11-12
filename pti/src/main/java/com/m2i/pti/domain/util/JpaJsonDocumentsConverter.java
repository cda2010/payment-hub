/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.domain.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.persistence.AttributeConverter;

/**
 *
 * @author cda
 */
public class JpaJsonDocumentsConverter implements AttributeConverter <JsonLigneReglementTemplate,String >{
     private final static ObjectMapper objectMapper = new ObjectMapper();
 
    @Override
    public String convertToDatabaseColumn(JsonLigneReglementTemplate meta) {
          String jsonString ="";
     try{
         Logger.getLogger("Start convert");
          jsonString = objectMapper.writeValueAsString(meta);
          
     } catch (JsonProcessingException ex) {
          Logger.getLogger(JpaJsonDocumentsConverter.class.getName()).log(Level.SEVERE, null, ex);
      }
     return jsonString;
              
          
    }

    @Override
    public JsonLigneReglementTemplate convertToEntityAttribute(String dbData) {
        JsonLigneReglementTemplate jst  = new JsonLigneReglementTemplate ();
       try{
           jst= objectMapper.readValue(dbData,JsonLigneReglementTemplate.class);
       } catch (IOException ex) {
          Logger.getLogger(JpaJsonDocumentsConverter.class.getName()).log(Level.SEVERE, null, ex);
      }
        return jst;
    }
}
