package com.gateway.payment.pricing.tarif;

import com.gateway.payment.pricing.tarif.domain.Formule;
import com.gateway.payment.pricing.tarif.domain.FormuleParametre;
import com.gateway.payment.pricing.tarif.domain.GrilleTarifaire;
import com.gateway.payment.pricing.tarif.domain.StatutFormule;
import com.gateway.payment.pricing.tarif.domain.StatutGrille;
import com.gateway.payment.pricing.tarif.domain.TrancheTarifaire;
import com.gateway.payment.pricing.tarif.domain.TypeDonnee;
import com.gateway.payment.pricing.tarif.domain.TypeFormule;
import com.gateway.payment.pricing.tarif.formule.FormulaRegistry;
import com.gateway.payment.pricing.tarif.formule.predefinies.AxPlusYFormula;
import com.gateway.payment.pricing.tarif.formule.predefinies.ExcessAxPlusYFormula;
import com.gateway.payment.pricing.tarif.formule.predefinies.ExcessCappedFormula;
import com.gateway.payment.pricing.tarif.formule.predefinies.FixedFormula;
import com.gateway.payment.pricing.tarif.formule.predefinies.PercentTotalFormula;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Catalogue et grille de démonstration identiques au seed de V2__moteur_tarification.sql. */
public final class JeuDeDonnees {

    private JeuDeDonnees() {
    }

    public static FormulaRegistry registry() {
        return new FormulaRegistry(List.of(new FixedFormula(), new PercentTotalFormula(), new AxPlusYFormula(),
                new ExcessAxPlusYFormula(), new ExcessCappedFormula()));
    }

    public static Formule fixed() {
        return formule("FIXED", montant("montantFixe", true, null));
    }

    public static Formule percentTotal() {
        return formule("PERCENT_TOTAL", taux(), montant("fraisFixes", false, "0"));
    }

    public static Formule axPlusY() {
        return formule("AX_PLUS_Y", taux(), montant("fraisFixes", true, null));
    }

    public static Formule excess() {
        return formule("EXCESS_AX_PLUS_Y", montant("seuil", true, null), taux(), montant("fraisFixes", false, "0"));
    }

    public static Formule excessCapped() {
        return formule("EXCESS_CAPPED", montant("seuil", true, null), taux(), montant("fraisFixes", true, null),
                montant("plafond", true, null));
    }

    public static List<Formule> toutes() {
        return List.of(fixed(), percentTotal(), axPlusY(), excess(), excessCapped());
    }

    /** TRF_STD : TRANSFERT / MOBILE_MONEY / XOF, 4 tranches de 0 à 500 000. */
    public static GrilleTarifaire trfStd() {
        GrilleTarifaire g = grille("TRF_STD", "TRANSFERT", "MOBILE_MONEY", "XOF");
        g.ajouterTranche(tranche(1, "0", "8000", fixed(), Map.of("montantFixe", BigDecimal.ZERO)));
        g.ajouterTranche(tranche(2, "8001", "25000", excess(),
                Map.of("seuil", 8000, "taux", new BigDecimal("0.015"), "fraisFixes", 100)));
        g.ajouterTranche(tranche(3, "25001", "100000", excess(),
                Map.of("seuil", 25000, "taux", new BigDecimal("0.01"), "fraisFixes", 200)));
        g.ajouterTranche(tranche(4, "100001", "500000", excessCapped(),
                Map.of("seuil", 100000, "taux", new BigDecimal("0.005"), "fraisFixes", 500, "plafond", 2000)));
        return g;
    }

    public static GrilleTarifaire grille(String code, String produit, String canal, String devise) {
        return GrilleTarifaire.builder()
                .id(UUID.randomUUID())
                .code(code)
                .libelle(code)
                .produit(produit)
                .canal(canal)
                .devise(devise)
                .dateDebut(LocalDate.of(2026, 1, 1))
                .version(1)
                .statut(StatutGrille.PUBLIEE)
                .continuiteRequise(true)
                .arrondiEchelle(0)
                .arrondiMode(RoundingMode.HALF_UP)
                .createdBy("test")
                .updatedBy("test")
                .build();
    }

    public static TrancheTarifaire tranche(int ordre, String min, String max, Formule formule, Map<String, Object> parametres) {
        return TrancheTarifaire.builder()
                .id(UUID.randomUUID())
                .ordre(ordre)
                .montantMin(new BigDecimal(min))
                .montantMax(max == null ? null : new BigDecimal(max))
                .formule(formule)
                .parametres(new LinkedHashMap<>(parametres))
                .build();
    }

    private static Formule formule(String code, FormuleParametre... parametres) {
        Formule f = Formule.builder()
                .id(UUID.randomUUID())
                .code(code)
                .libelle(code)
                .version(1)
                .type(TypeFormule.PREDEFINIE)
                .statut(StatutFormule.ACTIVE)
                .build();
        for (int i = 0; i < parametres.length; i++) {
            parametres[i].setFormule(f);
            parametres[i].setOrdre(i + 1);
            f.getParametres().add(parametres[i]);
        }
        return f;
    }

    private static FormuleParametre montant(String code, boolean obligatoire, String defaut) {
        return FormuleParametre.builder()
                .code(code)
                .libelle(libelle(code))
                .typeDonnee(TypeDonnee.DECIMAL)
                .obligatoire(obligatoire)
                .valeurDefaut(defaut)
                .valeurMin(BigDecimal.ZERO)
                .nbDecimales(2)
                .unite("MONTANT")
                .build();
    }

    private static FormuleParametre taux() {
        return FormuleParametre.builder()
                .code("taux")
                .libelle("Taux")
                .typeDonnee(TypeDonnee.DECIMAL)
                .obligatoire(true)
                .valeurMin(BigDecimal.ZERO)
                .valeurMax(BigDecimal.ONE)
                .nbDecimales(6)
                .unite("TAUX")
                .build();
    }

    private static String libelle(String code) {
        return switch (code) {
            case "montantFixe" -> "Montant fixe";
            case "fraisFixes" -> "Frais fixes";
            case "seuil" -> "Seuil";
            case "plafond" -> "Plafond";
            default -> code;
        };
    }
}
