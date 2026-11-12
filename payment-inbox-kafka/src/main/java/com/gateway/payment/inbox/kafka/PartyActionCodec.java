package com.gateway.payment.inbox.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * JSON du message Kafka, partagé par l'émetteur et le consommateur.
 */
public final class PartyActionCodec {

    private final ObjectMapper mapper;

    public PartyActionCodec() {
        this.mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public String write(PartyActionEvent event) {
        try {
            return mapper.writeValueAsString(event);
        } catch (Exception e) {
            throw new IllegalArgumentException("Message party.action illisible", e);
        }
    }

    public PartyActionEvent read(String json) {
        try {
            return mapper.readValue(json, PartyActionEvent.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Message party.action illisible", e);
        }
    }
}
