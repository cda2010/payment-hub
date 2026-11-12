/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.service.util;

import java.time.LocalDateTime;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 *
 * @author cda
 */
public class LocalDateTimeSerializer extends JsonSerializer<LocalDateTime>{
    private static final Logger LOG = LoggerFactory.getLogger(LocalDateTimeSerializer.class);
	
	@Override
	public void serialize(LocalDateTime arg0, JsonGenerator arg1, SerializerProvider arg2) throws IOException {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
		String format = formatter.format(arg0);
        LOG.info("Serialized Date='{}'", format);
		arg1.writeString(format);
	}


//and 
// convert string in format MM/dd/yyyy HH:mm:ss to LocalDateTime

}
