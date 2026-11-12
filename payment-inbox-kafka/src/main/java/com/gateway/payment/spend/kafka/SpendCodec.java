package com.gateway.payment.spend.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public final class SpendCodec {

    private final ObjectMapper mapper;

    public SpendCodec() {
        this.mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public String write(SpendEvent event) {
        try {
            return mapper.writeValueAsString(event);
        } catch (Exception e) {
            throw new IllegalArgumentException("Message payment.spend illisible", e);
        }
    }

    public SpendEvent read(String json) {
        try {
            return mapper.readValue(json, SpendEvent.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Message payment.spend illisible", e);
        }
    }
}
