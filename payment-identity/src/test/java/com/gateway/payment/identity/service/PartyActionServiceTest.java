package com.gateway.payment.identity.service;

import com.gateway.payment.identity.api.dto.PartyActionDtos.OpenActionRequest;
import com.gateway.payment.identity.api.dto.PartyActionDtos.PartyActionView;
import com.gateway.payment.identity.domain.ActionType;
import com.gateway.payment.identity.domain.PartyAction;
import com.gateway.payment.identity.domain.PartyActionStatus;
import com.gateway.payment.identity.repository.ActionTypeRepository;
import com.gateway.payment.identity.repository.PartyActionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PartyActionServiceTest {

    @Mock
    PartyActionRepository repository;

    @Mock
    ActionTypeRepository types;

    PartyActionService service;

    final UUID partyId = UUID.fromString("a1000001-0000-4000-8000-000000000006");

    @BeforeEach
    void setUp() {
        service = new PartyActionService(repository, types);
    }

    @Test
    void openUsesCatalogRoute() {
        ActionType kyc = ActionType.builder()
                .code("KYC_RENEW")
                .title("Pièce bientôt expirée")
                .subtitle("Mettez à jour votre dossier KYC")
                .icon("id-card-outline")
                .route("/kyc")
                .priority(20)
                .build();
        when(types.findById("KYC_RENEW")).thenReturn(Optional.of(kyc));
        when(repository.findOpen(partyId, "KYC_RENEW", "party", partyId.toString())).thenReturn(Optional.empty());
        when(repository.save(any(PartyAction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PartyActionView view = service.open(partyId, new OpenActionRequest(
                "CORIS_DEMO",
                "kyc_renew",
                null,
                "party",
                partyId.toString(),
                null,
                null
        ));

        assertThat(view.actionType()).isEqualTo("KYC_RENEW");
        assertThat(view.title()).isEqualTo("Pièce bientôt expirée");
        assertThat(view.route()).isEqualTo("/kyc");
        assertThat(view.priority()).isEqualTo(20);
        assertThat(view.subtitle()).isEqualTo("Mettez à jour votre dossier KYC");
        assertThat(view.status()).isEqualTo(PartyActionStatus.OPEN);
        verify(repository).save(any(PartyAction.class));
    }

    @Test
    void openResolvesAggregateInRoute() {
        ActionType docs = ActionType.builder()
                .code("DOC_REQUIRED")
                .title("Pièces justificatives manquantes")
                .subtitle("Déposez les documents demandés")
                .icon("document-attach-outline")
                .route("/journey/{aggregateId}")
                .routeFallback("/tabs/home")
                .priority(25)
                .build();
        when(types.findById("DOC_REQUIRED")).thenReturn(Optional.of(docs));
        when(repository.findOpen(partyId, "DOC_REQUIRED", "payment", "PAY-1")).thenReturn(Optional.empty());
        when(repository.save(any(PartyAction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PartyActionView view = service.open(partyId, new OpenActionRequest(
                null, "DOC_REQUIRED", "RIB manquant", "payment", "PAY-1", null, null
        ));

        assertThat(view.route()).isEqualTo("/journey/PAY-1");
        assertThat(view.subtitle()).isEqualTo("RIB manquant");
    }
}
