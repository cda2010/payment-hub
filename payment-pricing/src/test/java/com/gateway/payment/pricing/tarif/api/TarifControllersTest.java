package com.gateway.payment.pricing.tarif.api;

import com.gateway.payment.pricing.security.JwtAuthenticationFilter;
import com.gateway.payment.pricing.security.SecurityConfig;
import com.gateway.payment.pricing.tarif.TarifException;
import com.gateway.payment.pricing.tarif.domain.CibleReseau;
import com.gateway.payment.pricing.tarif.moteur.DemandeFrais;
import com.gateway.payment.pricing.tarif.moteur.GrilleApplicable;
import com.gateway.payment.pricing.tarif.moteur.MoteurFrais;
import com.gateway.payment.pricing.tarif.moteur.ResultatFrais;
import com.gateway.payment.pricing.tarif.service.FormuleService;
import com.gateway.payment.pricing.tarif.service.GrilleService;
import com.gateway.payment.pricing.tarif.validation.ValidationIssue;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({GrilleController.class, FormuleController.class, FraisController.class})
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
@TestPropertySource(properties = "pricing.security.keycloak.enabled=false")
class TarifControllersTest {

    private static final String GRILLE = """
            {"code":"trf_new","libelle":"Nouvelle","produit":"TRANSFERT","canal":"WEB","devise":"XOF",
             "dateDebut":"2026-11-01"}""";

    @Autowired
    private MockMvc mvc;

    @MockBean
    private GrilleService grilles;
    @MockBean
    private FormuleService formules;
    @MockBean
    private MoteurFrais moteur;

