package com.gateway.payment.network;

import com.gateway.payment.network.api.dto.NetworkDtos.ErreurImport;
import com.gateway.payment.network.api.dto.NetworkDtos.NoeudArbre;
import com.gateway.payment.network.api.dto.NetworkDtos.PageDto;
import com.gateway.payment.network.api.dto.NetworkDtos.PositionDto;
import com.gateway.payment.network.api.dto.NetworkDtos.RapportImport;
import com.gateway.payment.network.api.dto.NetworkDtos.UniteResume;
import com.gateway.payment.network.domain.UniteOrganisation;
import com.gateway.payment.network.repository.UniteOrganisationRepository;
import com.gateway.payment.network.service.ImportService;
import com.gateway.payment.network.service.ModeleImport;
import com.gateway.payment.network.service.PositionService;
import com.gateway.payment.network.service.RegroupementService;
import com.gateway.payment.network.service.UniteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Import en masse sur un vrai PostgreSQL (ignoré sans Docker). Pas de transaction de test : l'import gère la sienne,
 * annulée en essai ou en cas d'erreur, ce que l'on vérifie en relisant la base. Chaque test utilise ses propres codes.
 */
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class ImportPostgresTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void proprietes(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("network.security.keycloak.enabled", () -> "false");
    }

    private static final UUID FSE_UEMOA = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID MS_KAYA = UUID.fromString("10000000-0000-0000-0000-000000000023");
    private static final UUID RG_NORD = UUID.fromString("10000000-0000-0000-0000-000000000041");

    @Autowired
    private ImportService imports;
    @Autowired
    private UniteService service;
    @Autowired
    private RegroupementService regroupements;
    @Autowired
    private PositionService positions;
    @Autowired
    private UniteOrganisationRepository unites;

    @Test
    void essaiSansEcriturePuisApplicationDansNImporteQuelOrdre() {
        String csv = """
                code;libelle;type;code_parent;zone
                SD-YAKO-01;Boutique Yako;Sous-distributeur;MS-YAKO-01;
                MS-YAKO-01;Master Yako;master;AG-YAKO;
                AG-YAKO;Agence de Yako;AGENCE;FSE-UEMOA;uemoa
                RG-YAKO;Réseau Yako;REGROUPEMENT;AG-YAKO;
                """;

        RapportImport essai = unitesCsv(csv, true);
        assertThat(essai.erreurs()).isEmpty();
        assertThat(essai.applique()).isFalse();
        assertThat(essai.nbCreations()).isEqualTo(4);
        assertThat(unites.findByCodeIn(List.of("AG-YAKO", "SD-YAKO-01"))).isEmpty();

        RapportImport application = unitesCsv(csv, false);
        assertThat(application.applique()).isTrue();
        assertThat(application.nbCreations()).isEqualTo(4);
        UniteOrganisation agence = code("AG-YAKO");
        UniteOrganisation sd = code("SD-YAKO-01");
        assertThat(agence.getParentId()).isEqualTo(FSE_UEMOA);
        assertThat(agence.getNiveau()).isEqualTo(3);
        assertThat(agence.getCreatedBy()).isEqualTo("admin");
        assertThat(sd.getNiveau()).isEqualTo(5);
        assertThat(sd.getChemin()).startsWith(agence.getChemin()).endsWith(sd.getId() + "/");
        assertThat(service.ascendants(sd.getId())).extracting(UniteResume::code)
                .containsExactly("BF", "FSE-UEMOA", "AG-YAKO", "MS-YAKO-01");

        RapportImport membres = membresCsv("code_regroupement;code_membre\nRG-YAKO;MS-YAKO-01\n", false);
        assertThat(membres.applique()).isTrue();
        assertThat(membres.nbCreations()).isEqualTo(1);
        assertThat(membresCsv("code_regroupement;code_membre\nrg-yako;ms-yako-01\n", false).nbInchangees()).isEqualTo(1);
        assertThat(regroupements.couvertures(sd.getId())).extracting(c -> c.regroupement().code() + " via " + c.via().code())
                .contains("RG-YAKO via MS-YAKO-01");

        RapportImport deplacement = unitesCsv("""
                code;libelle;type;code_parent
                AG-YAKO;Agence de Yako (centre);AGENCE;FSE-UEMOA
                MS-KAYA;Master Kaya Services;MASTER;AG-YAKO
                SD-YAKO-01;Boutique Yako;SOUS_DISTRIBUTEUR;MS-YAKO-01
                """, false);
        assertThat(deplacement.erreurs()).isEmpty();
        assertThat(deplacement.nbMisesAJour()).isEqualTo(1);
        assertThat(deplacement.nbDeplacements()).isEqualTo(1);
        assertThat(deplacement.nbInchangees()).isEqualTo(1);
        assertThat(code("AG-YAKO").getLibelle()).isEqualTo("Agence de Yako (centre)");
        assertThat(unites.findById(MS_KAYA).orElseThrow().getChemin()).isEqualTo(agence.getChemin() + MS_KAYA + "/");

        RapportImport refuse = unitesCsv("""
                code;libelle;type;code_parent
                AG-YAKO;Libellé jamais enregistré;AGENCE;FSE-UEMOA
                MS-YAKO-01;Master Yako;MASTER;AG-KAYA
                """, false);
        assertThat(refuse.applique()).isFalse();
        assertThat(refuse.erreurs()).singleElement().satisfies(e -> {
            assertThat(e.ligne()).isEqualTo(3);
            assertThat(e.codeErreur()).isEqualTo("SORTIE_BRANCHE_REGROUPEMENT");
        });
        assertThat(code("AG-YAKO").getLibelle()).isEqualTo("Agence de Yako (centre)");
        assertThat(code("MS-YAKO-01").getParentId()).isEqualTo(agence.getId());
    }

    @Test
    void erreursRapporteesLigneParLigneEtRienNEstEcrit() {
        RapportImport r = unitesCsv("""
                code;libelle;type;code_parent;zone
                AG-VALIDE;Agence valide;AGENCE;FSE-UEMOA;
                AG X!;Code invalide;AGENCE;FSE-UEMOA;
                AG-TYPE;Type inconnu;GUICHET;FSE-UEMOA;
                AG-ORPHELINE;Parent inconnu;AGENCE;FSE-INCONNU;
                MS-MAL-PLACE;Master sous un FSE;MASTER;FSE-UEMOA;
                AG-VALIDE;Doublon;AGENCE;FSE-UEMOA;
                BFA;Pays mal codé;PAYS;;UEMOA
                AG-BOUCLE-1;Boucle 1;AGENCE;AG-BOUCLE-2;
                AG-BOUCLE-2;Boucle 2;AGENCE;AG-BOUCLE-1;
                AG-KAYA;Agence Kaya;MASTER;FSE-UEMOA;
                SD-SOUS-ERREUR;Sous un master en erreur;SOUS_DISTRIBUTEUR;MS-MAL-PLACE;
                AG-CEMAC;Mauvaise zone;AGENCE;FSE-UEMOA;CEMAC
                """, false);

        assertThat(r.applique()).isFalse();
        assertThat(r.nbLignes()).isEqualTo(12);
        assertThat(r.erreurs()).extracting(e -> e.ligne() + ":" + e.codeErreur()).containsExactly(
                "3:CODE_INVALIDE",
                "4:TYPE_INCONNU",
                "5:PARENT_INEXISTANT",
                "6:RATTACHEMENT_INTERDIT",
                "7:DOUBLON_FICHIER",
                "8:CODE_PAYS_INVALIDE",
                "9:PARENT_EN_ERREUR",
                "10:BOUCLE",
                "11:TYPE_DIFFERENT",
                "12:PARENT_EN_ERREUR",
                "13:ZONE_INCOHERENTE");
        assertThat(r.erreurs()).filteredOn(e -> e.ligne() == 6).extracting(ErreurImport::message)
                .singleElement().asString().contains("Master", "FSE-UEMOA");
        assertThat(unites.findByCodeIn(List.of("AG-VALIDE"))).isEmpty();
    }

    @Test
    void membresControlesAvantAjout() {
        RapportImport r = membresCsv("""
                code_regroupement;code_membre
                RG-AGENCES-NORD;AG-BOBO
                RG-AGENCES-NORD;MS-SAHEL
                RG-AGENCES-NORD;AG-DOUALA
                AG-KAYA;MS-KAYA
                RG-EQUIPE-SAHEL;SD-INCONNU
                RG-AGENCES-NORD;AG-BOBO
                """, false);

        assertThat(r.applique()).isFalse();
        assertThat(r.erreurs()).extracting(e -> e.ligne() + ":" + e.codeErreur()).containsExactly(
                "3:DOUBLON_COUVERTURE",
                "4:ZONE_INCOHERENTE",
                "5:PAS_UN_REGROUPEMENT",
                "6:UNITE_INEXISTANTE",
                "7:DOUBLON_FICHIER");
        assertThat(regroupements.membres(RG_NORD)).extracting(m -> m.unite().code())
                .containsExactly("AG-KAYA", "AG-OUAHIGOUYA");
    }

    @Test
    void lesModelesSImportentTelsQuels() {
        ModeleImport xlsx = imports.modele("unites", "xlsx");
        RapportImport r = imports.importerUnites(xlsx.nomFichier(), xlsx.contenu(), true, "admin");
        assertThat(r.format()).isEqualTo("XLSX");
        assertThat(r.erreurs()).isEmpty();
        assertThat(r.nbLignes()).isEqualTo(5);

        ModeleImport csv = imports.modele("membres", "csv");
        assertThat(new String(csv.contenu(), StandardCharsets.UTF_8)).startsWith("\uFEFFcode_regroupement;code_membre");
    }

    /** ~6 000 unités : une agence par regroupement, 5 masters de 8 sous-distributeurs et 5 marchands par agence. */
    @Test
    void volumeDeSixMilleUnites() {
        List<String> lignes = new ArrayList<>();
        StringBuilder membresCsv = new StringBuilder("code_regroupement;code_membre\n");
        for (int a = 1; a <= 120; a++) {
            String ag = "AG-V%03d".formatted(a);
            lignes.add(ag + ";Agence volume " + a + ";AGENCE;FSE-UEMOA;");
            lignes.add("RG-V%03d;Réseau volume %d;REGROUPEMENT;%s;".formatted(a, a, ag));
            for (int m = 1; m <= 5; m++) {
                String ms = "MS-V%03d-%d".formatted(a, m);
                lignes.add(ms + ";Master " + a + "-" + m + ";MASTER;" + ag + ";");
                lignes.add("MC-V%03d-%d;Marchand %d-%d;MARCHAND;%s;".formatted(a, m, a, m, ag));
                for (int s = 1; s <= 8; s++) {
                    lignes.add("SD-V%03d-%d-%d;Boutique %d-%d-%d;SOUS_DISTRIBUTEUR;%s;".formatted(a, m, s, a, m, s, ms));
                }
                if (m <= 3) {
                    membresCsv.append("RG-V%03d;%s\n".formatted(a, ms));
                }
            }
        }
        Collections.shuffle(lignes, new Random(42));
        String csv = "code;libelle;type;code_parent;zone\n" + String.join("\n", lignes);

        RapportImport essai = unitesCsv(csv, true);
        assertThat(essai.erreurs()).isEmpty();
        assertThat(essai.nbCreations()).isEqualTo(6240);
        RapportImport application = unitesCsv(csv, false);
        assertThat(application.applique()).isTrue();
        RapportImport membres = membresCsv(membresCsv.toString(), false);
        assertThat(membres.nbCreations()).isEqualTo(360);
        System.out.printf("Import 6240 unités : essai %d ms, application %d ms ; 360 membres : %d ms%n",
                essai.dureeMs(), application.dureeMs(), membres.dureeMs());

        long debut = System.nanoTime();
        PageDto<UniteResume> page = service.rechercher(
                new UniteService.Filtre("UEMOA", null, null, null, null, false, "sd-v"),
                PageRequest.of(2, 50, Sort.by("code")));
        PageDto<NoeudArbre> agences = service.enfants(FSE_UEMOA, PageRequest.of(0, 100, Sort.by("code")));
        UniteOrganisation sd = code("SD-V077-2-5");
        PositionDto position = positions.position(sd.getId(), null, null);
        PageDto<UniteResume> perimetre = regroupements.perimetre(code("RG-V077").getId(), PageRequest.of(0, 10, Sort.by("code")));
        System.out.printf("Lectures sur le réseau chargé : %d ms%n", (System.nanoTime() - debut) / 1_000_000);

        assertThat(page.total()).isEqualTo(4800);
        assertThat(page.contenu()).hasSize(50);
        assertThat(agences.total()).isGreaterThanOrEqualTo(124);
        assertThat(agences.contenu()).hasSize(100);
        assertThat(agences.contenu()).filteredOn(n -> n.code().equals("AG-V001")).singleElement()
                .satisfies(n -> assertThat(n.nbEnfants()).isEqualTo(11));
        assertThat(position.lignee()).extracting(UniteResume::code)
                .containsExactly("SD-V077-2-5", "MS-V077-2", "AG-V077", "FSE-UEMOA", "BF");
        assertThat(position.regroupements()).extracting(c -> c.regroupement().code() + " via " + c.via().code())
                .containsExactly("RG-V077 via MS-V077-2");
        assertThat(perimetre.total()).isEqualTo(27);
    }

    private RapportImport unitesCsv(String csv, boolean essai) {
        return imports.importerUnites("unites.csv", csv.getBytes(StandardCharsets.UTF_8), essai, "admin");
    }

    private RapportImport membresCsv(String csv, boolean essai) {
        return imports.importerMembres("membres.csv", csv.getBytes(StandardCharsets.UTF_8), essai, "admin");
    }

    private UniteOrganisation code(String code) {
        return unites.findByCodeIn(List.of(code)).stream().findFirst().orElseThrow();
    }
}
