package com.gateway.payment.network.service;

import com.gateway.payment.network.NetworkException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Contenu tabulaire d'un fichier d'import : CSV (séparateur « ; », « , » ou tabulation, détecté sur l'en-tête ;
 * UTF-8, ou Windows-1252 comme l'enregistre Excel en français) ou classeur Excel (.xlsx, .xls : première feuille).
 * La première ligne non vide est l'en-tête ; les noms de colonnes sont comparés sans casse, accents ni espaces.
 */
record TableauImport(String format, List<Ligne> lignes) {

    static final int MAX_LIGNES = 20_000;

    /** @param numero numéro de la ligne dans le fichier, l'en-tête compris (première ligne = 1) */
    record Ligne(int numero, Map<String, String> valeurs) {

        String valeur(String colonne) {
            String v = valeurs.get(colonne);
            return v == null ? "" : v.trim();
        }
    }

    static TableauImport lire(String nomFichier, byte[] contenu, List<String> colonnesObligatoires,
                              Map<String, String> alias) {
        if (contenu == null || contenu.length == 0) {
            throw invalide("FICHIER_VIDE", "Le fichier est vide.");
        }
        boolean classeur = estClasseur(nomFichier, contenu);
        List<List<String>> brutes = classeur ? lireClasseur(contenu) : lireCsv(contenu);
        int entete = 0;
        while (entete < brutes.size() && estVide(brutes.get(entete))) {
            entete++;
        }
        if (entete == brutes.size()) {
            throw invalide("FICHIER_VIDE", "Le fichier ne contient aucune ligne.");
        }
        List<String> colonnes = brutes.get(entete).stream().map(c -> colonne(c, alias)).toList();
        List<String> manquantes = colonnesObligatoires.stream().filter(c -> !colonnes.contains(c)).toList();
        if (!manquantes.isEmpty()) {
            throw invalide("COLONNES_MANQUANTES", "Colonnes absentes de l'en-tête : " + String.join(", ", manquantes)
                    + ". Attendu : " + String.join(" ; ", colonnesObligatoires) + " (voir le modèle).");
        }
        List<Ligne> lignes = new ArrayList<>();
        for (int i = entete + 1; i < brutes.size(); i++) {
            List<String> cellules = brutes.get(i);
            if (estVide(cellules)) {
                continue;
            }
            if (lignes.size() == MAX_LIGNES) {
                throw invalide("TROP_DE_LIGNES", "Un import est limité à " + MAX_LIGNES
                        + " lignes : découpez le fichier.");
            }
            Map<String, String> valeurs = new HashMap<>();
            for (int c = 0; c < colonnes.size() && c < cellules.size(); c++) {
                valeurs.putIfAbsent(colonnes.get(c), cellules.get(c));
            }
            lignes.add(new Ligne(i + 1, valeurs));
        }
        return new TableauImport(classeur ? "XLSX" : "CSV", lignes);
    }

    /** « Code parent », « code-parent », « CODE_PARENT » → code_parent ; puis les alias (« parent » → code_parent). */
    static String colonne(String entete, Map<String, String> alias) {
        String c = sansAccents(entete == null ? "" : entete).trim().toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
        return alias.getOrDefault(c, c);
    }

    static String sansAccents(String s) {
        return Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    }

    // ── CSV ─────────────────────────────────────────────────────────────────

    private static List<List<String>> lireCsv(byte[] contenu) {
        String texte = decoder(contenu);
        if (texte.startsWith("\uFEFF")) {
            texte = texte.substring(1);
        }
        char separateur = separateur(texte);
        List<List<String>> lignes = new ArrayList<>();
        List<String> ligne = new ArrayList<>();
        StringBuilder cellule = new StringBuilder();
        boolean guillemets = false;
        for (int i = 0; i < texte.length(); i++) {
            char ch = texte.charAt(i);
            if (guillemets) {
                if (ch == '"') {
                    if (i + 1 < texte.length() && texte.charAt(i + 1) == '"') {
                        cellule.append('"');
                        i++;
                    } else {
                        guillemets = false;
                    }
                } else {
                    cellule.append(ch);
                }
            } else if (ch == '"' && cellule.isEmpty()) {
                guillemets = true;
            } else if (ch == separateur) {
                ligne.add(cellule.toString());
                cellule.setLength(0);
            } else if (ch == '\n' || ch == '\r') {
                if (ch == '\r' && i + 1 < texte.length() && texte.charAt(i + 1) == '\n') {
                    i++;
                }
                ligne.add(cellule.toString());
                cellule.setLength(0);
                lignes.add(ligne);
                ligne = new ArrayList<>();
                verifierTaille(lignes.size());
            } else {
                cellule.append(ch);
            }
        }
        if (!cellule.isEmpty() || !ligne.isEmpty()) {
            ligne.add(cellule.toString());
            lignes.add(ligne);
        }
        return lignes;
    }

    /** UTF-8 s'il est valide, sinon Windows-1252 (CSV enregistré par Excel en français). */
    private static String decoder(byte[] contenu) {
        try {
            return StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(contenu))
                    .toString();
        } catch (CharacterCodingException e) {
            return new String(contenu, Charset.forName("windows-1252"));
        }
    }

    /** Le plus fréquent de « ; », tabulation et « , » sur la première ligne. */
    private static char separateur(String texte) {
        int fin = texte.indexOf('\n');
        String premiere = fin < 0 ? texte : texte.substring(0, fin);
        Map<Character, Long> nombres = new LinkedHashMap<>();
        for (char c : new char[]{';', '\t', ','}) {
            nombres.put(c, premiere.chars().filter(x -> x == c).count());
        }
        return nombres.entrySet().stream().max(Map.Entry.comparingByValue())
                .filter(e -> e.getValue() > 0).map(Map.Entry::getKey).orElse(';');
    }

    // ── Excel ───────────────────────────────────────────────────────────────

    private static boolean estClasseur(String nom, byte[] contenu) {
        String n = nom == null ? "" : nom.toLowerCase(Locale.ROOT);
        boolean zip = contenu.length > 3 && contenu[0] == 'P' && contenu[1] == 'K';
        boolean ole = contenu.length > 7 && (contenu[0] & 0xFF) == 0xD0 && (contenu[1] & 0xFF) == 0xCF;
        return n.endsWith(".xlsx") || n.endsWith(".xls") || zip || ole;
    }

    private static List<List<String>> lireClasseur(byte[] contenu) {
        try (Workbook classeur = WorkbookFactory.create(new ByteArrayInputStream(contenu))) {
            if (classeur.getNumberOfSheets() == 0) {
                throw invalide("FICHIER_VIDE", "Le classeur ne contient aucune feuille.");
            }
            Sheet feuille = classeur.getSheetAt(0);
            DataFormatter format = new DataFormatter(Locale.FRANCE);
            List<List<String>> lignes = new ArrayList<>();
            for (int r = 0; r <= feuille.getLastRowNum(); r++) {
                verifierTaille(r);
                Row row = feuille.getRow(r);
                List<String> cellules = new ArrayList<>();
                if (row != null) {
                    for (int c = 0; c < Math.max(row.getLastCellNum(), 0); c++) {
                        Cell cell = row.getCell(c);
                        cellules.add(cell == null ? "" : format.formatCellValue(cell));
                    }
                }
                lignes.add(cellules);
            }
            return lignes;
        } catch (IOException | RuntimeException e) {
            if (e instanceof NetworkException ne) {
                throw ne;
            }
            throw invalide("FICHIER_ILLISIBLE", "Classeur Excel illisible : enregistrez-le au format .xlsx ou en CSV.");
        }
    }

    private static void verifierTaille(int lignes) {
        if (lignes > MAX_LIGNES + 1000) {
            throw invalide("TROP_DE_LIGNES", "Un import est limité à " + MAX_LIGNES + " lignes : découpez le fichier.");
        }
    }

    private static boolean estVide(List<String> cellules) {
        return cellules.stream().allMatch(c -> c == null || c.isBlank());
    }

    private static NetworkException invalide(String code, String message) {
        return NetworkException.regle(code, message);
    }
}
