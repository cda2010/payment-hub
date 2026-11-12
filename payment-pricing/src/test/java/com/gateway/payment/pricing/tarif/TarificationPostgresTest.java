package com.gateway.payment.pricing.tarif;

import com.gateway.payment.pricing.api.dto.PricingDtos.QuoteRequest;
import com.gateway.payment.pricing.api.dto.PricingDtos.QuoteResponse;
import com.gateway.payment.pricing.service.PricingEngineService;
import com.gateway.payment.pricing.tarif.domain.GrilleTarifaire;
import com.gateway.payment.pricing.tarif.domain.StatutGrille;
import com.gateway.payment.pricing.tarif.moteur.DemandeFrais;
import com.gateway.payment.pricing.tarif.moteur.GrilleApplicable;
import com.gateway.payment.pricing.tarif.moteur.Localisation;
import com.gateway.payment.pricing.tarif.moteur.ResultatFrais;
import com.gateway.payment.pricing.tarif.moteur.MoteurFrais;
import com.gateway.payment.pricing.tarif.repository.GrilleTarifaireRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Migrations Flyway, JSONB et sélection des grilles sur un vrai PostgreSQL (ignoré sans Docker). */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class TarificationPostgresTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void proprietes(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("pricing.security.keycloak.enabled", () -> "false");
    }

    private static final LocalDate DATE = LocalDate.of(2026, 10, 11);

    @Autowired
    private MoteurFrais moteur;
    @Autowired
    private PricingEngineService pricing;
    @Autowired
    private GrilleTarifaireRepository grilles;
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private TransactionTemplate tx;
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ReseauDeTest reseau;

    @TestConfiguration
    static class Reseau {
        @Bean
        @Primary
        ReseauDeTest reseauDeTest() {
            return new ReseauDeTest();
        }
    }

    @ParameterizedTest(name = "TRF_STD : {0} → {1}")
    @CsvSource({"5000,0", "8000,0", "8001,100", "20000,280", "25000,355", "50000,450", "200000,1000", "500000,2000"})
    void grilleDeDemonstrationDuCahier(String montant, String frais) {
        assertThat(moteur.calculer(demande("TRANSFERT", "MOBILE_MONEY", montant)).frais()).isEqualByComparingTo(frais);
    }

    @Test
    void ta010_horsTranche() {
        assertThatThrownBy(() -> moteur.calculer(demande("TRANSFERT", "MOBILE_MONEY", "600000")))
                .hasFieldOrPropertyWithValue("code", "AUCUNE_TRANCHE");
    }

    @Test
    void grillesConvertiesDepuisLAncienModele() {
        assertThat(moteur.calculer(demande("ACCOUNT_TRANSFER", "WEB", "100000")).frais()).isEqualByComparingTo("700");
        assertThat(moteur.calculer(demande("ACCOUNT_TRANSFER", "WEB", "10000")).frais()).isEqualByComparingTo("100");
        assertThat(moteur.calculer(demande("ACCOUNT_TRANSFER", "WEB", "1000000")).frais()).isEqualByComparingTo("3500");
        assertThat(moteur.calculer(demande("SWIFT", "WEB", "1000000")).frais()).isEqualByComparingTo("26500");
        assertThat(moteur.calculer(demande("BILL_PAYMENT", "MOBILE", "1000")).grilleCode()).isEqualTo("FEE-BILL");
        assertThat(moteur.calculer(demande("CASH_IN", null, "1000")).frais()).isEqualByComparingTo("100");
    }

    @Test
    void jsonbRelitLesTauxEnBigDecimal() {
        Object taux = tx.execute(s -> grilles.findAll().stream()
                .filter(g -> g.getCode().equals("TRF_STD"))
                .findFirst().orElseThrow()
                .getTranches().get(1).getParametres().get("taux"));

        assertThat(taux).isEqualTo(new BigDecimal("0.015"));
    }

    @Test
    void ta011_grilleExpireeOuDesactiveeNonSelectionnee() {
        assertThatThrownBy(() -> moteur.calculer(new DemandeFrais("TRANSFERT", "MOBILE_MONEY", "XOF",
                new BigDecimal("20000"), LocalDate.of(2025, 12, 31))))
                .as("avant la date de début")
                .hasFieldOrPropertyWithValue("code", "AUCUNE_GRILLE_APPLICABLE");

        try {
            jdbc.update("UPDATE grille_tarifaire SET date_fin = DATE '2026-06-30' WHERE code = 'FEE-RTGS'");
            assertThatThrownBy(() -> moteur.calculer(demande("RTGS", "WEB", "1000000")))
                    .as("grille expirée").hasFieldOrPropertyWithValue("code", "AUCUNE_GRILLE_APPLICABLE");
            assertThat(moteur.calculer(new DemandeFrais("RTGS", "WEB", "XOF", new BigDecimal("1000000"),
                    LocalDate.of(2026, 6, 30))).frais()).as("dernier jour de validité").isEqualByComparingTo("5000");

            jdbc.update("UPDATE grille_tarifaire SET date_fin = NULL, statut = 'DESACTIVEE' WHERE code = 'FEE-RTGS'");
            assertThatThrownBy(() -> moteur.calculer(demande("RTGS", "WEB", "1000000")))
                    .as("grille désactivée").hasFieldOrPropertyWithValue("code", "AUCUNE_GRILLE_APPLICABLE");
        } finally {
            jdbc.update("UPDATE grille_tarifaire SET date_fin = NULL, statut = 'PUBLIEE' WHERE code = 'FEE-RTGS'");
        }
    }

    @Test
    void quoteDelegueLesFraisAuMoteur() {
        QuoteResponse q = pricing.quote(new QuoteRequest("ACCOUNT_TRANSFER", "WEB", "RETAIL", new BigDecimal("100000"),
                "XOF", null, null, null, null, null, null));

        assertThat(q.allowed()).isTrue();
        assertThat(q.feeAmount()).isEqualByComparingTo("700");
        assertThat(q.totalDebit()).isEqualByComparingTo("100700");
        assertThat(q.feeGridCode()).isEqualTo("FEE-DOMESTIC");
        assertThat(q.formulaCode()).isEqualTo("AX_PLUS_Y");
        assertThat(q.feeBearer()).isEqualTo("SENDER");
        assertThat(q.feeBreakdown()).isEqualTo("FEE-DOMESTIC v1, tranche 2 : 100000 * 0.005 + 200 = 700");
    }

    @Test
    void quoteSansGrilleEstSansFrais() {
        QuoteResponse q = pricing.quote(new QuoteRequest("INCONNU", "WEB", null, new BigDecimal("1000"),
                "XOF", null, null, null, null, null, null));

        assertThat(q.allowed()).isTrue();
        assertThat(q.feeAmount()).isEqualByComparingTo("0");
        assertThat(q.feeBreakdown()).isEqualTo("Sans frais");
    }

    @Test
    @WithMockUser(username = "maker", roles = "CONFIG_MANAGER")
    void cycleBrouillonValidationSimulationParLAPI() throws Exception {
        MvcResult cree = mvc.perform(post("/api/v1/grilles").contentType(MediaType.APPLICATION_JSON).content("""
                        {"code":"trf_pro","libelle":"Transfert pro","produit":"transfert","canal":"web","devise":"xof",
                         "dateDebut":"2026-11-01",
                         "tranches":[
                           {"montantMin":0,"montantMax":10000,"formule":{"code":"FIXED","version":1},"parametres":{"montantFixe":50}},
                           {"montantMin":10002,"formule":{"code":"PERCENT_TOTAL","version":1},"parametres":{"taux":0.0125},
                            "fraisMax":5000}
                         ]}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("TRF_PRO"))
                .andExpect(jsonPath("$.statut").value("BROUILLON"))
                .andExpect(jsonPath("$.createdBy").value("maker"))
                .andExpect(jsonPath("$.tranches[1].parametres.taux").value(0.0125))
                .andReturn();
        String id = com.jayway.jsonpath.JsonPath.read(cree.getResponse().getContentAsString(), "$.id");

        mvc.perform(post("/api/v1/grilles/{id}/valider", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valide").value(false))
                .andExpect(jsonPath("$.erreurs[0].code").value("TRANCHES_DISCONTINUES"))
                .andExpect(jsonPath("$.erreurs[0].tranche").value(2));

        mvc.perform(post("/api/v1/grilles/{id}/simulations", id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"montant\":100000}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.frais").value(1250))
                .andExpect(jsonPath("$.details").value("100000 * 0.0125"));

        mvc.perform(post("/api/v1/grilles/{id}/simulations", id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"montant\":10001}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("AUCUNE_TRANCHE"));

        GrilleTarifaire enBase = tx.execute(s -> {
            GrilleTarifaire g = grilles.findById(java.util.UUID.fromString(id)).orElseThrow();
            g.getTranches().size();
            return g;
        });
        assertThat(enBase.getStatut()).isEqualTo(StatutGrille.BROUILLON);
        assertThat(enBase.getTranches().get(1).getParametres()).isEqualTo(Map.of("taux", new BigDecimal("0.0125")));
    }

    @Test
    @WithMockUser(username = "maker", roles = "CONFIG_MANAGER")
    void grilleCibleeParSourceEtDestination() throws Exception {
        String corps = """
                {"code":"INTL-SAHEL","libelle":"Sahel vers CEMAC","produit":"TRANSFERT_INTL","devise":"XOF",
                 "dateDebut":"2026-01-01",
                 "source":{"uniteId":"%s"},"destination":{"zone":"cemac"},
                 "tranches":[{"montantMin":0,"formule":{"code":"FIXED","version":1},"parametres":{"montantFixe":1500}}]}"""
                .formatted(reseau.id("MS-SAHEL"));
        MvcResult cree = mvc.perform(post("/api/v1/grilles").contentType(MediaType.APPLICATION_JSON).content(corps))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.source.niveau").value("UNITE"))
                .andExpect(jsonPath("$.source.uniteCode").value("MS-SAHEL"))
                .andExpect(jsonPath("$.source.zone").value("UEMOA"))
                .andExpect(jsonPath("$.destination.niveau").value("ZONE"))
                .andExpect(jsonPath("$.destination.zone").value("CEMAC"))
                .andReturn();
        String id = com.jayway.jsonpath.JsonPath.read(cree.getResponse().getContentAsString(), "$.id");
        mvc.perform(post("/api/v1/grilles/{id}/publier", id)).andExpect(status().isOk());

        mvc.perform(get("/api/v1/grilles").param("source", "ms-sahel").param("destination", "CEMAC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.contenu[0].code").value("INTL-SAHEL"));

        mvc.perform(post("/api/v1/frais/calculer").contentType(MediaType.APPLICATION_JSON).content("""
                        {"produitCode":"TRANSFERT_INTL","devise":"XOF","montant":20000,
                         "source":{"uniteId":"%s"},"destination":{"pays":"CM"}}""".formatted(reseau.id("SD-SAHEL-01"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.grilleCode").value("INTL-SAHEL"))
                .andExpect(jsonPath("$.frais").value(1500))
                .andExpect(jsonPath("$.source.cible").value("MS-SAHEL"))
                .andExpect(jsonPath("$.destination.niveau").value("ZONE"));

        mvc.perform(post("/api/v1/frais/calculer").contentType(MediaType.APPLICATION_JSON).content("""
                        {"produitCode":"TRANSFERT_INTL","devise":"XOF","montant":20000,
                         "source":{"uniteId":"%s"},"destination":{"pays":"CM"}}""".formatted(reseau.id("AG-KAYA"))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("AUCUNE_GRILLE_APPLICABLE"));
    }

    /**
     * 2 000 grilles publiées visent chacune un sous-distributeur : le moteur ne lit en base que celles qui peuvent
     * viser l'opération (lignée, regroupements, zone, « toutes »), et le calcul reste constant.
     */
    @Test
    void selectionEnBaseParmiDeuxMilleGrillesCiblees() {
        UUID fixed = jdbc.queryForObject("SELECT id FROM formule WHERE code = 'FIXED' AND version = 1", UUID.class);
        UUID msSahel = reseau.id("MS-SAHEL");
        try {
            List<Object[]> grillesVolume = new ArrayList<>();
            for (int i = 1; i <= 2000; i++) {
                UUID parent = i <= 3 ? msSahel : UUID.randomUUID();
                grillesVolume.add(ligne("V-%04d".formatted(i), UUID.randomUUID(), "SD-V%04d".formatted(i),
                        "SOUS_DISTRIBUTEUR", parent, 99));
            }
            grillesVolume.add(ligne("V-OHG", reseau.id("AG-OUAHIGOUYA"), "AG-OUAHIGOUYA", "AGENCE",
                    reseau.id("FSE-UEMOA"), 40));
            grillesVolume.add(ligne("V-TOUTES", null, null, null, null, 10));
            inserer(grillesVolume, fixed);

            UUID sd = reseau.id("SD-SAHEL-01");
            List<GrilleTarifaire> candidates = grilles.candidates("VOLUME", null, "XOF", DATE,
                    GrilleTarifaireRepository.Perimetre.de(reseau.unite(sd)), GrilleTarifaireRepository.Perimetre.INCONNU);
            assertThat(candidates).extracting(GrilleTarifaire::getCode).containsExactlyInAnyOrder("V-OHG", "V-TOUTES");
            assertThat(calculer(sd).grilleCode()).isEqualTo("V-OHG");

            inserer(List.<Object[]>of(ligne("V-SD", sd, "SD-SAHEL-01", "SOUS_DISTRIBUTEUR", msSahel, 70)), fixed);
            long debut = System.nanoTime();
            for (int i = 0; i < 200; i++) {
                assertThat(calculer(sd).frais()).isEqualByComparingTo("70");
            }
            System.out.printf("Calcul parmi 2 003 grilles ciblées : %.2f ms en moyenne%n",
                    (System.nanoTime() - debut) / 200 / 1_000_000.0);

            GrilleApplicable a = moteur.applicable(new DemandeFrais("VOLUME", null, "XOF", null, DATE,
                    new Localisation(sd, null, null), null));
            assertThat(a.statut()).isEqualTo(GrilleApplicable.Statut.TROUVEE);
            assertThat(a.grille().code()).isEqualTo("V-SD");
            assertThat(a.autres()).extracting(GrilleApplicable.Retenue::code).containsExactly("V-OHG", "V-TOUTES");
            assertThat(a.voisinesSource()).as("trois grilles visent d'autres sous-distributeurs de MS-SAHEL").isEqualTo(3);
            assertThat(a.voisinesDestination()).isZero();
        } finally {
            jdbc.update("DELETE FROM grille_tarifaire WHERE produit = 'VOLUME'");
        }
    }

    private ResultatFrais calculer(UUID source) {
        return moteur.calculer(new DemandeFrais("VOLUME", null, "XOF", new BigDecimal("1000"), DATE,
                new Localisation(source, null, null), null));
    }

    private static Object[] ligne(String code, UUID unite, String uniteCode, String type, UUID parent, int frais) {
        return new Object[]{UUID.randomUUID(), code, unite, uniteCode, type, parent, frais};
    }

    /** Insertion directe (lots JDBC) de grilles publiées à une tranche FIXED, produit VOLUME. */
    private void inserer(List<Object[]> lignes, UUID formule) {
        jdbc.batchUpdate("INSERT INTO grille_tarifaire (id, code, libelle, produit, devise, date_debut, version, statut,"
                + " source_zone, source_unite_id, source_unite_code, source_unite_libelle, source_unite_type,"
                + " source_unite_parent_id, created_by, updated_by)"
                + " VALUES (?, ?, ?, 'VOLUME', 'XOF', DATE '2026-01-01', 1, 'PUBLIEE', ?, ?, ?, ?, ?, ?, 'test', 'test')",
                lignes.stream().map(l -> new Object[]{l[0], l[1], "Volume " + l[1], l[2] == null ? null : "UEMOA",
                        l[2], l[3], l[3], l[4], l[5]}).toList());
        jdbc.batchUpdate("INSERT INTO tranche_tarifaire (id, grille_id, ordre, montant_min, formule_id, parametres)"
                        + " VALUES (?, ?, 1, 0, ?, CAST(? AS jsonb))",
                lignes.stream().map(l -> new Object[]{UUID.randomUUID(), l[0], formule,
                        "{\"montantFixe\": " + l[6] + "}"}).toList());
    }

    private static DemandeFrais demande(String produit, String canal, String montant) {
        return new DemandeFrais(produit, canal, "XOF", new BigDecimal(montant), DATE);
    }
}
