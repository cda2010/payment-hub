package com.gateway.payment.identity.inbox;

import com.gateway.payment.identity.api.dto.PartyActionDtos.CompleteActionRequest;
import com.gateway.payment.identity.api.dto.PartyActionDtos.OpenActionRequest;
import com.gateway.payment.identity.service.PartyActionService;
import com.gateway.payment.inbox.kafka.PartyActionCodec;
import com.gateway.payment.inbox.kafka.PartyActionEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PartyActionKafkaListenerTest {

    @Mock
    PartyActionService actions;

    PartyActionKafkaListener listener;

    final PartyActionCodec codec = new PartyActionCodec();
    final UUID partyId = UUID.fromString("a1000001-0000-4000-8000-000000000006");

    @BeforeEach
    void setUp() {
        listener = new PartyActionKafkaListener(actions, codec);
    }

    @Test
    void openMessageCreatesTheRow() {
        String json = codec.write(PartyActionEvent.open(
                partyId, "CORIS_DEMO", "KYC_RENEW", "party", partyId.toString(), null, null));

        listener.onMessage(json);

        ArgumentCaptor<OpenActionRequest> request = ArgumentCaptor.forClass(OpenActionRequest.class);
        verify(actions).open(eq(partyId), request.capture());
        assertThat(request.getValue().actionType()).isEqualTo("KYC_RENEW");
        assertThat(request.getValue().tenantId()).isEqualTo("CORIS_DEMO");
        assertThat(request.getValue().aggregateId()).isEqualTo(partyId.toString());
    }

    @Test
    void completeMessageClosesTheRow() {
        String json = codec.write(PartyActionEvent.complete(
                partyId, null, "FIRST_OP", "party", partyId.toString(), "DONE"));

        listener.onMessage(json);

        ArgumentCaptor<CompleteActionRequest> request = ArgumentCaptor.forClass(CompleteActionRequest.class);
        verify(actions).complete(eq(partyId), request.capture());
        assertThat(request.getValue().actionType()).isEqualTo("FIRST_OP");
        assertThat(request.getValue().status()).isEqualTo("DONE");
    }

    @Test
    void unknownTypeDoesNotWrite() {
        listener.onMessage("{\"eventType\":\"party.action.other\",\"partyId\":\"" + partyId + "\",\"actionType\":\"KYC_RENEW\"}");

        verify(actions, never()).open(any(), any());
        verify(actions, never()).complete(any(), any());
    }

    @Test
    void unknownActionCodeIsDropped() {
        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Type d'action inconnu"))
                .when(actions).open(eq(partyId), any());
        String json = codec.write(PartyActionEvent.open(
                partyId, null, "INCONNU", "party", partyId.toString(), null, null));

        assertThatCode(() -> listener.onMessage(json)).doesNotThrowAnyException();
    }

    @Test
    void brokenJsonIsDropped() {
        assertThatCode(() -> listener.onMessage("{pas du json")).doesNotThrowAnyException();
        verify(actions, never()).open(any(), any());
    }
}
