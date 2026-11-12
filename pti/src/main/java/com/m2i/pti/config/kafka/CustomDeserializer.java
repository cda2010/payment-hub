/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.config.kafka;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import org.apache.kafka.common.serialization.Deserializer;

/**
 *
 * @author cda
 */
public class CustomDeserializer<T extends Serializable> implements Deserializer<T> {

    private ObjectMapper mapper = new ObjectMapper();
    public static final String VALUE_CLASS_NAME_CONFIG = "value.class.name";

    @Override
    public void configure(final Map<String, ?> settings, final boolean isKey) {

        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.findAndRegisterModules();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public T deserialize(final String topic, final byte[] bytes) {
        try {
            return mapper.readValue(bytes, new TypeReference<T>() {
            });
        } catch (final IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public void close() {
    }

}
