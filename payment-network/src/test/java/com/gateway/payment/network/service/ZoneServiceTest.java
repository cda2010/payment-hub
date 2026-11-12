package com.gateway.payment.network.service;

import com.gateway.payment.network.NetworkException;
import com.gateway.payment.network.api.dto.NetworkDtos.CreationZoneRequest;
import com.gateway.payment.network.api.dto.NetworkDtos.ModificationZoneRequest;
import com.gateway.payment.network.api.dto.NetworkDtos.ZoneDto;
import com.gateway.payment.network.domain.StatutUnite;
import com.gateway.payment.network.domain.TypeUnite;
import com.gateway.payment.network.domain.Zone;
import com.gateway.payment.network.repository.UniteOrganisationRepository;
import com.gateway.payment.network.repository.ZoneRepository;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ZoneServiceTest {

    private final ZoneRepository zones = mock(ZoneRepository.class);
    private final UniteOrganisationRepository unites = mock(UniteOrganisationRepository.class);
    private final ZoneService service = new ZoneService(zones, unites);
    private final Map<String, Zone> base = new HashMap<>();

    @BeforeEach
    void referentiel() {
        when(zones.findById(any())).thenAnswer(i -> Optional.ofNullable(base.get(i.<String>getArgument(0))));
        when(zones.existsById(any())).thenAnswer(i -> base.containsKey(i.<String>getArgument(0)));
        when(zones.save(any())).thenAnswer(i -> {
            Zone z = i.getArgument(0);
            base.put(z.getCode(), z);
            return z;
        });
        service.creer(new CreationZoneRequest("UEMOA", "Union", "XOF"), "flyway");
    }

    @Test
    void uneZoneSeCreeActiveAvecSonCodeEtSaDeviseEnMajuscules() {
        ZoneDto zone = service.creer(new CreationZoneRequest(" eac ", " Afrique de l'Est ", "kes"), "admin");

        assertThat(zone.code()).isEqualTo("EAC");
        assertThat(zone.libelle()).isEqualTo("Afrique de l'Est");
        assertThat(zone.devise()).isEqualTo("KES");
        assertThat(zone.statut()).isEqualTo(StatutUnite.ACTIF);
        assertThat(zone.createdBy()).isEqualTo("admin");
    }

    @Test
    void leCodeDUneZoneEstUnique() {
        erreur(() -> service.creer(new CreationZoneRequest("uemoa", "Doublon", "XOF"), "admin"), "ZONE_EXISTANTE");
    }

    @Test
    void laDeviseSeFigeDesQueLaZoneContientUneUnite() {
        assertThat(service.modifier("UEMOA", new ModificationZoneRequest("UEMOA", "eur"), "admin").devise())
                .isEqualTo("EUR");

        when(unites.countByZone("UEMOA")).thenReturn(3L);
        erreur(() -> service.modifier("UEMOA", new ModificationZoneRequest("UEMOA", "XOF"), "admin"), "DEVISE_FIGEE");
        assertThat(service.modifier("UEMOA", new ModificationZoneRequest("Union ouest-africaine", "EUR"), "admin").libelle())
                .isEqualTo("Union ouest-africaine");
    }

    @Test
    void uneZoneNeSeDesactiveQueSansPaysActif() {
        when(unites.existsByZoneAndTypeAndStatut("UEMOA", TypeUnite.PAYS, StatutUnite.ACTIF)).thenReturn(true);
        erreur(() -> service.desactiver("UEMOA", "admin"), "ZONE_AVEC_PAYS_ACTIFS");

        when(unites.existsByZoneAndTypeAndStatut("UEMOA", TypeUnite.PAYS, StatutUnite.ACTIF)).thenReturn(false);
        assertThat(service.desactiver("UEMOA", "admin").statut()).isEqualTo(StatutUnite.INACTIF);
        erreur(() -> service.exigerActive("UEMOA"), "ZONE_INACTIVE");

        assertThat(service.activer("uemoa", "admin").statut()).isEqualTo(StatutUnite.ACTIF);
    }

    @Test
    void zoneInconnue() {
        erreur(() -> service.consulter("XYZ"), "ZONE_INEXISTANTE");
    }

    private static void erreur(ThrowingCallable appel, String code) {
        assertThatThrownBy(appel).isInstanceOf(NetworkException.class).hasFieldOrPropertyWithValue("code", code);
    }
}
