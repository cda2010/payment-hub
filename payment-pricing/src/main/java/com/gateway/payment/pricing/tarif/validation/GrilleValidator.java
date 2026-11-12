package com.gateway.payment.pricing.tarif.validation;

import com.gateway.payment.pricing.tarif.domain.GrilleTarifaire;
import com.gateway.payment.pricing.tarif.domain.TrancheTarifaire;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Contrôles de cohérence d'une grille (EF-008, EF-010) : période, présence de tranches, bornes,
 * planchers/plafonds, paramètres de chaque tranche, chevauchements et trous si la continuité est requise.
 * Les bornes sont incluses ; deux tranches sont contiguës si la seconde commence une unité après la fin
 * de la première, l'unité étant la plus petite décimale utilisée par les bornes de la grille.
 */
@Component
public class GrilleValidator {

    /** Échelle des colonnes NUMERIC(18,2) des bornes et des frais. */
    static final int DECIMALES_MONTANT = 2;

    private final ParametreValidator parametres;

    public GrilleValidator(ParametreValidator parametres) {
        this.parametres = parametres;
    }

    public List<ValidationIssue> valider(GrilleTarifaire grille) {
        List<ValidationIssue> erreurs = new ArrayList<>();

        if (grille.getDateFin() != null && grille.getDateDebut() != null
                && grille.getDateFin().isBefore(grille.getDateDebut())) {
            erreurs.add(ValidationIssue.grille("GRILLE_DATES_INVALIDES", "dateFin",
                    "La date de fin précède la date de début."));
        }
        if (grille.getTranches().isEmpty()) {
            erreurs.add(ValidationIssue.grille("GRILLE_SANS_TRANCHE", "tranches",
                    "La grille ne contient aucune tranche."));
            return erreurs;
        }

        List<TrancheTarifaire> bornees = new ArrayList<>();
        for (TrancheTarifaire t : grille.getTranches()) {
            List<ValidationIssue> issues = validerTranche(t);
            erreurs.addAll(issues);
            if (issues.stream().noneMatch(i -> "TRANCHE_BORNES_INVALIDES".equals(i.code()))) {
                bornees.add(t);
            }
        }
        controlerEnchainement(bornees, grille.isContinuiteRequise(), erreurs);
        return erreurs;
    }

    /** Contrôles propres à une tranche, indépendamment des autres : bornes, frais min/max, paramètres. */
    public List<ValidationIssue> validerTranche(TrancheTarifaire t) {
        List<ValidationIssue> erreurs = new ArrayList<>();
        controlerBornes(t, erreurs);
        controlerFrais(t, erreurs);
        erreurs.addAll(parametres.valider(t.getFormule(), t.getParametres(), t.getOrdre()).erreurs());
        return erreurs;
    }

    private void controlerBornes(TrancheTarifaire t, List<ValidationIssue> erreurs) {
        if (t.getMontantMin() == null || t.getMontantMin().signum() < 0) {
            erreurs.add(ValidationIssue.tranche(t.getOrdre(), "TRANCHE_BORNES_INVALIDES", "montantMin",
                    "Le montant minimum est obligatoire et doit être positif ou nul."));
            return;
        }
        if (tropDeDecimales(t.getMontantMin()) || tropDeDecimales(t.getMontantMax())) {
            erreurs.add(ValidationIssue.tranche(t.getOrdre(), "TRANCHE_BORNES_INVALIDES",
                    tropDeDecimales(t.getMontantMin()) ? "montantMin" : "montantMax",
                    "Les bornes acceptent au plus " + DECIMALES_MONTANT + " décimales."));
            return;
        }
        if (t.getMontantMax() != null && t.getMontantMax().compareTo(t.getMontantMin()) < 0) {
            erreurs.add(ValidationIssue.tranche(t.getOrdre(), "TRANCHE_BORNES_INVALIDES", "montantMax",
                    "Le montant maximum (" + texte(t.getMontantMax()) + ") est inférieur au montant minimum ("
                            + texte(t.getMontantMin()) + ")."));
        }
    }

