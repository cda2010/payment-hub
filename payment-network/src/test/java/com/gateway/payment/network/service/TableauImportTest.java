package com.gateway.payment.network.service;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TableauImportTest {

    private static final List<String> COLONNES = List.of("code", "libelle", "type", "code_parent");
    private static final Map<String, String> ALIAS = Map.of("parent", "code_parent");

    @Test
    void csvPointVirguleAvecBomGuillemetsEtLignesVides() {
        String csv = "\uFEFFCode;Libellé;TYPE;Code parent\r\n"
                + "AG-DORI;\"Agence de Dori; centre\";Agence;FSE-UEMOA\r\n"
                + "\r\n"
                + ";;;\r\n"
                + "MS-DORI;\"Master \"\"Dori\"\"\";MASTER;AG-DORI";

        TableauImport t = TableauImport.lire("unites.csv", csv.getBytes(StandardCharsets.UTF_8), COLONNES, ALIAS);

        assertThat(t.format()).isEqualTo("CSV");
        assertThat(t.lignes()).hasSize(2);
        assertThat(t.lignes().get(0).numero()).isEqualTo(2);
        assertThat(t.lignes().get(0).valeur("libelle")).isEqualTo("Agence de Dori; centre");
        assertThat(t.lignes().get(1).numero()).isEqualTo(5);
        assertThat(t.lignes().get(1).valeur("libelle")).isEqualTo("Master \"Dori\"");
        assertThat(t.lignes().get(1).valeur("code_parent")).isEqualTo("AG-DORI");
    }

    @Test
    void csvVirguleEtWindows1252CommeExcelEnFrancais() {
        String csv = "code,libelle,type,parent\nSD-01,Boutique Dédougou,SOUS_DISTRIBUTEUR,MS-01\n";

        TableauImport t = TableauImport.lire("x.csv", csv.getBytes(Charset.forName("windows-1252")), COLONNES, ALIAS);

        assertThat(t.lignes()).singleElement().satisfies(l -> {
            assertThat(l.valeur("libelle")).isEqualTo("Boutique Dédougou");
            assertThat(l.valeur("code_parent")).isEqualTo("MS-01");
        });
    }

    @Test
    void classeurExcelPremiereFeuille() throws Exception {
        byte[] xlsx;
        try (XSSFWorkbook classeur = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet feuille = classeur.createSheet("Unites");
            ligne(feuille, 0, "code", "libelle", "type", "code_parent", "zone");
            ligne(feuille, 2, "BF2", "Pays test", "PAYS", "", "UEMOA");
            Row numerique = feuille.createRow(3);
            numerique.createCell(0).setCellValue(12345);
            numerique.createCell(1).setCellValue("Agence numérique");
            numerique.createCell(2).setCellValue("AGENCE");
            numerique.createCell(3).setCellValue("FSE-UEMOA");
            classeur.createSheet("Aide");
            classeur.write(out);
            xlsx = out.toByteArray();
        }

        TableauImport t = TableauImport.lire("unites.xlsx", xlsx, COLONNES, ALIAS);

        assertThat(t.format()).isEqualTo("XLSX");
        assertThat(t.lignes()).extracting(TableauImport.Ligne::numero).containsExactly(3, 4);
        assertThat(t.lignes().get(0).valeur("zone")).isEqualTo("UEMOA");
        assertThat(t.lignes().get(1).valeur("code")).isEqualTo("12345");
    }

    @Test
    void fichiersRefuses() {
        assertThatThrownBy(() -> TableauImport.lire("x.csv", new byte[0], COLONNES, ALIAS))
                .hasFieldOrPropertyWithValue("code", "FICHIER_VIDE");
        assertThatThrownBy(() -> TableauImport.lire("x.csv", "code;libelle\nA;B\n".getBytes(StandardCharsets.UTF_8),
                COLONNES, ALIAS))
                .hasFieldOrPropertyWithValue("code", "COLONNES_MANQUANTES")
                .hasMessageContaining("type, code_parent");
        assertThatThrownBy(() -> TableauImport.lire("x.xlsx", "PK pas un classeur".getBytes(StandardCharsets.UTF_8),
                COLONNES, ALIAS))
                .hasFieldOrPropertyWithValue("code", "FICHIER_ILLISIBLE");
    }

    @Test
    void nomsDeColonnesNormalises() {
        assertThat(TableauImport.colonne(" Code-Parent ", Map.of())).isEqualTo("code_parent");
        assertThat(TableauImport.colonne("Libellé", Map.of())).isEqualTo("libelle");
        assertThat(TableauImport.colonne("Parent", ALIAS)).isEqualTo("code_parent");
        assertThat(ImportService.normaliser("Sous-distributeur")).isEqualTo("SOUS_DISTRIBUTEUR");
    }

    private static void ligne(Sheet feuille, int numero, String... valeurs) {
        Row row = feuille.createRow(numero);
        for (int i = 0; i < valeurs.length; i++) {
            row.createCell(i).setCellValue(valeurs[i]);
        }
    }
}
