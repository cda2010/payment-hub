package com.gateway.payment.identity.enrolement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.payment.identity.domain.IdentityParty;
import com.gateway.payment.identity.repository.IdentityPartyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnrolementServiceTest {

    @Mock EnrolementCompteRepository comptes;
    @Mock EnrolementRepository dossiers;
    @Mock EnrolementPassageRepository passages;
    @Mock IdentityPartyRepository parties;
    @Mock CoffreDocuments coffre;
    @Mock CatalogueEnrolement catalogue;
    @Mock PiecesEnrolement pieces;
    @Mock LimiteEnrolement limites;

    EnrolementService service;
    EnrolementCompte awa;
    EnrolementCompte banque;

    @BeforeEach
    void setUp() {
        EnrollmentProperties properties = new EnrollmentProperties();
        properties.setExposeCode(true);
        service = new EnrolementService(comptes, dossiers, passages, parties, coffre, catalogue, pieces, properties,
                new AnnuaireFse(properties), new InfosOmnicanal(properties, new ObjectMapper()), limites);
        lenient().when(passages.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(coffre.deposer(any(), any(), any(), any(), any(), any())).thenReturn(UUID.randomUUID());
        lenient().when(catalogue.pour(any())).thenReturn(parcours("BF", true));
        lenient().when(pieces.demander(any(), any())).thenReturn(parcours("BF", true).documents());
        awa = compte("MONEY", "70123456", "70123456");
        banque = compte("BANK", "0003000300030003", "70000003");
        Map<UUID, Enrolement> stock = new HashMap<>();
        lenient().when(dossiers.save(any())).thenAnswer(invocation -> {
            Enrolement dossier = invocation.getArgument(0);
            if (dossier.getId() == null) {
                dossier.setId(UUID.randomUUID());
            }
            stock.put(dossier.getId(), dossier);
            return dossier;
        });
        lenient().when(dossiers.findById(any())).thenAnswer(invocation ->
                Optional.ofNullable(stock.get(invocation.getArgument(0))));
    }

    @Test
    void unknownWalletDoesNotStartACode() {
        when(comptes.findByTenantIdAndNumero("CORIS_DEMO", "70999999")).thenReturn(List.of());

        assertThatThrownBy(() -> service.chercherPortefeuille("70 99 99 99"))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void walletCodeThenEntersTheExistingAccount() {
        when(comptes.findByTenantIdAndNumero("CORIS_DEMO", "70123456")).thenReturn(List.of(awa));
        IdentityParty party = new IdentityParty();
        party.setDisplayName("Awa Ouédraogo");
        when(parties.findById(awa.getPartyId())).thenReturn(Optional.of(party));

        EnrolementService.Vue code = service.chercherPortefeuille("70 12 34 56");

        assertThat(code.etape()).isEqualTo("CODE");
        assertThat(code.code()).hasSize(6);
        assertThat(code.nom()).isNull();

        EnrolementService.Vue entre = service.confirmer(code.id(), code.code());

        assertThat(entre.etape()).isEqualTo("ENTRE");
        assertThat(entre.workflow()).isEqualTo("ENROLEMENT");
        assertThat(entre.nom()).isEqualTo("Awa Ouédraogo");
        assertThat(entre.numeroCompte()).isNull();
    }

    @Test
    void bankCodeDoesNotRevealThePhone() {
        when(comptes.findByTenantIdAndNumero("CORIS_DEMO", "0003000300030003")).thenReturn(List.of(banque));
        when(parties.findById(banque.getPartyId())).thenReturn(Optional.empty());

        EnrolementService.Vue code = service.chercherCompte("0003000300030003", null);
        EnrolementService.Vue suite = service.confirmer(code.id(), code.code());

        assertThat(suite.etape()).isEqualTo("DOSSIER");
        assertThat(suite.toString()).doesNotContain("70000003");
    }

    @Test
    void newPhoneCanOpenAWalletAfterBothSides() {
        when(comptes.findByTenantIdAndNumero("CORIS_DEMO", "76001122")).thenReturn(List.of());
        when(parties.save(any())).thenAnswer(invocation -> {
            IdentityParty party = invocation.getArgument(0);
            party.setId(UUID.randomUUID());
            return party;
        });
        when(comptes.existsByTenantIdAndMaisonAndNumero(any(), any(), any())).thenReturn(false);
        when(comptes.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        EnrolementService.Vue code = service.commencer("76 00 11 22");
        service.confirmer(code.id(), code.code());
        service.dossier(code.id(), "BF", "Traoré", "Aminata", null, null);
        service.proposer(code.id());
        service.suite(code.id(), "IDENTIFICATION");
        service.adresse(code.id(), Map.of(
                "countryCode", "BF",
                "regionCode", "CEN",
                "provinceCode", "KAD",
                "communeCode", "OUA",
                "city", "Ouagadougou",
                "arrondissement", "5",
                "sector", "23",
                "district", "Patte d'Oie",
                "street", "Rue 15.32"));
        String document = Base64.getEncoder().encodeToString(new byte[] {1, 2, 3});
        service.piece(code.id(), "RECTO", "image/jpeg", document, "CNI");
        service.piece(code.id(), "VERSO", "image/jpeg", document, "CNI");

        EnrolementService.Vue ouvert = service.ouvrir(code.id());

        assertThat(ouvert.etape()).isEqualTo("OUVERT");
        assertThat(ouvert.numeroCompte()).startsWith("226");
    }

    @Test
    void ivoryCoastFormRequiresBirthPlace() {
        when(catalogue.pour("CI")).thenReturn(parcours("CI", true));
        when(comptes.findByTenantIdAndNumero("CORIS_DEMO", "76001123")).thenReturn(List.of());

        EnrolementService.Vue code = service.commencer("76 00 11 23");
        service.confirmer(code.id(), code.code());

        assertThatThrownBy(() -> service.dossier(code.id(), "CI", "Traoré", "Aminata", null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Lieu de naissance");
    }

    private static ParcoursPays parcours(String code, boolean verso) {
        List<ParcoursPays.Champ> champs = new java.util.ArrayList<>(List.of(
                new ParcoursPays.Champ("nom", "Nom", "TEXT", true),
                new ParcoursPays.Champ("prenom", "Prénom", "TEXT", true),
                new ParcoursPays.Champ("dateNaissance", "Date de naissance", "DATE", false)
        ));
        if ("CI".equals(code)) {
            champs.add(new ParcoursPays.Champ("lieuNaissance", "Lieu de naissance", "TEXT", true));
        }
        List<ParcoursPays.Document> documents = new java.util.ArrayList<>();
        documents.add(new ParcoursPays.Document("RECTO", "Recto", true));
        if (verso) {
            documents.add(new ParcoursPays.Document("VERSO", "Verso", true));
        }
        return new ParcoursPays(code, code, "XOF", List.of("MONEY"), champs, documents);
    }

    private static EnrolementCompte compte(String maison, String numero, String telephone) {
        EnrolementCompte compte = new EnrolementCompte();
        compte.setId(UUID.randomUUID());
        compte.setTenantId("CORIS_DEMO");
        compte.setMaison(maison);
        compte.setNumero(numero);
        compte.setPartyId(UUID.randomUUID());
        compte.setTelephone(telephone);
        return compte;
    }
}