    private void controlerFrais(TrancheTarifaire t, List<ValidationIssue> erreurs) {
        if (tropDeDecimales(t.getFraisMin()) || tropDeDecimales(t.getFraisMax())) {
            erreurs.add(ValidationIssue.tranche(t.getOrdre(), "TRANCHE_FRAIS_INVALIDES",
                    tropDeDecimales(t.getFraisMin()) ? "fraisMin" : "fraisMax",
                    "Les frais minimum et maximum acceptent au plus " + DECIMALES_MONTANT + " décimales."));
        }
        if (t.getFraisMin() != null && t.getFraisMin().signum() < 0) {
            erreurs.add(ValidationIssue.tranche(t.getOrdre(), "TRANCHE_FRAIS_INVALIDES", "fraisMin",
                    "Le frais minimum doit être positif ou nul."));
        }
        if (t.getFraisMax() != null && t.getFraisMax().signum() < 0) {
            erreurs.add(ValidationIssue.tranche(t.getOrdre(), "TRANCHE_FRAIS_INVALIDES", "fraisMax",
                    "Le frais maximum doit être positif ou nul."));
        }
        if (t.getFraisMin() != null && t.getFraisMax() != null && t.getFraisMax().compareTo(t.getFraisMin()) < 0) {
            erreurs.add(ValidationIssue.tranche(t.getOrdre(), "TRANCHE_FRAIS_INVALIDES", "fraisMax",
                    "Le frais maximum est inférieur au frais minimum."));
        }
    }

    private void controlerEnchainement(List<TrancheTarifaire> tranches, boolean continuite, List<ValidationIssue> erreurs) {
        List<TrancheTarifaire> triees = tranches.stream()
                .sorted(Comparator.comparing(TrancheTarifaire::getMontantMin))
                .toList();
        BigDecimal unite = unite(triees);
        for (int i = 1; i < triees.size(); i++) {
            TrancheTarifaire precedente = triees.get(i - 1);
            TrancheTarifaire courante = triees.get(i);
            if (precedente.getMontantMax() == null) {
                erreurs.add(ValidationIssue.tranche(precedente.getOrdre(), "TRANCHE_OUVERTE_NON_DERNIERE", "montantMax",
                        "Seule la dernière tranche peut être ouverte (sans montant maximum) ; elle chevauche la tranche "
                                + courante.getOrdre() + "."));
                continue;
            }
            int comparaison = courante.getMontantMin().compareTo(precedente.getMontantMax());
            if (comparaison <= 0) {
                erreurs.add(ValidationIssue.tranche(courante.getOrdre(), "TRANCHES_CHEVAUCHEMENT", "montantMin",
                        "La tranche " + courante.getOrdre() + " (" + intervalle(courante) + ") chevauche la tranche "
                                + precedente.getOrdre() + " (" + intervalle(precedente) + ")."));
            } else if (continuite && courante.getMontantMin().compareTo(precedente.getMontantMax().add(unite)) > 0) {
                erreurs.add(ValidationIssue.tranche(courante.getOrdre(), "TRANCHES_DISCONTINUES", "montantMin",
                        "Trou entre la tranche " + precedente.getOrdre() + " (fin " + texte(precedente.getMontantMax())
                                + ") et la tranche " + courante.getOrdre() + " (début " + texte(courante.getMontantMin())
                                + ") : les montants intermédiaires ne sont couverts par aucune tranche."));
            }
        }
    }

    /** 1 si toutes les bornes sont entières, 0,01 si au moins une borne a deux décimales, etc. */
    static BigDecimal unite(List<TrancheTarifaire> tranches) {
        int echelle = tranches.stream()
                .flatMap(t -> Stream.of(t.getMontantMin(), t.getMontantMax()))
                .filter(Objects::nonNull)
                .mapToInt(v -> Math.max(0, v.stripTrailingZeros().scale()))
                .max()
                .orElse(0);
        return BigDecimal.ONE.movePointLeft(echelle);
    }

    private static boolean tropDeDecimales(BigDecimal v) {
        return v != null && v.stripTrailingZeros().scale() > DECIMALES_MONTANT;
    }

    private static String intervalle(TrancheTarifaire t) {
        return texte(t.getMontantMin()) + " – " + (t.getMontantMax() == null ? "∞" : texte(t.getMontantMax()));
    }

    private static String texte(BigDecimal v) {
        return v.signum() == 0 ? "0" : v.stripTrailingZeros().toPlainString();
    }
}
