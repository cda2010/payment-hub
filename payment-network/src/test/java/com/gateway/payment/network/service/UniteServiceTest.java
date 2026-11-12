package com.gateway.payment.network.service;

import com.gateway.payment.network.NetworkException;
import com.gateway.payment.network.Reseau;
import com.gateway.payment.network.api.dto.NetworkDtos.CreationUniteRequest;
import com.gateway.payment.network.api.dto.NetworkDtos.ModificationUniteRequest;
import com.gateway.payment.network.api.dto.NetworkDtos.UniteDetail;
import com.gateway.payment.network.domain.RegroupementMembre;
import com.gateway.payment.network.domain.StatutUnite;
import com.gateway.payment.network.domain.UniteOrganisation;
import com.gateway.payment.network.domain.Zone;
import com.gateway.payment.network.repository.RegroupementMembreRepository;
import com.gateway.payment.network.repository.UniteOrganisationRepository;
import com.gateway.payment.network.repository.ZoneRepository;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

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
import static com.gateway.payment.network.domain.TypeUnite.PAYS;
import static com.gateway.payment.network.domain.TypeUnite.REGROUPEMENT;
import static com.gateway.payment.network.domain.TypeUnite.SOUS_DISTRIBUTEUR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UniteServiceTest {

    private final UniteOrganisationRepository unites = mock(UniteOrganisationRepository.class);
    private final RegroupementMembreRepository membres = mock(RegroupementMembreRepository.class);
    private final ZoneRepository zones = mock(ZoneRepository.class);
    private final UniteService service = new UniteService(unites, membres, new ZoneService(zones, unites));
    private final Map<UUID, UniteOrganisation> base = new HashMap<>();
    private final Map<String, Zone> referentiel = new HashMap<>();

    private UniteOrganisation pays;
    private UniteOrganisation fse;
    private UniteOrganisation agNord;
    private UniteOrganisation agSud;
    private UniteOrganisation master;
    private UniteOrganisation sd;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void reseau() {
        when(unites.findById(any())).thenAnswer(i -> Optional.ofNullable(base.get(i.<UUID>getArgument(0))));
        when(unites.findAllById(any())).thenAnswer(i -> ((Collection<UUID>) i.getArgument(0)).stream()
                .map(base::get).filter(Objects::nonNull).toList());
        when(unites.existsByCodeFse(anyString())).thenReturn(false);
        when(zones.findById(any())).thenAnswer(i -> Optional.ofNullable(referentiel.get(i.<String>getArgument(0))));
        zone("UEMOA", "XOF", StatutUnite.ACTIF);
        zone("CEMAC", "XAF", StatutUnite.ACTIF);
        zone("EAC", "KES", StatutUnite.INACTIF);
        pays = enregistrer(Reseau.pays("BF", "UEMOA"));
        fse = enregistrer(Reseau.sous(pays, FSE, "FSE-UEMOA"));
        agNord = enregistrer(Reseau.sous(fse, AGENCE, "AG-NORD"));
        agSud = enregistrer(Reseau.sous(fse, AGENCE, "AG-SUD"));
        master = enregistrer(Reseau.sous(agNord, MASTER, "MS-NORD"));
        sd = enregistrer(Reseau.sous(master, SOUS_DISTRIBUTEUR, "SD-01"));
    }

    // ── Création ────────────────────────────────────────────────────────────

    @Test
    void creationDUnSousDistributeurHeriteZoneNiveauEtChemin() {
        UniteDetail detail = service.creer(new CreationUniteRequest("sd-02", "SD 02", SOUS_DISTRIBUTEUR, null, master.getId(), null), "admin");

        ArgumentCaptor<UniteOrganisation> capture = ArgumentCaptor.forClass(UniteOrganisation.class);
        verify(unites).save(capture.capture());
        UniteOrganisation creee = capture.getValue();
        assertThat(creee.getCode()).isEqualTo("SD-02");
        assertThat(creee.getZone()).isEqualTo("UEMOA");
        assertThat(creee.getNiveau()).isEqualTo(5);
        assertThat(creee.getChemin()).isEqualTo(master.getChemin() + creee.getId() + "/");
        assertThat(creee.getStatut()).isEqualTo(StatutUnite.ACTIF);
        assertThat(detail.parent().code()).isEqualTo("MS-NORD");
        assertThat(detail.ascendants()).extracting(a -> a.code()).containsExactly("BF", "FSE-UEMOA", "AG-NORD", "MS-NORD");
    }

    @Test
    void unPaysSeCreeALaRacineDUneZoneActive() {
        UniteDetail detail = service.creer(new CreationUniteRequest("gh", "Ghana", PAYS, "cemac", null, null), "admin");

        ArgumentCaptor<UniteOrganisation> capture = ArgumentCaptor.forClass(UniteOrganisation.class);
        verify(unites).save(capture.capture());
        UniteOrganisation creee = capture.getValue();
        assertThat(creee.getCode()).isEqualTo("GH");
        assertThat(creee.getZone()).isEqualTo("CEMAC");
        assertThat(creee.getNiveau()).isEqualTo(1);
        assertThat(creee.getParentId()).isNull();
        assertThat(creee.getChemin()).isEqualTo("/" + creee.getId() + "/");
        assertThat(detail.devise()).isEqualTo("XAF");
    }

    @Test
    void unPaysExigeUnCodeIsoEtUneZoneActive() {
        erreur(() -> service.creer(new CreationUniteRequest("GHA", "Ghana", PAYS, "UEMOA", null, null), "admin"),
                "CODE_PAYS_INVALIDE");
        erreur(() -> service.creer(new CreationUniteRequest("GH", "Ghana", PAYS, null, null, null), "admin"),
                "ZONE_OBLIGATOIRE");
        erreur(() -> service.creer(new CreationUniteRequest("GH", "Ghana", PAYS, "ZZZ", null, null), "admin"),
                "ZONE_INEXISTANTE");
        erreur(() -> service.creer(new CreationUniteRequest("KE", "Kenya", PAYS, "EAC", null, null), "admin"),
                "ZONE_INACTIVE");
        erreur(() -> service.creer(new CreationUniteRequest("GH", "Ghana", PAYS, "UEMOA", pays.getId(), null), "admin"),
                "RATTACHEMENT_INTERDIT");
        verify(unites, never()).save(any());
    }

    @Test
    void unPaysNeSeReactiveQueDansUneZoneActive() {
        UniteOrganisation kenya = enregistrer(Reseau.pays("KE", "EAC"));
        kenya.setStatut(StatutUnite.INACTIF);

        erreur(() -> service.activer(kenya.getId(), "admin"), "ZONE_INACTIVE");

        referentiel.get("EAC").setStatut(StatutUnite.ACTIF);
        assertThat(service.activer(kenya.getId(), "admin").statut()).isEqualTo(StatutUnite.ACTIF);
    }

    @Test
    void unFseSeRattacheAUnPaysDontIlHeriteLaZone() {
        erreur(() -> service.creer(new CreationUniteRequest("FSE-2", "FSE 2", FSE, "UEMOA", null, "0019"), "admin"),
                "PARENT_OBLIGATOIRE");
        erreur(() -> service.creer(new CreationUniteRequest("FSE-2", "FSE 2", FSE, null, agNord.getId(), "0019"), "admin"),
                "RATTACHEMENT_INTERDIT");

        service.creer(new CreationUniteRequest("FSE-2", "FSE 2", FSE, null, pays.getId(), "0019"), "admin");

        ArgumentCaptor<UniteOrganisation> capture = ArgumentCaptor.forClass(UniteOrganisation.class);
        verify(unites).save(capture.capture());
        assertThat(capture.getValue().getZone()).isEqualTo("UEMOA");
        assertThat(capture.getValue().getCodeFse()).isEqualTo("0019");
        assertThat(capture.getValue().getNiveau()).isEqualTo(2);
        assertThat(capture.getValue().getChemin()).isEqualTo(pays.getChemin() + capture.getValue().getId() + "/");
    }

    @Test
    void uneFseExigeUnCodeFseOmnicanal() {
        erreur(() -> service.creer(new CreationUniteRequest("FSE-X", "FSE", FSE, null, pays.getId(), null), "admin"),
                "CODE_FSE_OBLIGATOIRE");
        erreur(() -> service.creer(new CreationUniteRequest("FSE-X", "FSE", FSE, null, pays.getId(), " "), "admin"),
                "CODE_FSE_OBLIGATOIRE");
        erreur(() -> service.creer(new CreationUniteRequest("AG-X", "Agence", AGENCE, null, fse.getId(), "0011"), "admin"),
                "CODE_FSE_INTERDIT");
        when(unites.existsByCodeFse("0011")).thenReturn(true);
        erreur(() -> service.creer(new CreationUniteRequest("FSE-X", "FSE", FSE, null, pays.getId(), "0011"), "admin"),
                "CODE_FSE_EXISTANT");
        verify(unites, never()).save(any());
    }

    @Test
    void rattachementHorsMatriceRefuse() {
        erreur(() -> service.creer(new CreationUniteRequest("MS-X", "Master", MASTER, null, fse.getId(), null), "admin"),
                "RATTACHEMENT_INTERDIT");
        erreur(() -> service.creer(new CreationUniteRequest("MC-X", "Marchand", MARCHAND, null, master.getId(), null), "admin"),
                "RATTACHEMENT_INTERDIT");
        erreur(() -> service.creer(new CreationUniteRequest("AG-X", "Agence", AGENCE, null, null, null), "admin"),
                "PARENT_OBLIGATOIRE");
    }

    @Test
    void zoneDifferenteDuParentRefusee() {
        erreur(() -> service.creer(new CreationUniteRequest("AG-X", "Agence", AGENCE, "CEMAC", fse.getId(), null), "admin"),
                "ZONE_INCOHERENTE");
    }

    @Test
    void parentInactifRefuse() {
        agSud.setStatut(StatutUnite.INACTIF);

        erreur(() -> service.creer(new CreationUniteRequest("MC-X", "Marchand", MARCHAND, null, agSud.getId(), null), "admin"),
                "PARENT_INACTIF");
    }

    @Test
    void codeExistantRefuse() {
        when(unites.existsByCode("AG-NORD")).thenReturn(true);

        assertThatThrownBy(() -> service.creer(new CreationUniteRequest("ag-nord", "Doublon", AGENCE, null, fse.getId(), null), "admin"))
                .isInstanceOfSatisfying(NetworkException.class, e -> {
                    assertThat(e.getCode()).isEqualTo("CODE_EXISTANT");
                    assertThat(e.getStatus().value()).isEqualTo(409);
                });
    }

    @Test
    void unRegroupementSeRattacheANImporteQuelNiveau() {
        erreur(() -> service.creer(new CreationUniteRequest("RG-X", "Rg", REGROUPEMENT, "UEMOA", null, null), "admin"),
                "PARENT_OBLIGATOIRE");

        service.creer(new CreationUniteRequest("RG-NORD", "Agences du Nord", REGROUPEMENT, null, fse.getId(), null), "admin");
        service.creer(new CreationUniteRequest("RG-EQUIPE", "Équipe", REGROUPEMENT, null, sd.getId(), null), "admin");

        ArgumentCaptor<UniteOrganisation> capture = ArgumentCaptor.forClass(UniteOrganisation.class);
        verify(unites, org.mockito.Mockito.times(2)).save(capture.capture());
        UniteOrganisation nord = capture.getAllValues().get(0);
        assertThat(nord.getParentId()).isEqualTo(fse.getId());
        assertThat(nord.getZone()).isEqualTo("UEMOA");
        assertThat(nord.getNiveau()).isEqualTo(3);
        assertThat(nord.getChemin()).isEqualTo(fse.getChemin() + nord.getId() + "/");
        assertThat(capture.getAllValues().get(1).getNiveau()).isEqualTo(6);
    }

    @Test
    void unRegroupementNAccueilleAucuneUnite() {
        UniteOrganisation rg = enregistrer(Reseau.regroupement("RG", fse));

        erreur(() -> service.creer(new CreationUniteRequest("AG-X", "Agence", AGENCE, null, rg.getId(), null), "admin"),
                "RATTACHEMENT_INTERDIT");
        erreur(() -> service.creer(new CreationUniteRequest("RG-2", "Rg", REGROUPEMENT, null, rg.getId(), null), "admin"),
                "RATTACHEMENT_INTERDIT");
    }

    // ── Modification ────────────────────────────────────────────────────────

    @Test
    void changementDeTypeRefuseSiLesEnfantsNePeuventPlusEnDependre() {
        when(unites.findByParentIdOrderByCode(master.getId())).thenReturn(List.of(sd));

        erreur(() -> service.modifier(master.getId(), new ModificationUniteRequest("Master", MARCHAND), "admin"),
                "TYPE_INCOMPATIBLE_ENFANTS");
        assertThat(master.getType()).isEqualTo(MASTER);
    }

    @Test
    void unMasterSansEnfantPeutDevenirMarchand() {
        UniteDetail detail = service.modifier(master.getId(), new ModificationUniteRequest("Marchand Nord", MARCHAND), "admin");

        assertThat(detail.type()).isEqualTo(MARCHAND);
        assertThat(detail.libelle()).isEqualTo("Marchand Nord");
    }

    @Test
    void unRegroupementResteUnRegroupementEtUnPaysResteUnPays() {
        UniteOrganisation rg = enregistrer(Reseau.regroupement("RG", fse));

        erreur(() -> service.modifier(rg.getId(), new ModificationUniteRequest("Rg", AGENCE), "admin"),
                "TYPE_NON_MODIFIABLE");
        erreur(() -> service.modifier(pays.getId(), new ModificationUniteRequest("Burkina", FSE), "admin"),
                "TYPE_NON_MODIFIABLE");
        erreur(() -> service.modifier(fse.getId(), new ModificationUniteRequest("Fse", PAYS), "admin"),
                "TYPE_NON_MODIFIABLE");
        erreur(() -> service.modifier(fse.getId(), new ModificationUniteRequest("Fse", AGENCE), "admin"),
                "TYPE_NON_MODIFIABLE");
    }

    @Test
    void leLibelleDUnPaysSeModifie() {
        assertThat(service.modifier(pays.getId(), new ModificationUniteRequest("Burkina Faso", PAYS), "admin").libelle())
                .isEqualTo("Burkina Faso");
    }

    @Test
    void unRegroupementSeDeplaceSiSesMembresRestentDansLaBranche() {
        UniteOrganisation rg = enregistrer(Reseau.regroupement("RG", fse));
        when(membres.findByRegroupementId(rg.getId())).thenReturn(List.of(membre(rg, agSud)));

        erreur(() -> service.deplacer(rg.getId(), agNord.getId(), "admin"), "MEMBRE_HORS_BRANCHE");

        String ancien = rg.getChemin();
        service.deplacer(rg.getId(), agSud.getId(), "admin");
        verify(unites).deplacerBranche(eq(ancien), eq(ancien.length()), eq(agSud.getChemin() + rg.getId() + "/"),
                eq(1), any(), eq("admin"));
    }

    // ── Déplacement ─────────────────────────────────────────────────────────

    @Test
    void deplacementDUneBrancheRecalculeLesChemins() {
        when(membres.findDansBranche(master.getChemin())).thenReturn(List.of());
        String ancien = master.getChemin();

        service.deplacer(master.getId(), agSud.getId(), "admin");

        verify(unites).deplacerBranche(eq(ancien), eq(ancien.length()), eq(agSud.getChemin() + master.getId() + "/"),
                eq(0), any(), eq("admin"));
        assertThat(master.getParentId()).isEqualTo(agSud.getId());
    }

    @Test
    void deplacementSousUnTypeNonAutoriseRefuse() {
        erreur(() -> service.deplacer(master.getId(), fse.getId(), "admin"), "RATTACHEMENT_INTERDIT");
        erreur(() -> service.deplacer(pays.getId(), agNord.getId(), "admin"), "RATTACHEMENT_INTERDIT");
        verify(unites, never()).deplacerBranche(anyString(), anyInt(), anyString(), anyInt(), any(), anyString());
    }

    @Test
    void unFseNeChangePasDePays() {
        UniteOrganisation mali = enregistrer(Reseau.pays("ML", "UEMOA"));

        erreur(() -> service.deplacer(fse.getId(), mali.getId(), "admin"), "FSE_NON_DEPLACABLE");
        verify(unites, never()).deplacerBranche(anyString(), anyInt(), anyString(), anyInt(), any(), anyString());
    }

    @Test
    void deplacementVersUneAutreZoneRefuse() {
        UniteOrganisation fseCemac = enregistrer(Reseau.sous(enregistrer(Reseau.pays("CM", "CEMAC")), FSE, "FSE-CEMAC"));
        UniteOrganisation agDouala = enregistrer(Reseau.sous(fseCemac, AGENCE, "AG-DOUALA"));

        erreur(() -> service.deplacer(master.getId(), agDouala.getId(), "admin"), "ZONE_INCOHERENTE");
    }

    @Test
    void deplacementQuiSortiraitUnMembreDeLaBrancheDeSonRegroupementRefuse() {
        UniteOrganisation rg = enregistrer(Reseau.regroupement("RG-EQUIPE", agNord));
        RegroupementMembre m = membre(rg, master);
        when(membres.findDansBranche(master.getChemin())).thenReturn(List.of(m));
        when(membres.findByRegroupementIdIn(any())).thenReturn(List.of(m));

        erreur(() -> service.deplacer(master.getId(), agSud.getId(), "admin"), "SORTIE_BRANCHE_REGROUPEMENT");
    }

    @Test
    void unRegroupementSousLaBrancheDeplaceeLaSuit() {
        UniteOrganisation rg = enregistrer(Reseau.regroupement("RG-EQUIPE", master));
        RegroupementMembre m = membre(rg, sd);
        when(membres.findDansBranche(master.getChemin())).thenReturn(List.of(m));
        when(membres.findByRegroupementIdIn(any())).thenReturn(List.of(m));

        service.deplacer(master.getId(), agSud.getId(), "admin");
        verify(unites).deplacerBranche(eq(master.getChemin()), anyInt(), anyString(), eq(0), any(), eq("admin"));
    }

    @Test
    void deplacementQuiCouvriraitDeuxFoisUneUniteRefuse() {
        UniteOrganisation marchand = enregistrer(Reseau.sous(agNord, MARCHAND, "MC-NORD"));
        UniteOrganisation rg = enregistrer(Reseau.regroupement("RG", fse));
        List<RegroupementMembre> appartenances = List.of(membre(rg, agSud), membre(rg, marchand));
        when(membres.findDansBranche(marchand.getChemin())).thenReturn(List.of(appartenances.get(1)));
        when(membres.findByRegroupementIdIn(any())).thenReturn(appartenances);

        erreur(() -> service.deplacer(marchand.getId(), agSud.getId(), "admin"), "DOUBLON_COUVERTURE");
    }

    // ── Activation ──────────────────────────────────────────────────────────

    @Test
    void desactivationRefuseeSIlResteDesEnfantsActifs() {
        when(unites.existsByParentIdAndStatut(master.getId(), StatutUnite.ACTIF)).thenReturn(true);

        erreur(() -> service.desactiver(master.getId(), "admin"), "ENFANTS_ACTIFS");
        assertThat(master.getStatut()).isEqualTo(StatutUnite.ACTIF);
    }

    @Test
    void desactivationPuisReactivation() {
        assertThat(service.desactiver(sd.getId(), "admin").statut()).isEqualTo(StatutUnite.INACTIF);
        assertThat(service.activer(sd.getId(), "admin").statut()).isEqualTo(StatutUnite.ACTIF);
    }

    @Test
    void reactivationRefuseeSousUnParentInactif() {
        sd.setStatut(StatutUnite.INACTIF);
        master.setStatut(StatutUnite.INACTIF);

        erreur(() -> service.activer(sd.getId(), "admin"), "PARENT_INACTIF");
    }

    // ── Outils ──────────────────────────────────────────────────────────────

    private UniteOrganisation enregistrer(UniteOrganisation u) {
        base.put(u.getId(), u);
        return u;
    }

    private void zone(String code, String devise, StatutUnite statut) {
        referentiel.put(code, Zone.builder().code(code).libelle(code).devise(devise).statut(statut).build());
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
