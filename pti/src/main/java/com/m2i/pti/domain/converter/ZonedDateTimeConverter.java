package com.m2i.pti.domain.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Converter(autoApply = true)
public class ZonedDateTimeConverter implements AttributeConverter<ZonedDateTime, Timestamp> {
// also ZonedDateTimeConverter and InstantConverter

    @Override
    public Timestamp convertToDatabaseColumn(ZonedDateTime localDate) {
        return localDate != null ? Timestamp.valueOf(localDate.toLocalDateTime()) : null;
    }

    @Override
    public ZonedDateTime convertToEntityAttribute(Timestamp date) {
        return date != null ? ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()) : null;
    }
}
