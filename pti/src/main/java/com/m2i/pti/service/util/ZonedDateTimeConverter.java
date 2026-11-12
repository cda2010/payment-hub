package com.m2i.pti.service.util;

import jakarta.persistence.AttributeConverter;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.TimeZone;

public class ZonedDateTimeConverter implements AttributeConverter<ZonedDateTime, Calendar> {
    @Override
    public Calendar convertToDatabaseColumn(ZonedDateTime entityAttribute) {
        if (entityAttribute == null) {
            return null;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(entityAttribute.toInstant().toEpochMilli());
        calendar.setTimeZone(TimeZone.getTimeZone(entityAttribute.getZone()));
        return calendar;
    }

    @Override
    public ZonedDateTime convertToEntityAttribute(Calendar databaseColumn) {
        if (databaseColumn == null) {
            return null;
        }

        return ZonedDateTime.ofInstant(databaseColumn.toInstant(),
                databaseColumn
                        .getTimeZone()
                        .toZoneId());
    }
}
