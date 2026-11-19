package com.gateway.payment.identity.service;

import com.gateway.payment.identity.domain.IdentityAppareil;
import com.gateway.payment.identity.domain.IdentityChannelUser;
import com.gateway.payment.identity.repository.IdentityAppareilRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppareilServiceTest {

    @Mock IdentityAppareilRepository appareils;
    @Mock SessionsKeycloak sessions;

    AppareilService service;
    IdentityChannelUser utilisateur;

    @BeforeEach
    void setUp() {
        service = new AppareilService(appareils, sessions);
        utilisateur = IdentityChannelUser.builder()
                .tenantId("CORIS_DEMO")
                .username("73061292")
                .partyId(UUID.randomUUID())
                .build();
        lenient().when(appareils.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(appareils.findByPartyIdOrderByLastSeenAtDesc(any())).thenReturn(List.of());
    }

    @Test
    void leMemeIdentifiantMetAJourLaFiche() {
        when(appareils.findByTenantIdAndAppareilId("CORIS_DEMO", "appareil-1")).thenReturn(Optional.empty());

        AppareilService.Vue cree = service.enregistrer(utilisateur, "session-1", "appareil-1", "Mac", "web");

        assertThat(cree.nom()).isEqualTo("Mac");
        assertThat(cree.plateforme()).isEqualTo("WEB");
        assertThat(cree.courant()).isTrue();
        assertThat(cree.statut()).isEqualTo("ACTIF");

        IdentityAppareil fiche = new IdentityAppareil();
        fiche.setTenantId("CORIS_DEMO");
        fiche.setPartyId(utilisateur.getPartyId());
        fiche.setAppareilId("appareil-1");
        fiche.setSessionId("session-1");
        fiche.setStatut(IdentityAppareil.Statut.ACTIF);
        when(appareils.findByTenantIdAndAppareilId("CORIS_DEMO", "appareil-1")).thenReturn(Optional.of(fiche));

        AppareilService.Vue suivi = service.enregistrer(utilisateur, "session-2", "appareil-1", "Mac", "WEB");

        assertThat(suivi.courant()).isTrue();
        verify(sessions).revoquer("session-1");
        ArgumentCaptor<IdentityAppareil> capteur = ArgumentCaptor.forClass(IdentityAppareil.class);
        verify(appareils, org.mockito.Mockito.atLeastOnce()).save(capteur.capture());
        assertThat(capteur.getAllValues().getLast().getSessionId()).isEqualTo("session-2");
    }

    @Test
    void revoquerLAppareilCourantExigeLOubli() {
        IdentityAppareil fiche = new IdentityAppareil();
        UUID id = UUID.randomUUID();
        fiche.setId(id);
        fiche.setPartyId(utilisateur.getPartyId());
        fiche.setSessionId("session-1");
        fiche.setStatut(IdentityAppareil.Statut.ACTIF);
        when(appareils.findById(id)).thenReturn(Optional.of(fiche));

        assertThatThrownBy(() -> service.revoquer(utilisateur, id, "session-1", false))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("celui de la session");
    }
}
