/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.omnicanal.presentation.domain.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.persistence.AttributeConverter;
import org.slf4j.LoggerFactory;

/**
 *
 * @author HP PAVILLON
 */
public class JpaJsonDocumentsConverter implements AttributeConverter <JsonTransaction,String > {
   
  private final static ObjectMapper objectMapper = new ObjectMapper();
 
    @Override
    public String convertToDatabaseColumn(JsonTransaction meta) {
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
    public JsonTransaction convertToEntityAttribute(String dbData) {
        JsonTransaction jst  = new JsonTransaction ();
       try{
           jst= objectMapper.readValue(dbData,JsonTransaction.class);
       } catch (IOException ex) {
          Logger.getLogger(JpaJsonDocumentsConverter.class.getName()).log(Level.SEVERE, null, ex);
      }
        return jst;
    }
    
}
