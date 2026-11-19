package com.gateway.payment.network.service;

import com.gateway.payment.network.NetworkException;
import com.gateway.payment.network.connexion.ConnexionGuichet;
import com.gateway.payment.network.envoi.EnvoiAcces;
import com.gateway.payment.network.alias.AliasLecture;
import com.gateway.payment.network.alias.AliasLecture.AliasVu;
import com.gateway.payment.network.api.dto.GuichetDtos.CreationUtilisateurRequest;
import com.gateway.payment.network.api.dto.GuichetDtos.MouvementRequest;
import com.gateway.payment.network.domain.StatutUnite;
import com.gateway.payment.network.domain.TypeUnite;
import com.gateway.payment.network.domain.UniteOrganisation;
import com.gateway.payment.network.domain.UniteUtilisateur;
import com.gateway.payment.network.api.dto.GuichetDtos.AffectationRequest;
import com.gateway.payment.network.api.dto.GuichetDtos.DesaffectationRequest;
import com.gateway.payment.network.domain.AffectationCaissier;
import com.gateway.payment.network.domain.Caisse;
import com.gateway.payment.network.domain.PointVente;
import com.gateway.payment.network.domain.PositionGuichet;
import com.gateway.payment.network.repository.AffectationCaissierRepository;
import com.gateway.payment.network.repository.CaisseRepository;
import com.gateway.payment.network.repository.MouvementAliasRepository;
import com.gateway.payment.network.repository.OperationCaisseRepository;
import com.gateway.payment.network.repository.PointVenteRepository;
import com.gateway.payment.network.repository.PositionGuichetRepository;
import com.gateway.payment.network.repository.UniteOrganisationRepository;
import com.gateway.payment.network.repository.UniteUtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GuichetServiceTest {

    @Mock UniteOrganisationRepository unites;
    @Mock UniteUtilisateurRepository utilisateurs;
    @Mock PointVenteRepository points;
    @Mock CaisseRepository caisses;
    @Mock MouvementAliasRepository mouvements;
    @Mock PositionGuichetRepository positions;
    @Mock OperationCaisseRepository operations;
    @Mock AliasLecture alias;
    @Mock ConnexionGuichet connexion;
    @Mock EnvoiAcces envoi;
    @Mock AffectationCaissierRepository affectations;

    GuichetService service;
    UUID uniteId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new GuichetService(unites, utilisateurs, points, caisses, mouvements, positions, operations, alias, connexion, envoi, affectations, new ContexteUnite());
        lenient().when(unites.findById(uniteId)).thenReturn(Optional.of(UniteOrganisation.builder()
                .id(uniteId)
                .type(TypeUnite.MARCHAND)
                .statut(StatutUnite.ACTIF)
                .build()));
    }

    @Test
    void unSeulAdminActif() {
        when(utilisateurs.existsByUniteIdAndRoleAndStatut(uniteId, "ADMIN", "ACTIF")).thenReturn(true);

        assertThatThrownBy(() -> service.creerUtilisateur(
                uniteId, new CreationUtilisateurRequest("awa.admin", "Awa", "ADMIN", "SMS", "22670001122", null), "test"))
                .isInstanceOf(NetworkException.class)
                .hasMessageContaining("administrateur");
    }

    @Test
    void leLoginNestPasUnTelephone() {
        assertThatThrownBy(() -> service.creerUtilisateur(
                uniteId, new CreationUtilisateurRequest("70000001", "Awa", "CAISSIER", "SMS", "22670001122", null), "test"))
                .isInstanceOf(NetworkException.class)
                .hasMessageContaining("téléphone");
    }

    @Test
    void leMotDePasseTemporaireEstEnvoye() {
        when(utilisateurs.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var dto = service.creerUtilisateur(uniteId, new CreationUtilisateurRequest(
                "Awa.Caisse", "Awa Caisse", "CAISSIER", "SMS", "22670001122", null), "test");

        ArgumentCaptor<String> secret = ArgumentCaptor.forClass(String.class);
        verify(connexion).ouvrir(any(), eq(uniteId), eq("awa.caisse"), eq("Awa Caisse"), eq("CAISSIER"),
                eq("MERCHANT"), secret.capture(), isNull());
        verify(envoi).envoyer(eq("SMS"), eq("22670001122"), eq("awa.caisse"), eq(secret.getValue()));
        assertThat(secret.getValue()).hasSizeGreaterThanOrEqualTo(12);
        assertThat(dto.identifiant()).isEqualTo("awa.caisse");
        assertThat(dto.canal()).isEqualTo("SMS");
        assertThat(dto.telephone()).isEqualTo("22670001122");
        assertThat(dto.nouveauCompte()).isTrue();
    }

    @Test
    void unLoginDejaConnuEstRattacheSansNouveauMotDePasse() {
        UUID autre = UUID.randomUUID();
        when(unites.findById(autre)).thenReturn(Optional.of(UniteOrganisation.builder()
                .id(autre).type(TypeUnite.AGENCE).statut(StatutUnite.ACTIF).code("AG-2").libelle("Agence 2").build()));
        when(utilisateurs.findAllByIdentifiantIgnoreCase("awa.caisse")).thenReturn(List.of(UniteUtilisateur.builder()
                .id(UUID.randomUUID()).uniteId(uniteId).identifiant("awa.caisse").role("CAISSIER").statut("ACTIF").build()));
        when(utilisateurs.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var dto = service.creerUtilisateur(autre, new CreationUtilisateurRequest(
                "awa.caisse", "Awa Caisse", "CAISSIER", "SMS", "22670001122", null), "test");

        verify(connexion).rattacher("awa.caisse", "CAISSIER");
        verify(connexion, never()).ouvrir(any(), any(), any(), any(), any(), any(), any(), any());
        verify(envoi, never()).envoyer(any(), any(), any(), any());
        assertThat(dto.uniteId()).isEqualTo(autre);
        assertThat(dto.nouveauCompte()).isFalse();
    }

    @Test
    void deuxUnitesDemandentUnChoix() {
        UUID autre = UUID.randomUUID();
        when(utilisateurs.findAllByIdentifiantIgnoreCase("awa.caisse")).thenReturn(List.of(
                UniteUtilisateur.builder().id(UUID.randomUUID()).uniteId(uniteId).identifiant("awa.caisse")
                        .nom("Awa").role("CAISSIER").statut("ACTIF").build(),
                UniteUtilisateur.builder().id(UUID.randomUUID()).uniteId(autre).identifiant("awa.caisse")
                        .nom("Awa").role("ADMIN").statut("ACTIF").build()));
        when(unites.findById(autre)).thenReturn(Optional.of(UniteOrganisation.builder()
                .id(autre).code("AG-2").libelle("Agence 2").type(TypeUnite.AGENCE).statut(StatutUnite.ACTIF).build()));

        var moi = service.moi("awa.caisse");

        assertThat(moi.uniteActiveId()).isNull();
        assertThat(moi.rattachements()).hasSize(2);
        assertThatThrownBy(() -> service.situation("awa.caisse"))
                .isInstanceOf(NetworkException.class)
                .hasMessageContaining("Choisissez");
    }

    @Test
    void uneCaisseOuverteGardeLUniteDeLaSeance() {
        UUID autre = UUID.randomUUID();
        UUID caissierId = UUID.randomUUID();
        UUID pointId = UUID.randomUUID();
        ContexteFixe fixe = new ContexteFixe();
        fixe.choisie = autre;
        service = new GuichetService(unites, utilisateurs, points, caisses, mouvements, positions, operations,
                alias, connexion, envoi, affectations, fixe);
        UniteUtilisateur ici = UniteUtilisateur.builder()
                .id(caissierId).uniteId(uniteId).identifiant("a.caisse").nom("Awa").role("CAISSIER").statut("ACTIF").build();
        when(utilisateurs.findAllByIdentifiantIgnoreCase("a.caisse")).thenReturn(List.of(ici, UniteUtilisateur.builder()
                .id(UUID.randomUUID()).uniteId(autre).identifiant("a.caisse").nom("Awa").role("CAISSIER").statut("ACTIF").build()));
        when(unites.findById(autre)).thenReturn(Optional.of(UniteOrganisation.builder()
                .id(autre).code("AG-2").libelle("Agence 2").type(TypeUnite.AGENCE).statut(StatutUnite.ACTIF).build()));
        when(caisses.findFirstByUtilisateurIdInAndStatut(any(), eq("OUVERTE")))
                .thenReturn(Optional.of(seance(pointId, caissierId, "OUVERTE", 4)));
        when(points.findById(pointId)).thenReturn(Optional.of(PointVente.builder()
                .id(pointId).uniteId(uniteId).code("PV-01").libelle("Hall").statut("ACTIF").build()));
        when(points.findByUniteIdOrderByCodeAsc(uniteId)).thenReturn(List.of());
        when(utilisateurs.findByUniteIdOrderByNomAsc(uniteId)).thenReturn(List.of(ici));

        var moi = service.moi("a.caisse");

        assertThat(moi.uniteActiveId()).isEqualTo(uniteId);
        assertThat(moi.avertissement()).contains("Ferme la caisse");
    }

    @Test
    void fermerUnRattachementLaisseLesAutresActifs() {
        UUID id = UUID.randomUUID();
        UniteUtilisateur ici = UniteUtilisateur.builder()
                .id(id).uniteId(uniteId).identifiant("awa.caisse").role("CAISSIER").statut("ACTIF").build();
        when(utilisateurs.findByIdAndUniteId(id, uniteId)).thenReturn(Optional.of(ici));
        when(utilisateurs.findAllByIdentifiantIgnoreCase("awa.caisse")).thenReturn(List.of(ici, UniteUtilisateur.builder()
                .id(UUID.randomUUID()).uniteId(UUID.randomUUID()).identifiant("awa.caisse").role("ADMIN").statut("ACTIF").build()));
        when(utilisateurs.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var dto = service.desactiver(uniteId, id);

        assertThat(dto.statut()).isEqualTo("INACTIF");
        verify(connexion).definirAcces("awa.caisse", true);
    }

    @Test
    void leDernierRattachementFermeLAcces() {
        UUID id = UUID.randomUUID();
        UniteUtilisateur ici = UniteUtilisateur.builder()
                .id(id).uniteId(uniteId).identifiant("awa.caisse").role("CAISSIER").statut("ACTIF").build();
        when(utilisateurs.findByIdAndUniteId(id, uniteId)).thenReturn(Optional.of(ici));
        when(utilisateurs.findAllByIdentifiantIgnoreCase("awa.caisse")).thenReturn(List.of(ici));
        when(utilisateurs.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var dto = service.desactiver(uniteId, id);

        assertThat(dto.statut()).isEqualTo("INACTIF");
        verify(connexion).definirAcces("awa.caisse", false);
    }

    @Test
    void uneCaisseOuverteRefuseLaDesactivation() {
        UUID id = UUID.randomUUID();
        when(utilisateurs.findByIdAndUniteId(id, uniteId)).thenReturn(Optional.of(UniteUtilisateur.builder()
                .id(id).uniteId(uniteId).identifiant("awa.caisse").role("CAISSIER").statut("ACTIF").build()));
        when(caisses.existsByUtilisateurIdAndStatut(id, "OUVERTE")).thenReturn(true);

        assertThatThrownBy(() -> service.desactiver(uniteId, id))
                .isInstanceOf(NetworkException.class)
                .hasMessageContaining("Ferme la caisse");
        verify(connexion, never()).definirAcces(any(), eq(false));
    }

    @Test
    void unCaissierEstAffectePuisRetireAvecUnMotif() {
        UUID pointId = UUID.randomUUID();
        UUID caissierId = UUID.randomUUID();
        when(points.findById(pointId)).thenReturn(Optional.of(com.gateway.payment.network.domain.PointVente.builder()
                .id(pointId).uniteId(uniteId).code("PV-01").libelle("Hall").statut("ACTIF").build()));
        when(utilisateurs.findByIdAndUniteId(caissierId, uniteId)).thenReturn(Optional.of(UniteUtilisateur.builder()
                .id(caissierId).uniteId(uniteId).identifiant("a.caisse").nom("Awa Caisse").role("CAISSIER").statut("ACTIF").build()));
        when(affectations.findByPointVenteIdAndUtilisateurIdAndStatut(pointId, caissierId, "ACTIF")).thenReturn(Optional.empty());
        when(affectations.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var cree = service.affecter(pointId,
                new AffectationRequest(caissierId, LocalDate.now(), LocalDate.now().plusDays(5)),
                admin());

        assertThat(cree.login()).isEqualTo("a.caisse");
        assertThat(cree.effective()).isTrue();
        assertThat(cree.fin()).isEqualTo(LocalDate.now().plusDays(5));

        AffectationCaissier ligne = AffectationCaissier.builder()
                .id(cree.id()).pointVenteId(pointId).utilisateurId(caissierId)
                .debut(LocalDate.now()).statut("ACTIF").build();
        when(affectations.findById(cree.id())).thenReturn(Optional.of(ligne));
        when(caisses.existsByPointVenteIdAndUtilisateurIdAndStatut(pointId, caissierId, "OUVERTE")).thenReturn(false);

        assertThatThrownBy(() -> service.desaffecter(cree.id(), new DesaffectationRequest("  "), admin()))
                .isInstanceOf(NetworkException.class)
                .hasMessageContaining("motif");

        var close = service.desaffecter(cree.id(), new DesaffectationRequest("Fin de vacation"), admin());
        assertThat(close.statut()).isEqualTo("CLOTUREE");
        assertThat(close.motif()).isEqualTo("Fin de vacation");
        assertThat(close.effective()).isFalse();
    }

    @Test
    void lecheanceRetireLAffectation() {
        UUID pointId = UUID.randomUUID();
        UUID caissierId = UUID.randomUUID();
        when(points.findById(pointId)).thenReturn(Optional.of(com.gateway.payment.network.domain.PointVente.builder()
                .id(pointId).uniteId(uniteId).code("PV-01").statut("ACTIF").build()));
        when(utilisateurs.findAllByIdentifiantIgnoreCase("a.caisse")).thenReturn(List.of(UniteUtilisateur.builder()
                .id(caissierId).uniteId(uniteId).identifiant("a.caisse").role("CAISSIER").statut("ACTIF").build()));
        when(affectations.findByPointVenteIdAndUtilisateurIdAndStatut(pointId, caissierId, "ACTIF"))
                .thenReturn(Optional.of(AffectationCaissier.builder()
                        .id(UUID.randomUUID()).pointVenteId(pointId).utilisateurId(caissierId)
                        .debut(LocalDate.now().minusDays(3)).fin(LocalDate.now().minusDays(1)).statut("ACTIF").build()));

        assertThatThrownBy(() -> service.ouvrir(pointId, BigDecimal.ZERO, "a.caisse", "Bearer t"))
                .isInstanceOf(NetworkException.class)
                .hasMessageContaining("affecté");
    }

    @Test
    void seulUnCaissierOuvreLaCaisse() {
        UUID pointId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        when(points.findById(pointId)).thenReturn(Optional.of(com.gateway.payment.network.domain.PointVente.builder()
                .id(pointId).uniteId(uniteId).code("PV-01").statut("ACTIF").build()));
        when(utilisateurs.findAllByIdentifiantIgnoreCase("a.admin")).thenReturn(List.of(UniteUtilisateur.builder()
                .id(adminId).uniteId(uniteId).identifiant("a.admin").role("ADMIN").statut("ACTIF").build()));

        assertThatThrownBy(() -> service.ouvrir(pointId, BigDecimal.ZERO, "a.admin", "Bearer t"))
                .isInstanceOf(NetworkException.class)
                .hasMessageContaining("caissier");
    }

    @Test
    void approvisionnementExigeLetiquetteDeLaCaisse() {
        UUID pointId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        UUID source = UUID.randomUUID();
        UUID cible = UUID.randomUUID();
        when(points.findById(pointId)).thenReturn(Optional.of(com.gateway.payment.network.domain.PointVente.builder()
                .id(pointId).uniteId(uniteId).statut("ACTIF").build()));
        when(utilisateurs.findByIdAndUniteId(adminId, uniteId)).thenReturn(Optional.of(UniteUtilisateur.builder()
                .id(adminId).uniteId(uniteId).role("ADMIN").statut("ACTIF").build()));
        when(alias.lire(source, "Bearer t")).thenReturn(new AliasVu(source, "TRESORERIE", "UNITE", uniteId, "XOF", true));
        when(alias.lire(cible, "Bearer t")).thenReturn(new AliasVu(cible, "CPTECOMMISSION", "POINT_VENTE", pointId, "XOF", true));

        MouvementRequest req = new MouvementRequest(
                "APPROVISIONNEMENT", source, cible, new BigDecimal("1000"), "XOF", adminId);
        assertThatThrownBy(() -> service.transferer(pointId, req, "Bearer t", "test"))
                .isInstanceOf(NetworkException.class)
                .hasMessageContaining("CPTEOPERATION");
    }

    @Test
    void remonteeRepartDeLaCaisseVersLunite() {
        UUID pointId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        UUID caisseAlias = UUID.randomUUID();
        UUID uniteAlias = UUID.randomUUID();
        when(points.findById(pointId)).thenReturn(Optional.of(com.gateway.payment.network.domain.PointVente.builder()
                .id(pointId).uniteId(uniteId).statut("ACTIF").build()));
        when(utilisateurs.findByIdAndUniteId(adminId, uniteId)).thenReturn(Optional.of(UniteUtilisateur.builder()
                .id(adminId).uniteId(uniteId).role("ADMIN").statut("ACTIF").build()));
        when(alias.lire(caisseAlias, "Bearer t"))
                .thenReturn(new AliasVu(caisseAlias, "CPTEOPERATION", "POINT_VENTE", pointId, "XOF", true));
        when(alias.lire(uniteAlias, "Bearer t"))
                .thenReturn(new AliasVu(uniteAlias, "TRESORERIE", "UNITE", uniteId, "XOF", true));
        when(mouvements.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(positions.findById(pointId)).thenReturn(Optional.of(PositionGuichet.builder()
                .pointVenteId(pointId).soldeCompte(new BigDecimal("1000.00")).fondsAttendu(BigDecimal.ZERO).devise("XOF").build()));
        when(positions.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var dto = service.transferer(pointId, new MouvementRequest(
                "REMONTEE", caisseAlias, uniteAlias, new BigDecimal("500"), "XOF", adminId), "Bearer t", "test");

        assertThat(dto.sens()).isEqualTo("REMONTEE");
        assertThat(dto.etiquetteSource()).isEqualTo("CPTEOPERATION");
        assertThat(dto.etiquetteCible()).isEqualTo("TRESORERIE");
    }

    @Test
    void unEcartNempechePasLouverture() {
        UUID pointId = UUID.randomUUID();
        UUID caissierId = UUID.randomUUID();
        preparerCaissier(pointId, caissierId, new BigDecimal("1000.00"), new BigDecimal("2500.00"));
        when(caisses.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var ecart = service.ouvrir(pointId, new BigDecimal("800.00"), "a.caisse", "Bearer t");

        assertThat(ecart.statut()).isEqualTo("OUVERTE");
        assertThat(ecart.ecartOuverture()).isEqualByComparingTo("-200.00");
        assertThat(ecart.soldeCompteOuverture()).isEqualByComparingTo("2500.00");
        assertThat(ecart.numero()).isEqualTo(1);
    }

    @Test
    void unDepotAugmenteLesEspecesEtDiminueLeCompte() {
        UUID pointId = UUID.randomUUID();
        UUID caissierId = UUID.randomUUID();
        preparerCaissier(pointId, caissierId, BigDecimal.ZERO, new BigDecimal("5000.00"));
        when(caisses.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(operations.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var ouverte = service.ouvrir(pointId, BigDecimal.ZERO, "a.caisse", "Bearer t");
        ArgumentCaptor<Caisse> capteur = ArgumentCaptor.forClass(Caisse.class);
        verify(caisses).save(capteur.capture());
        when(caisses.findById(ouverte.id())).thenReturn(Optional.of(capteur.getValue()));

        var apres = service.enregistrerOperation(ouverte.id(), "DEPOT", new BigDecimal("1500"), null, "a.caisse");

        assertThat(apres.depots()).isEqualByComparingTo("1500.00");
        assertThat(apres.physique()).isEqualByComparingTo("1500.00");
        assertThat(apres.soldeCompte()).isEqualByComparingTo("3500.00");

        assertThatThrownBy(() -> service.enregistrerOperation(ouverte.id(), "RETRAIT", new BigDecimal("2000"), null, "a.caisse"))
                .isInstanceOf(NetworkException.class)
                .hasMessageContaining("espèces");
    }

    private void preparerCaissier(UUID pointId, UUID caissierId, BigDecimal fonds, BigDecimal solde) {
        when(points.findById(pointId)).thenReturn(Optional.of(com.gateway.payment.network.domain.PointVente.builder()
                .id(pointId).uniteId(uniteId).code("PV-01").libelle("Hall").statut("ACTIF").build()));
        when(utilisateurs.findAllByIdentifiantIgnoreCase("a.caisse")).thenReturn(List.of(UniteUtilisateur.builder()
                .id(caissierId).uniteId(uniteId).identifiant("a.caisse").nom("Awa Caisse").role("CAISSIER").statut("ACTIF").build()));
        when(affectations.findByPointVenteIdAndUtilisateurIdAndStatut(pointId, caissierId, "ACTIF"))
                .thenReturn(Optional.of(AffectationCaissier.builder()
                        .id(UUID.randomUUID()).pointVenteId(pointId).utilisateurId(caissierId)
                        .debut(LocalDate.now()).statut("ACTIF").build()));
        when(alias.compteOperation(eq(pointId), eq("Bearer t"))).thenReturn(
                new AliasVu(UUID.randomUUID(), "CPTEOPERATION", "POINT_VENTE", pointId, "XOF", true));
        when(positions.findById(pointId)).thenReturn(Optional.of(PositionGuichet.builder()
                .pointVenteId(pointId).soldeCompte(solde).fondsAttendu(fonds).devise("XOF").build()));
        lenient().when(positions.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void reactiverUnSecondAdminEstRefuse() {
        UUID adminId = UUID.randomUUID();
        when(utilisateurs.findByIdAndUniteId(adminId, uniteId)).thenReturn(Optional.of(UniteUtilisateur.builder()
                .id(adminId).uniteId(uniteId).identifiant("70000099").role("ADMIN").statut("INACTIF").build()));
        when(utilisateurs.existsByUniteIdAndRoleAndStatut(uniteId, "ADMIN", "ACTIF")).thenReturn(true);

        assertThatThrownBy(() -> service.activer(uniteId, adminId))
                .isInstanceOf(NetworkException.class)
                .hasMessageContaining("administrateur");
    }

    @Test
    void unCaissierVoitSesSeancesEtPasCellesDUnAutre() {
        UUID caissierId = UUID.randomUUID();
        UUID pointId = UUID.randomUUID();
        UniteUtilisateur caissier = UniteUtilisateur.builder()
                .id(caissierId).uniteId(uniteId).identifiant("a.caisse").nom("Awa Caisse")
                .role("CAISSIER").statut("ACTIF").build();
        when(utilisateurs.findAllByIdentifiantIgnoreCase("a.caisse")).thenReturn(List.of(caissier));
        when(points.findByUniteIdOrderByCodeAsc(uniteId)).thenReturn(List.of(PointVente.builder()
                .id(pointId).uniteId(uniteId).code("PV-01").libelle("Hall").statut("ACTIF").build()));
        when(utilisateurs.findByUniteIdOrderByNomAsc(uniteId)).thenReturn(List.of(caissier));
        when(caisses.findByUtilisateurIdAndStatutOrderByOuverteLeDesc(caissierId, "OUVERTE")).thenReturn(List.of(
                seance(pointId, caissierId, "OUVERTE", 2)));

        var ouvertes = service.seances("a.caisse", null, "ouvertes");

        assertThat(ouvertes).hasSize(1);
        assertThat(ouvertes.getFirst().numero()).isEqualTo(2);
        assertThat(ouvertes.getFirst().pointCode()).isEqualTo("PV-01");
        assertThat(ouvertes.getFirst().login()).isEqualTo("a.caisse");
        assertThatThrownBy(() -> service.seances("a.caisse", "autre", "archives"))
                .isInstanceOf(NetworkException.class)
                .hasMessageContaining("propres");
    }

    @Test
    void unAdminConsulteUnCaissierParSonLogin() {
        UUID adminId = UUID.randomUUID();
        UUID caissierId = UUID.randomUUID();
        UUID pointId = UUID.randomUUID();
        UniteUtilisateur adminUnite = UniteUtilisateur.builder()
                .id(adminId).uniteId(uniteId).identifiant("a.admin").nom("Admin").role("ADMIN").statut("ACTIF").build();
        UniteUtilisateur caissier = UniteUtilisateur.builder()
                .id(caissierId).uniteId(uniteId).identifiant("a.caisse").nom("Awa Caisse")
                .role("CAISSIER").statut("ACTIF").build();
        when(utilisateurs.findAllByIdentifiantIgnoreCase("a.admin")).thenReturn(List.of(adminUnite));
        when(utilisateurs.findAllByIdentifiantIgnoreCase("a.caisse")).thenReturn(List.of(caissier));
        when(points.findByUniteIdOrderByCodeAsc(uniteId)).thenReturn(List.of(PointVente.builder()
                .id(pointId).uniteId(uniteId).code("PV-01").libelle("Hall").statut("ACTIF").build()));
        when(utilisateurs.findByUniteIdOrderByNomAsc(uniteId)).thenReturn(List.of(adminUnite, caissier));
        when(caisses.findByPointVenteIdInAndStatutIn(List.of(pointId), List.of("OUVERTE")))
                .thenReturn(List.of(seance(pointId, caissierId, "OUVERTE", 3)));
        when(caisses.findTop40ByUtilisateurIdAndStatutNotOrderByOuverteLeDesc(caissierId, "OUVERTE"))
                .thenReturn(List.of(seance(pointId, caissierId, "FERMEE", 1)));

        assertThatThrownBy(() -> service.seances("a.admin", " ", "archives"))
                .isInstanceOf(NetworkException.class)
                .hasMessageContaining("login");

        var ouvertes = service.seances("a.admin", null, "ouvertes");
        var archives = service.seances("a.admin", "a.caisse", "archives");

        assertThat(ouvertes).extracting(s -> s.numero()).containsExactly(3);
        assertThat(archives).extracting(s -> s.statut()).containsExactly("FERMEE");
        assertThat(archives.getFirst().login()).isEqualTo("a.caisse");
    }

    private static Caisse seance(UUID pointId, UUID caissierId, String statut, int numero) {
        return Caisse.builder()
                .id(UUID.randomUUID())
                .pointVenteId(pointId)
                .utilisateurId(caissierId)
                .numero(numero)
                .statut(statut)
                .fondsAttendu(BigDecimal.ZERO)
                .fondsDeclare(new BigDecimal("50.00"))
                .ecartOuverture(new BigDecimal("50.00"))
                .soldeCompteOuverture(new BigDecimal("600.00"))
                .depots(BigDecimal.ZERO)
                .retraits(BigDecimal.ZERO)
                .ouverteLe(Instant.parse("2026-11-17T10:00:00Z"))
                .ouvertePar("a.caisse")
                .fermeeLe("FERMEE".equals(statut) ? Instant.parse("2026-11-17T18:00:00Z") : null)
                .fondsPhysiqueCloture("FERMEE".equals(statut) ? BigDecimal.ZERO : null)
                .ecartFermeture("FERMEE".equals(statut) ? new BigDecimal("-50.00") : null)
                .build();
    }

    private static final class ContexteFixe extends ContexteUnite {
        private UUID choisie;

        @Override
        public UUID demandee() {
            return choisie;
        }
    }

    private static UsernamePasswordAuthenticationToken admin() {
        return new UsernamePasswordAuthenticationToken(
                "admin", null, java.util.List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }
}
