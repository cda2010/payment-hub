package com.gateway.payment.network.service;

import com.gateway.payment.network.NetworkException;
import com.gateway.payment.network.Reseau;
import com.gateway.payment.network.api.dto.NetworkDtos.CouvertureDto;
import com.gateway.payment.network.domain.RegroupementMembre;
import com.gateway.payment.network.domain.StatutUnite;
import com.gateway.payment.network.domain.UniteOrganisation;
import com.gateway.payment.network.repository.RegroupementMembreRepository;
import com.gateway.payment.network.repository.UniteOrganisationRepository;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static com.gateway.payment.network.domain.TypeUnite.AGENCE;
import static com.gateway.payment.network.domain.TypeUnite.FSE;
import static com.gateway.payment.network.domain.TypeUnite.MARCHAND;
import static com.gateway.payment.network.domain.TypeUnite.MASTER;
import static com.gateway.payment.network.domain.TypeUnite.SOUS_DISTRIBUTEUR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RegroupementServiceTest {

    private final UniteOrganisationRepository unites = mock(UniteOrganisationRepository.class);
    private final RegroupementMembreRepository membres = mock(RegroupementMembreRepository.class);
    private final RegroupementService service = new RegroupementService(unites, membres);
    private final Map<UUID, UniteOrganisation> base = new HashMap<>();
    private final List<RegroupementMembre> appartenances = new ArrayList<>();

    private UniteOrganisation burkina;
    private UniteOrganisation fse;
    private UniteOrganisation agA;
    private UniteOrganisation agB;
    private UniteOrganisation agC;
    private UniteOrganisation masterA;
    private UniteOrganisation sdA;
    private UniteOrganisation nord;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void reseau() {
        when(unites.findById(any())).thenAnswer(i -> Optional.ofNullable(base.get(i.<UUID>getArgument(0))));
        when(unites.findAllById(any())).thenAnswer(i -> ((Collection<UUID>) i.getArgument(0)).stream()
                .map(base::get).filter(Objects::nonNull).toList());
        when(membres.findByRegroupementId(any())).thenAnswer(i -> appartenances.stream()
                .filter(m -> m.getRegroupementId().equals(i.getArgument(0))).toList());
        when(membres.findByRegroupementIdAndUniteId(any(), any())).thenAnswer(i -> appartenances.stream()
                .filter(m -> m.getRegroupementId().equals(i.getArgument(0)) && m.getUniteId().equals(i.getArgument(1)))
                .findFirst());
        when(membres.findByUniteIdIn(any())).thenAnswer(i -> appartenances.stream()
                .filter(m -> ((Collection<UUID>) i.getArgument(0)).contains(m.getUniteId())).toList());

        burkina = enregistrer(Reseau.pays("BF", "UEMOA"));
        fse = enregistrer(Reseau.sous(burkina, FSE, "FSE-UEMOA"));
        agA = enregistrer(Reseau.sous(fse, AGENCE, "AG-A"));
        agB = enregistrer(Reseau.sous(fse, AGENCE, "AG-B"));
        agC = enregistrer(Reseau.sous(fse, AGENCE, "AG-C"));
        masterA = enregistrer(Reseau.sous(agA, MASTER, "MS-A"));
        sdA = enregistrer(Reseau.sous(masterA, SOUS_DISTRIBUTEUR, "SD-A"));
        nord = enregistrer(Reseau.regroupement("RG-NORD", fse));
        appartenances.add(membre(nord, agA));
        appartenances.add(membre(nord, agB));
    }

    @Test
    void regrouperDesAgencesDontChacuneGardeSesFilles() {
        service.ajouterMembre(nord.getId(), agC.getId(), "admin");

        ArgumentCaptor<RegroupementMembre> capture = ArgumentCaptor.forClass(RegroupementMembre.class);
        verify(membres).save(capture.capture());
        assertThat(capture.getValue().getUniteId()).isEqualTo(agC.getId());
        assertThat(capture.getValue().getAjoutePar()).isEqualTo("admin");
        assertThat(agC.getParentId()).isEqualTo(fse.getId());
    }

    @Test
    void lesNiveauxPeuventEtreMelanges() {
        UniteOrganisation transversal = enregistrer(Reseau.regroupement("RG-PREMIUM", fse));
        UniteOrganisation marchandB = enregistrer(Reseau.sous(agB, MARCHAND, "MC-B"));
        appartenances.add(membre(transversal, masterA));

        service.ajouterMembre(transversal.getId(), marchandB.getId(), "admin");

        verify(membres).save(any());
    }

    @Test
    void uniteDejaCouverteParUneAgenceMembreRefusee() {
        erreur(() -> service.ajouterMembre(nord.getId(), sdA.getId(), "admin"), "DOUBLON_COUVERTURE");
    }

    @Test
    void ascendantDUnMembreRefuse() {
        UniteOrganisation rg = enregistrer(Reseau.regroupement("RG-MASTERS", fse));
        appartenances.add(membre(rg, masterA));

        erreur(() -> service.ajouterMembre(rg.getId(), agA.getId(), "admin"), "DOUBLON_COUVERTURE");
    }

    @Test
    void lesMembresSontPrisDansLaBrancheDuParent() {
        UniteOrganisation equipe = enregistrer(Reseau.regroupement("RG-EQUIPE", masterA));

        erreur(() -> service.ajouterMembre(equipe.getId(), agB.getId(), "admin"), "MEMBRE_HORS_BRANCHE");
        erreur(() -> service.ajouterMembre(equipe.getId(), agA.getId(), "admin"), "MEMBRE_HORS_BRANCHE");
        service.ajouterMembre(equipe.getId(), sdA.getId(), "admin");
        verify(membres).save(any());
    }

    @Test
    void leParentPeutEtreMembreDeSonRegroupement() {
        UniteOrganisation equipe = enregistrer(Reseau.regroupement("RG-EQUIPE", masterA));

        service.ajouterMembre(equipe.getId(), masterA.getId(), "admin");
        appartenances.add(membre(equipe, masterA));

        erreur(() -> service.ajouterMembre(equipe.getId(), sdA.getId(), "admin"), "DOUBLON_COUVERTURE");
    }

    @Test
    void autresRefus() {
        UniteOrganisation autre = enregistrer(Reseau.regroupement("RG-AUTRE", fse));
        UniteOrganisation fseCemac = enregistrer(Reseau.sous(enregistrer(Reseau.pays("CM", "CEMAC")), FSE, "FSE-CEMAC"));
        UniteOrganisation transversal = enregistrer(Reseau.regroupement("RG-T", fse));

        erreur(() -> service.ajouterMembre(transversal.getId(), autre.getId(), "admin"), "MEMBRE_REGROUPEMENT_INTERDIT");
        erreur(() -> service.ajouterMembre(transversal.getId(), fseCemac.getId(), "admin"), "ZONE_INCOHERENTE");
        erreur(() -> service.ajouterMembre(agA.getId(), agB.getId(), "admin"), "PAS_UN_REGROUPEMENT");
        assertThatThrownBy(() -> service.ajouterMembre(nord.getId(), agA.getId(), "admin"))
                .isInstanceOfSatisfying(NetworkException.class, e -> assertThat(e.getStatus().value()).isEqualTo(409));
        agC.setStatut(StatutUnite.INACTIF);
        erreur(() -> service.ajouterMembre(nord.getId(), agC.getId(), "admin"), "UNITE_INACTIVE");
    }

    @Test
    void unRegroupementSousUnPaysPeutCouvrirToutSonReseau() {
        UniteOrganisation mali = enregistrer(Reseau.pays("ML", "UEMOA"));
        UniteOrganisation reseauBf = enregistrer(Reseau.regroupement("RG-BF", burkina));

        service.ajouterMembre(reseauBf.getId(), burkina.getId(), "admin");
        verify(membres).save(any());
        appartenances.add(membre(reseauBf, burkina));

        erreur(() -> service.ajouterMembre(reseauBf.getId(), agC.getId(), "admin"), "DOUBLON_COUVERTURE");
        erreur(() -> service.ajouterMembre(reseauBf.getId(), mali.getId(), "admin"), "MEMBRE_HORS_BRANCHE");
        assertThat(service.couvertures(sdA.getId())).extracting(c -> c.regroupement().code() + " via " + c.via().code())
                .containsExactlyInAnyOrder("RG-NORD via AG-A", "RG-BF via BF");
    }

    @Test
    void couvertureParUnAscendantMembre() {
        List<CouvertureDto> couvertures = service.couvertures(sdA.getId());

        assertThat(couvertures).singleElement().satisfies(c -> {
            assertThat(c.regroupement().code()).isEqualTo("RG-NORD");
            assertThat(c.via().code()).isEqualTo("AG-A");
            assertThat(c.direct()).isFalse();
        });
        assertThat(service.couvertures(agA.getId())).singleElement().satisfies(c -> assertThat(c.direct()).isTrue());
        assertThat(service.couvertures(agC.getId())).isEmpty();
    }

    private UniteOrganisation enregistrer(UniteOrganisation u) {
        base.put(u.getId(), u);
        return u;
    }

    private static RegroupementMembre membre(UniteOrganisation regroupement, UniteOrganisation unite) {
        return RegroupementMembre.builder()
                .id(UUID.randomUUID())
                .regroupementId(regroupement.getId())
                .uniteId(unite.getId())
                .ajoutePar("test")
                .build();
    }

    private static void erreur(ThrowingCallable appel, String code) {
        assertThatThrownBy(appel).isInstanceOfSatisfying(NetworkException.class,
                e -> assertThat(e.getCode()).isEqualTo(code));
    }
}
