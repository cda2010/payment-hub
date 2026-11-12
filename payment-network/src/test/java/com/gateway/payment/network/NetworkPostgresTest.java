package com.gateway.payment.network;

import com.gateway.payment.network.NetworkException;
import com.gateway.payment.network.api.dto.NetworkDtos.CreationUniteRequest;
import com.gateway.payment.network.api.dto.NetworkDtos.CreationZoneRequest;
import com.gateway.payment.network.api.dto.NetworkDtos.ModificationZoneRequest;
import com.gateway.payment.network.api.dto.NetworkDtos.NoeudArbre;
import com.gateway.payment.network.api.dto.NetworkDtos.PageDto;
import com.gateway.payment.network.api.dto.NetworkDtos.PositionDto;
import com.gateway.payment.network.api.dto.NetworkDtos.UniteResume;
import com.gateway.payment.network.domain.TypeUnite;
import com.gateway.payment.network.domain.UniteOrganisation;
import com.gateway.payment.network.repository.UniteOrganisationRepository;
import com.gateway.payment.network.service.PositionService;
import com.gateway.payment.network.service.RegroupementService;
import com.gateway.payment.network.service.UniteService;
import com.gateway.payment.network.service.ZoneService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Migrations Flyway, jeu de démonstration et requêtes sur les chemins, sur un vrai PostgreSQL (ignoré sans Docker). */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
@Transactional
class NetworkPostgresTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void proprietes(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("network.security.keycloak.enabled", () -> "false");
    }

    private static final UUID BURKINA = UUID.fromString("a0000000-0000-0000-0000-000000000854");
    private static final UUID MALI = UUID.fromString("a0000000-0000-0000-0000-000000000466");
    private static final UUID FSE_UEMOA = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID FSE_CEMAC = UUID.fromString("20000000-0000-0000-0000-000000000001");
    private static final UUID AG_OUAHIGOUYA = UUID.fromString("10000000-0000-0000-0000-000000000011");
    private static final UUID AG_KAYA = UUID.fromString("10000000-0000-0000-0000-000000000012");
    private static final UUID AG_BOBO = UUID.fromString("10000000-0000-0000-0000-000000000013");
    private static final UUID MS_SAHEL = UUID.fromString("10000000-0000-0000-0000-000000000021");
    private static final UUID MC_BOBO = UUID.fromString("10000000-0000-0000-0000-000000000024");
    private static final UUID SD_SAHEL_01 = UUID.fromString("10000000-0000-0000-0000-000000000031");
    private static final UUID RG_NORD = UUID.fromString("10000000-0000-0000-0000-000000000041");
    private static final UUID RG_PAYS_SAHEL = UUID.fromString("10000000-0000-0000-0000-000000000042");
    private static final UUID RG_EQUIPE_SAHEL = UUID.fromString("10000000-0000-0000-0000-000000000043");

    private static final Pageable PAR_CODE = PageRequest.of(0, 100, Sort.by("code"));
    private static final Pageable PAR_NIVEAU = PageRequest.of(0, 100, Sort.by("niveau", "code"));

    @Autowired
    private UniteService service;
    @Autowired
    private RegroupementService regroupements;
    @Autowired
    private UniteOrganisationRepository unites;
    @Autowired
    private ZoneService zones;
    @Autowired
    private PositionService positions;
    @Autowired
    private MockMvc mvc;

    @Test
    void arbreDeDemonstrationParZoneChargeNiveauParNiveau() {
        List<NoeudArbre> uemoa = service.racines("UEMOA");

        assertThat(uemoa).extracting(NoeudArbre::code).containsExactly("BF", "BJ", "CI", "GW", "ML", "NE", "SN", "TG");
        assertThat(uemoa.get(0).nbEnfants()).isEqualTo(1);
        assertThat(uemoa.get(1).nbEnfants()).isZero();
        assertThat(service.enfants(BURKINA, PAR_CODE).contenu()).singleElement().satisfies(fse -> {
            assertThat(fse.code()).isEqualTo("FSE-UEMOA");
            assertThat(fse.nbEnfants()).isEqualTo(4);
        });
        PageDto<NoeudArbre> agences = service.enfants(FSE_UEMOA, PAR_CODE);
        assertThat(agences.contenu()).extracting(NoeudArbre::code)
                .containsExactly("AG-BOBO", "AG-KAYA", "AG-OUAHIGOUYA", "RG-AGENCES-NORD");
        assertThat(agences.contenu().get(3)).satisfies(rg -> {
            assertThat(rg.nbMembres()).isEqualTo(2);
            assertThat(rg.nbEnfants()).isZero();
        });
        PageDto<NoeudArbre> premiere = service.enfants(FSE_UEMOA, PageRequest.of(0, 3, Sort.by("code")));
        assertThat(premiere.total()).isEqualTo(4);
        assertThat(premiere.contenu()).hasSize(3);

        List<NoeudArbre> cemac = service.racines("CEMAC");
        assertThat(cemac).extracting(NoeudArbre::code).containsExactly("CF", "CG", "CM", "GA", "GQ", "TD");
        assertThat(cemac.get(2).nbEnfants()).isEqualTo(1);
    }

    @Test
    void positionDUneUniteDUnPaysEtDUneZone() {
        PositionDto sd = positions.position(SD_SAHEL_01, null, null);
        assertThat(sd.zone()).isEqualTo("UEMOA");
        assertThat(sd.devise()).isEqualTo("XOF");
        assertThat(sd.unite().code()).isEqualTo("SD-SAHEL-01");
        assertThat(sd.lignee()).extracting(UniteResume::code)
                .containsExactly("SD-SAHEL-01", "MS-SAHEL", "AG-OUAHIGOUYA", "FSE-UEMOA", "BF");
        assertThat(sd.regroupements()).extracting(c -> c.regroupement().code() + " via " + c.via().code())
                .containsExactlyInAnyOrder("RG-AGENCES-NORD via AG-OUAHIGOUYA", "RG-EQUIPE-SAHEL via SD-SAHEL-01");

        PositionDto mali = positions.position(null, "ml", null);
        assertThat(mali.unite().id()).isEqualTo(MALI);
        assertThat(mali.lignee()).extracting(UniteResume::code).containsExactly("ML");

        PositionDto cemac = positions.position(null, null, "cemac");
        assertThat(cemac.devise()).isEqualTo("XAF");
        assertThat(cemac.unite()).isNull();
        assertThat(cemac.lignee()).isEmpty();

        assertThatThrownBy(() -> positions.position(null, "ZZ", null))
                .isInstanceOf(NetworkException.class).hasMessageContaining("ZZ");
        assertThatThrownBy(() -> positions.position(SD_SAHEL_01, "BF", null))
                .isInstanceOf(NetworkException.class).hasMessageContaining("exactement un");
    }

    @Test
    void laMigrationPlaceLesFseSousLeurPaysEtDecaleLaBranche() {
        UniteOrganisation fseUemoa = unites.findById(FSE_UEMOA).orElseThrow();
        UniteOrganisation sd = unites.findById(SD_SAHEL_01).orElseThrow();

        assertThat(fseUemoa.getParentId()).isEqualTo(BURKINA);
        assertThat(fseUemoa.getNiveau()).isEqualTo(2);
        assertThat(sd.getNiveau()).isEqualTo(5);
        assertThat(sd.getChemin()).isEqualTo("/" + BURKINA + "/" + FSE_UEMOA + "/" + AG_OUAHIGOUYA + "/" + MS_SAHEL
                + "/" + SD_SAHEL_01 + "/");
        assertThat(unites.findById(FSE_CEMAC).orElseThrow().getParentId())
                .isEqualTo(UUID.fromString("a0000000-0000-0000-0000-000000000120"));
        assertThat(service.descendants(BURKINA, PAR_NIVEAU).total()).isEqualTo(12);
    }

    @Test
    void laMigrationRangeLesRegroupementsDansLArbre() {
        UniteOrganisation nord = unites.findById(RG_NORD).orElseThrow();
        assertThat(nord.getParentId()).isEqualTo(FSE_UEMOA);
        assertThat(nord.getNiveau()).isEqualTo(3);
        assertThat(nord.getChemin()).isEqualTo("/" + BURKINA + "/" + FSE_UEMOA + "/" + RG_NORD + "/");
        assertThat(unites.findById(RG_PAYS_SAHEL)).isEmpty();

        UniteOrganisation equipe = unites.findById(RG_EQUIPE_SAHEL).orElseThrow();
        assertThat(equipe.getParentId()).isEqualTo(MS_SAHEL);
        assertThat(equipe.getNiveau()).isEqualTo(5);
        assertThat(regroupements.membres(RG_EQUIPE_SAHEL)).extracting(m -> m.unite().code())
                .containsExactly("SD-SAHEL-01", "SD-SAHEL-02");
        assertThat(regroupements.couvertures(SD_SAHEL_01)).extracting(c -> c.regroupement().code() + " via " + c.via().code())
                .containsExactlyInAnyOrder("RG-AGENCES-NORD via AG-OUAHIGOUYA", "RG-EQUIPE-SAHEL via SD-SAHEL-01");
        assertThat(service.ascendants(RG_EQUIPE_SAHEL)).extracting(UniteResume::code)
                .containsExactly("BF", "FSE-UEMOA", "AG-OUAHIGOUYA", "MS-SAHEL");
    }

    @Test
    void unRegroupementSeCreeEtSeDeplaceDansLArbre() {
        UniteOrganisation rg = creer("RG-BOBO", TypeUnite.REGROUPEMENT, null, AG_BOBO);
        assertThat(rg.getNiveau()).isEqualTo(4);
        regroupements.ajouterMembre(rg.getId(), MC_BOBO, "admin");
        assertThatThrownBy(() -> regroupements.ajouterMembre(rg.getId(), AG_KAYA, "admin"))
                .hasFieldOrPropertyWithValue("code", "MEMBRE_HORS_BRANCHE");
        assertThatThrownBy(() -> creer("AG-X", TypeUnite.AGENCE, null, rg.getId()))
                .hasFieldOrPropertyWithValue("code", "RATTACHEMENT_INTERDIT");
        assertThatThrownBy(() -> creer("RG-RACINE", TypeUnite.REGROUPEMENT, "UEMOA", null))
                .hasFieldOrPropertyWithValue("code", "PARENT_OBLIGATOIRE");

        assertThatThrownBy(() -> service.deplacer(RG_EQUIPE_SAHEL, AG_BOBO, "admin"))
                .hasFieldOrPropertyWithValue("code", "MEMBRE_HORS_BRANCHE");
        service.deplacer(RG_NORD, BURKINA, "admin");
        UniteOrganisation nord = unites.findById(RG_NORD).orElseThrow();
        assertThat(nord.getNiveau()).isEqualTo(2);
        assertThat(nord.getChemin()).isEqualTo("/" + BURKINA + "/" + RG_NORD + "/");
    }

    @Test
    void lesFseNeChangentPasDePays() {
        assertThatThrownBy(() -> service.deplacer(FSE_UEMOA, MALI, "admin"))
                .hasFieldOrPropertyWithValue("code", "FSE_NON_DEPLACABLE");

        UniteOrganisation fseMali = creer("FSE-MALI", TypeUnite.FSE, null, MALI);
        assertThat(fseMali.getZone()).isEqualTo("UEMOA");
        assertThat(fseMali.getNiveau()).isEqualTo(2);
        assertThat(service.ascendants(fseMali.getId())).extracting(UniteResume::code).containsExactly("ML");
    }

    @Test
    void uneNouvelleZoneAccueilleSesPaysPuisSeDesactiveQuandIlsLeSont() {
        zones.creer(new CreationZoneRequest("rdc", "Zone RDC", "cdf"), "admin");
        UniteOrganisation congo = creer("cd", TypeUnite.PAYS, "RDC", null);
        UniteOrganisation fse = creer("FSE-RDC", TypeUnite.FSE, null, congo.getId());

        assertThat(congo.getCode()).isEqualTo("CD");
        assertThat(congo.getNiveau()).isEqualTo(1);
        assertThat(fse.getZone()).isEqualTo("RDC");
        assertThat(service.consulter(fse.getId()).devise()).isEqualTo("CDF");
        assertThat(service.racines("rdc")).singleElement().satisfies(p -> assertThat(p.nbEnfants()).isEqualTo(1));
        assertThat(zones.consulter("RDC")).satisfies(z -> {
            assertThat(z.nbPays()).isEqualTo(1);
            assertThat(z.nbUnites()).isEqualTo(2);
        });
        assertThatThrownBy(() -> zones.modifier("RDC", new ModificationZoneRequest("Zone RDC", "USD"), "admin"))
                .hasFieldOrPropertyWithValue("code", "DEVISE_FIGEE");
        assertThatThrownBy(() -> zones.desactiver("RDC", "admin"))
                .hasFieldOrPropertyWithValue("code", "ZONE_AVEC_PAYS_ACTIFS");

        service.desactiver(fse.getId(), "admin");
        service.desactiver(congo.getId(), "admin");
        zones.desactiver("RDC", "admin");
        assertThatThrownBy(() -> service.activer(congo.getId(), "admin"))
                .hasFieldOrPropertyWithValue("code", "ZONE_INACTIVE");
        assertThatThrownBy(() -> creer("CG2", TypeUnite.PAYS, "UEMOA", null))
                .hasFieldOrPropertyWithValue("code", "CODE_PAYS_INVALIDE");
        assertThatThrownBy(() -> creer("BF", TypeUnite.PAYS, "UEMOA", null))
                .hasFieldOrPropertyWithValue("code", "CODE_EXISTANT");
    }

    @Test
    void lesAgencesDuNordCouvrentLeursFilles() {
        assertThat(regroupements.membres(RG_NORD)).extracting(m -> m.unite().code())
                .containsExactly("AG-KAYA", "AG-OUAHIGOUYA");
        assertThat(regroupements.perimetre(RG_NORD, PAR_NIVEAU).contenu()).extracting(UniteResume::code).containsExactly(
                "AG-KAYA", "AG-OUAHIGOUYA", "MC-MARCHE-OHG", "MS-KAYA", "MS-SAHEL", "SD-SAHEL-01", "SD-SAHEL-02");
        assertThat(regroupements.couvertures(SD_SAHEL_01)).filteredOn(c -> c.regroupement().id().equals(RG_NORD))
                .singleElement().satisfies(c -> assertThat(c.via().code()).isEqualTo("AG-OUAHIGOUYA"));
        assertThat(regroupements.couvertures(MC_BOBO)).isEmpty();
    }

    @Test
    void deplacementDUnMasterEtDeSaBranche() {
        service.deplacer(MS_SAHEL, AG_BOBO, "admin");

        UniteOrganisation master = unites.findById(MS_SAHEL).orElseThrow();
        UniteOrganisation sd = unites.findById(SD_SAHEL_01).orElseThrow();
        UniteOrganisation bobo = unites.findById(AG_BOBO).orElseThrow();
        assertThat(master.getParentId()).isEqualTo(AG_BOBO);
        assertThat(master.getChemin()).isEqualTo(bobo.getChemin() + MS_SAHEL + "/");
        assertThat(sd.getChemin()).isEqualTo(master.getChemin() + SD_SAHEL_01 + "/");
        assertThat(sd.getNiveau()).isEqualTo(5);
        assertThat(sd.getUpdatedBy()).isEqualTo("admin");
        assertThat(service.descendants(AG_BOBO, PAR_NIVEAU).contenu()).extracting(UniteResume::code)
                .containsExactly("MC-BOBO-STATION", "MS-SAHEL", "RG-EQUIPE-SAHEL", "SD-SAHEL-01", "SD-SAHEL-02");
        assertThat(unites.findById(RG_EQUIPE_SAHEL).orElseThrow().getChemin())
                .isEqualTo(master.getChemin() + RG_EQUIPE_SAHEL + "/");
        assertThat(regroupements.couvertures(SD_SAHEL_01)).extracting(c -> c.regroupement().code())
                .containsExactly("RG-EQUIPE-SAHEL");
    }

    @Test
    void deplacementRefuseSIlCouvreDeuxFoisUneUnite() {
        UniteOrganisation transversal = creer("RG-MIXTE", TypeUnite.REGROUPEMENT, null, FSE_UEMOA);
        regroupements.ajouterMembre(transversal.getId(), AG_KAYA, "admin");
        regroupements.ajouterMembre(transversal.getId(), MS_SAHEL, "admin");

        assertThatThrownBy(() -> service.deplacer(MS_SAHEL, AG_KAYA, "admin"))
                .hasFieldOrPropertyWithValue("code", "DOUBLON_COUVERTURE");
    }

    @Test
    void ascendantsEtCreationSousUnMaster() {
        UniteOrganisation sd = creer("SD-SAHEL-03", TypeUnite.SOUS_DISTRIBUTEUR, null, MS_SAHEL);

        assertThat(sd.getNiveau()).isEqualTo(5);
        assertThat(service.ascendants(sd.getId())).extracting(UniteResume::code)
                .containsExactly("BF", "FSE-UEMOA", "AG-OUAHIGOUYA", "MS-SAHEL");
        assertThat(regroupements.couvertures(sd.getId())).extracting(c -> c.regroupement().code())
                .containsExactly("RG-AGENCES-NORD");
    }

    @Test
    void desactivationRefuseeAvecDesEnfantsActifs() {
        assertThatThrownBy(() -> service.desactiver(AG_OUAHIGOUYA, "admin"))
                .hasFieldOrPropertyWithValue("code", "ENFANTS_ACTIFS");
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void cycleParLApi() throws Exception {
        mvc.perform(get("/api/v1/network/unites").param("zone", "UEMOA").param("type", "AGENCE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(3))
                .andExpect(jsonPath("$.contenu.length()").value(3));
        mvc.perform(get("/api/v1/network/unites").param("zone", "UEMOA").param("type", "AGENCE", "MASTER")
                        .param("taille", "2").param("page", "1").param("tri", "code"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(5))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.contenu[0].code").value("AG-OUAHIGOUYA"));
        mvc.perform(get("/api/v1/network/unites").param("branche", AG_OUAHIGOUYA.toString()).param("sansRegroupements", "true"))
                .andExpect(jsonPath("$.total").value(5));
        mvc.perform(get("/api/v1/network/arbre").param("zone", "CEMAC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[2].code").value("CM"))
                .andExpect(jsonPath("$[2].nbEnfants").value(1));
        mvc.perform(get("/api/v1/network/unites/{id}/enfants", FSE_UEMOA).param("taille", "2"))
                .andExpect(jsonPath("$.total").value(4))
                .andExpect(jsonPath("$.contenu[0].code").value("AG-BOBO"));
        mvc.perform(post("/api/v1/network/unites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"ms-x\",\"libelle\":\"Master X\",\"type\":\"MASTER\",\"parentId\":\"" + FSE_UEMOA + "\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("RATTACHEMENT_INTERDIT"));
        mvc.perform(post("/api/v1/network/regroupements/{id}/membres", RG_NORD)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"uniteId\":\"" + SD_SAHEL_01 + "\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("DOUBLON_COUVERTURE"));
        mvc.perform(post("/api/v1/network/regroupements/{id}/membres", RG_NORD)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"uniteId\":\"" + AG_BOBO + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.length()").value(3));
        mvc.perform(get("/api/v1/network/zones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[1].code").value("UEMOA"))
                .andExpect(jsonPath("$[1].nbPays").value(8))
                .andExpect(jsonPath("$[1].devise").value("XOF"));
        mvc.perform(post("/api/v1/network/zones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"cemac\",\"libelle\":\"Doublon\",\"devise\":\"XAF\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ZONE_EXISTANTE"));
        mvc.perform(get("/api/v1/network/unites/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("UNITE_INEXISTANTE"));
    }

    private UniteOrganisation creer(String code, TypeUnite type, String zone, UUID parentId) {
        String codeFse = type == TypeUnite.FSE ? code : null;
        UUID id = service.creer(new CreationUniteRequest(code, code, type, zone, parentId, codeFse), "admin").id();
        return unites.findById(id).orElseThrow();
    }
}
