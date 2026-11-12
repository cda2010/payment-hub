/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.config.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.Serializable;
import java.util.Map;
import org.apache.kafka.common.serialization.Serializer;

/**
 *
 * @author cda
 */
public class CustomSerializer<T extends Serializable> implements Serializer<T> {
private ObjectMapper mapper;
    @Override
    public void configure(Map<String, ?> map, boolean bln) {
         mapper = new ObjectMapper();
    }

     @Override
  public byte[] serialize(final String topic, final T object) {
    try {
      return mapper.writeValueAsBytes(object);
    } catch (final JsonProcessingException e) {
      throw new IllegalArgumentException(e);
    }
  }

    @Override
    public void close() {
       
    }
    
}
