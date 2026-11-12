package com.gateway.payment.network.service;

import com.gateway.payment.network.NetworkException;
import com.gateway.payment.network.domain.TypeUnite;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/** Fichier modèle d'import (en-tête et lignes d'exemple), en CSV ou en Excel avec une feuille d'aide. */
public record ModeleImport(String nomFichier, String typeContenu, byte[] contenu) {

    private static final String XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private static final List<List<String>> EXEMPLE_UNITES = List.of(
            List.of("AG-DORI", "Agence de Dori", "AGENCE", "FSE-UEMOA", ""),
            List.of("MS-DORI-01", "Master Dori Centre", "MASTER", "AG-DORI", ""),
            List.of("SD-DORI-01", "Boutique Dori Marché", "SOUS_DISTRIBUTEUR", "MS-DORI-01", ""),
            List.of("MC-DORI-01", "Supermarché Dori", "MARCHAND", "AG-DORI", ""),
            List.of("RG-DORI", "Réseau de Dori", "REGROUPEMENT", "AG-DORI", ""));

    private static final List<List<String>> EXEMPLE_MEMBRES = List.of(
            List.of("RG-DORI", "MS-DORI-01"),
            List.of("RG-DORI", "MC-DORI-01"));

    static ModeleImport de(String objet, String format) {
        String o = objet == null ? "" : objet.toLowerCase(Locale.ROOT);
        String f = format == null ? "csv" : format.toLowerCase(Locale.ROOT);
        List<String> colonnes;
        List<List<String>> exemple;
        switch (o) {
            case "unites" -> {
                colonnes = ImportService.COLONNES_UNITES;
                exemple = EXEMPLE_UNITES;
            }
            case "membres" -> {
                colonnes = ImportService.COLONNES_MEMBRES;
                exemple = EXEMPLE_MEMBRES;
            }
            default -> throw NetworkException.introuvable("MODELE_INEXISTANT", "Modèle « " + objet
                    + " » inconnu : unites ou membres.");
        }
        return switch (f) {
            case "csv" -> new ModeleImport("modele-" + o + ".csv", "text/csv;charset=UTF-8", csv(colonnes, exemple));
            case "xlsx" -> new ModeleImport("modele-" + o + ".xlsx", XLSX, xlsx(o, colonnes, exemple));
            default -> throw NetworkException.regle("FORMAT_INCONNU", "Format « " + format + " » inconnu : csv ou xlsx.");
        };
    }

    /** UTF-8 avec BOM et « ; » : Excel en français l'ouvre directement avec les accents. */
    private static byte[] csv(List<String> colonnes, List<List<String>> lignes) {
        StringBuilder sb = new StringBuilder("\uFEFF").append(String.join(";", colonnes)).append("\r\n");
        lignes.forEach(l -> sb.append(String.join(";", l)).append("\r\n"));
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static byte[] xlsx(String objet, List<String> colonnes, List<List<String>> lignes) {
        try (XSSFWorkbook classeur = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            CellStyle gras = classeur.createCellStyle();
            Font police = classeur.createFont();
            police.setBold(true);
            gras.setFont(police);

            Sheet donnees = classeur.createSheet(objet.equals("unites") ? "Unites" : "Membres");
            ligne(donnees, 0, colonnes, gras);
            for (int i = 0; i < lignes.size(); i++) {
                ligne(donnees, i + 1, lignes.get(i), null);
            }
            for (int c = 0; c < colonnes.size(); c++) {
                donnees.setColumnWidth(c, 24 * 256);
            }
            donnees.createFreezePane(0, 1);

            Sheet aide = classeur.createSheet("Aide");
            int r = 0;
            for (String texte : aide(objet)) {
                aide.createRow(r++).createCell(0).setCellValue(texte);
            }
            if (objet.equals("unites")) {
                r++;
                ligne(aide, r++, List.of("Type", "Libellé", "Se rattache à"), gras);
                for (TypeUnite t : TypeUnite.values()) {
                    String parents = RegleRattachement.estRacine(t) ? "(racine : aucun parent, zone obligatoire)"
                            : RegleRattachement.parentsAutorises(t).stream().sorted().map(TypeUnite::name)
                            .collect(Collectors.joining(", "));
                    ligne(aide, r++, List.of(t.name(), t.libelle(), parents), null);
                }
            }
            aide.setColumnWidth(0, 30 * 256);
            aide.setColumnWidth(1, 22 * 256);
            aide.setColumnWidth(2, 60 * 256);
            classeur.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Génération du modèle impossible", e);
        }
    }

    private static List<String> aide(String objet) {
        if (objet.equals("unites")) {
            return List.of(
                    "Import des unités : la première feuille est lue, la ligne 1 est l'en-tête.",
                    "code : lettres, chiffres, _ ou - (40 max) ; pour un pays, son code ISO sur 2 lettres.",
                    "code_parent : code d'une unité existante ou d'une ligne du fichier (dans n'importe quel ordre).",
                    "zone : obligatoire pour un pays seulement ; ailleurs elle vient du parent.",
                    "Code déjà connu : le libellé est mis à jour et, si code_parent a changé, l'unité est déplacée avec sa branche.",
                    "Tout le fichier passe ou rien ne passe : contrôlez-le d'abord (essai), puis importez.");
        }
        return List.of(
                "Import des membres de regroupements : la première feuille est lue, la ligne 1 est l'en-tête.",
                "Le membre doit être actif, dans la même zone et dans la branche du parent du regroupement.",
                "Un regroupement couvre chaque membre et sa descendance : deux membres sur la même branche sont refusés.",
                "Un membre déjà présent est ignoré. Tout le fichier passe ou rien ne passe.");
    }

    private static void ligne(Sheet feuille, int numero, List<String> valeurs, CellStyle style) {
        Row row = feuille.createRow(numero);
        for (int c = 0; c < valeurs.size(); c++) {
            var cell = row.createCell(c);
            cell.setCellValue(valeurs.get(c));
            if (style != null) {
                cell.setCellStyle(style);
            }
        }
    }
}