    @Test
    void calculSansAuthentificationRefuse() throws Exception {
        mvc.perform(post("/api/v1/frais/calculer").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "CONFIG_MANAGER")
    void grilleApplicableSansMontant() throws Exception {
        UUID unite = UUID.randomUUID();
        when(moteur.applicable(any())).thenReturn(new GrilleApplicable(GrilleApplicable.Statut.TROUVEE, null,
                new GrilleApplicable.Retenue(UUID.randomUUID(), "OHG", 2, "Ouahigouya", null, LocalDate.of(2026, 1, 1),
                        null, new ResultatFrais.Correspondance(CibleReseau.Niveau.UNITE, "AG-OUAHIGOUYA", null),
                        ResultatFrais.Correspondance.TOUTES),
                List.of(), 3, 0));

        mvc.perform(post("/api/v1/frais/grille-applicable").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"produitCode\":\"transfert\",\"devise\":\"XOF\",\"source\":{\"uniteId\":\"" + unite + "\"}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("TROUVEE"))
                .andExpect(jsonPath("$.grille.code").value("OHG"))
                .andExpect(jsonPath("$.grille.source.cible").value("AG-OUAHIGOUYA"))
                .andExpect(jsonPath("$.voisinesSource").value(3));
        ArgumentCaptor<DemandeFrais> demande = ArgumentCaptor.forClass(DemandeFrais.class);
        verify(moteur).applicable(demande.capture());
        assertThat(demande.getValue().montant()).isNull();
        assertThat(demande.getValue().source().uniteId()).isEqualTo(unite);

        mvc.perform(post("/api/v1/frais/grille-applicable").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void calculRenvoieLeFormatDuCahier() throws Exception {
        when(moteur.calculer(any())).thenReturn(new ResultatFrais(new BigDecimal("20000"), "XOF", UUID.randomUUID(),
                "TRF_STD", 1, new ResultatFrais.TrancheRetenue(2, new BigDecimal("8001"), new BigDecimal("25000"), null, null),
                "EXCESS_AX_PLUS_Y", 1, Map.of("taux", new BigDecimal("0.015")), new BigDecimal("12000"),
                new BigDecimal("280.000"), null, new BigDecimal("280.000"), new ResultatFrais.Arrondi(0, RoundingMode.HALF_UP),
                new BigDecimal("280"), "(20000 - 8000) * 0.015 + 100", LocalDate.of(2026, 10, 11)));

        mvc.perform(post("/api/v1/frais/calculer").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"produitCode\":\"TRANSFERT\",\"canalCode\":\"MOBILE_MONEY\",\"devise\":\"XOF\",\"montant\":20000}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.grilleCode").value("TRF_STD"))
                .andExpect(jsonPath("$.versionGrille").value(1))
                .andExpect(jsonPath("$.tranche.montantMin").value(8001))
                .andExpect(jsonPath("$.tranche.montantMax").value(25000))
                .andExpect(jsonPath("$.formuleCode").value("EXCESS_AX_PLUS_Y"))
                .andExpect(jsonPath("$.baseTaxable").value(12000))
                .andExpect(jsonPath("$.frais").value(280))
                .andExpect(jsonPath("$.details").value("(20000 - 8000) * 0.015 + 100"))
                .andExpect(jsonPath("$.ajustement").doesNotExist());

        ArgumentCaptor<DemandeFrais> demande = ArgumentCaptor.forClass(DemandeFrais.class);
        verify(moteur).calculer(demande.capture());
        assertThat(demande.getValue().montant()).isEqualByComparingTo("20000");
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void erreurFonctionnelleDuMoteurEn422() throws Exception {
        when(moteur.calculer(any())).thenThrow(TarifException.calcul("AUCUNE_TRANCHE", "Aucune tranche de TRF_STD v1 ne couvre ce montant."));

        mvc.perform(post("/api/v1/frais/calculer").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"produitCode\":\"TRANSFERT\",\"devise\":\"XOF\",\"montant\":600000}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("AUCUNE_TRANCHE"))
                .andExpect(jsonPath("$.details").doesNotExist());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void requeteIncompleteEn400AvecChamps() throws Exception {
        mvc.perform(post("/api/v1/frais/calculer").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"produitCode\":\"TRANSFERT\",\"montant\":-5}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("REQUETE_INVALIDE"))
                .andExpect(jsonPath("$.details[*].champ").value(org.hamcrest.Matchers.containsInAnyOrder("devise", "montant")));
        verifyNoInteractions(moteur);
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void creationDeGrilleReserveeAuxAdministrateurs() throws Exception {
        mvc.perform(post("/api/v1/grilles").contentType(MediaType.APPLICATION_JSON).content(GRILLE))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/grilles").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/grilles/{id}/tranches/{t}/dupliquer", UUID.randomUUID(), UUID.randomUUID()))
                .andExpect(status().isForbidden());
        verifyNoInteractions(grilles);
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void simulationEtValidationOuvertesAuxUtilisateursConnectes() throws Exception {
        mvc.perform(post("/api/v1/grilles/{id}/valider", UUID.randomUUID())).andExpect(status().isOk());
        mvc.perform(post("/api/v1/grilles/{id}/simulations", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"montant\":1000}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "maker", roles = "CONFIG_MANAGER")
    void creationDeGrilleParUnConfigManager() throws Exception {
        mvc.perform(post("/api/v1/grilles").contentType(MediaType.APPLICATION_JSON).content(GRILLE))
                .andExpect(status().isCreated());
        verify(grilles).creer(any(), eq("maker"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void configurationInvalideEn400AvecTrancheEtChamp() throws Exception {
        UUID id = UUID.randomUUID();
        UUID trancheId = UUID.randomUUID();
        when(grilles.modifierTranche(eq(id), eq(trancheId), any(), any())).thenThrow(TarifException.configurationInvalide(
                "Configuration de tranche invalide.",
                List.of(ValidationIssue.tranche(2, "PARAM_OBLIGATOIRE_MANQUANT", "parametres.taux",
                        "Paramètre obligatoire manquant : Taux (taux)."))));

        mvc.perform(put("/api/v1/grilles/{id}/tranches/{t}", id, trancheId).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"montantMin\":8001,\"montantMax\":25000,\"formule\":{\"code\":\"EXCESS_AX_PLUS_Y\",\"version\":1},"
                                + "\"parametres\":{\"seuil\":8000}}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CONFIGURATION_INVALIDE"))
                .andExpect(jsonPath("$.details[0].tranche").value(2))
                .andExpect(jsonPath("$.details[0].champ").value("parametres.taux"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void grillePublieeNonModifiableEn409() throws Exception {
        UUID id = UUID.randomUUID();
        when(grilles.modifier(eq(id), any(), any())).thenThrow(new TarifException(HttpStatus.CONFLICT,
                "GRILLE_NON_MODIFIABLE", "La grille TRF_STD v1 est au statut PUBLIEE."));

        mvc.perform(put("/api/v1/grilles/{id}", id).contentType(MediaType.APPLICATION_JSON).content(GRILLE))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("GRILLE_NON_MODIFIABLE"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void tauxTransmisEnDecimalSansPerteDePrecision() throws Exception {
        UUID id = UUID.randomUUID();

        mvc.perform(post("/api/v1/grilles/{id}/tranches", id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"montantMin\":0,\"formule\":{\"code\":\"PERCENT_TOTAL\",\"version\":1},"
                                + "\"parametres\":{\"taux\":0.015}}"))
                .andExpect(status().isCreated());

        ArgumentCaptor<com.gateway.payment.pricing.tarif.api.dto.TarifDtos.TrancheRequest> req =
                ArgumentCaptor.forClass(com.gateway.payment.pricing.tarif.api.dto.TarifDtos.TrancheRequest.class);
        verify(grilles).ajouterTranche(eq(id), req.capture(), any());
        assertThat(req.getValue().parametres().get("taux")).isEqualTo(new BigDecimal("0.015"));
    }
}
