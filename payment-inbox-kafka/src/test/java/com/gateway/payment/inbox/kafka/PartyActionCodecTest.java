package com.gateway.payment.inbox.kafka;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PartyActionCodecTest {

    private final PartyActionCodec codec = new PartyActionCodec();

    @Test
    void roundTripOpen() {
        UUID partyId = UUID.fromString("a1000001-0000-4000-8000-000000000006");
        Instant expiresAt = Instant.parse("2026-11-24T10:15:56Z");
        PartyActionEvent event = PartyActionEvent.open(
                partyId, "CORIS_DEMO", "COTISATION_DUE", "cotisation", "SEED-COTIS-BATCH",
                "Échéance de novembre", expiresAt);

        PartyActionEvent read = codec.read(codec.write(event));

        assertThat(read.eventType()).isEqualTo(PartyActionEvent.OPEN);
        assertThat(read.partyId()).isEqualTo(partyId);
        assertThat(read.actionType()).isEqualTo("COTISATION_DUE");
        assertThat(read.aggregateId()).isEqualTo("SEED-COTIS-BATCH");
        assertThat(read.subtitle()).isEqualTo("Échéance de novembre");
        assertThat(read.expiresAt()).isEqualTo(expiresAt);
        assertThat(read.opening()).isTrue();
    }

    @Test
    void roundTripComplete() {
        UUID partyId = UUID.fromString("a1000001-0000-4000-8000-000000000006");
        PartyActionEvent event = PartyActionEvent.complete(
                partyId, null, "FIRST_OP", "party", partyId.toString(), null);

        PartyActionEvent read = codec.read(codec.write(event));

        assertThat(read.eventType()).isEqualTo(PartyActionEvent.COMPLETE);
        assertThat(read.status()).isEqualTo("DONE");
        assertThat(read.opening()).isFalse();
    }
}
