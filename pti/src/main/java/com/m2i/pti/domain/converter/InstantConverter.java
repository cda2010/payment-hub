package com.m2i.pti.domain.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.sql.Timestamp;
import java.time.Instant;

@Converter(autoApply = true)
public class InstantConverter implements AttributeConverter<Instant, Timestamp> {
// also ZonedDateTimeConverter and InstantConverter

    @Override
    public Timestamp convertToDatabaseColumn(Instant localDate) {
        return localDate != null ? Timestamp.from(localDate) : null;
    }

    @Override
    public Instant convertToEntityAttribute(Timestamp date) {
        return date != null ? date.toInstant() : null;
    }
}
