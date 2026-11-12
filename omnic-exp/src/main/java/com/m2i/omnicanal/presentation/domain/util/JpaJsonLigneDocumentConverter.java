/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.omnicanal.presentation.domain.util;

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
public class JpaJsonLigneDocumentConverter implements AttributeConverter <JsonLigneTransaction,String >{
    private final static ObjectMapper objectMapper = new ObjectMapper();
        @Override
    public String convertToDatabaseColumn(JsonLigneTransaction meta) {
          String jsonString ="";
     try{
         Logger.getLogger("Start convert");
          jsonString = objectMapper.writeValueAsString(meta);
          
     } catch (JsonProcessingException ex) {
          Logger.getLogger(JpaJsonLigneDocumentConverter.class.getName()).log(Level.SEVERE, null, ex);
      }
     return jsonString;
              
          
    }

    @Override
    public JsonLigneTransaction convertToEntityAttribute(String dbData) {
        JsonLigneTransaction jst  = new JsonLigneTransaction ();
       try{
           jst= objectMapper.readValue(dbData,JsonLigneTransaction.class);
       } catch (IOException ex) {
          Logger.getLogger(JpaJsonLigneDocumentConverter.class.getName()).log(Level.SEVERE, null, ex);
      }
        return jst;
    }
}
