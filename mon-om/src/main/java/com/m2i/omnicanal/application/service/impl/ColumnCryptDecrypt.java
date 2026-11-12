/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.omnicanal.application.service.impl;

import jakarta.persistence.AttributeConverter;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author cda
 */
public class ColumnCryptDecrypt implements AttributeConverter<String, String>{
    @Autowired
    SecureClass secureClass;
    @Override
  public String convertToDatabaseColumn(String attribute) {
    /* perform encryption here */
      String encryptedString = secureClass.encrypt(attribute ) ;
      return  encryptedString;
  }
  @Override
  public String convertToEntityAttribute(String dbData) {
    /* perform decryption here */
     String decryptedString = secureClass.decrypt(dbData ) ;
      return  decryptedString;
  }
}
