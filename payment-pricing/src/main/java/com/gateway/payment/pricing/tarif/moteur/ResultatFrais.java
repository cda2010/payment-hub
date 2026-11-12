package com.gateway.payment.pricing.tarif.moteur;

import com.gateway.payment.pricing.tarif.domain.BaseCalculFrais;
import com.gateway.payment.pricing.tarif.domain.CibleReseau;
import com.gateway.payment.pricing.tarif.domain.PeriodeCumul;
import com.gateway.payment.pricing.tarif.domain.ScopeCumul;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

/**
 * Résultat d'un calcul ou d'une simulation : tout ce qu'il faut pour rattacher les frais à la version
 * de grille et de formule réellement utilisée (ENF-007) et pour expliquer le calcul (EF-009).
 *
 * @param montant           assiette utilisée (pas forcément le principal de l'opération)
 * @param fraisFormule      résultat brut de la formule
 * @param fraisAvantArrondi après plancher/plafond de la tranche, avant arrondi
 * @param ajustement        PLANCHER ou PLAFOND si les frais de la tranche ont été bornés, sinon vide
 * @param source            pourquoi la grille vise la source de l'opération (calcul seulement)
 * @param destination       idem pour la destination
 * @param baseCalcul        assiette déclarée sur la grille
 */
public record ResultatFrais(
        BigDecimal montant,
        String devise,
        UUID grilleId,
        String grilleCode,
        int versionGrille,
        TrancheRetenue tranche,
        String formuleCode,
        int versionFormule,
        Map<String, Object> parametres,
        BigDecimal baseTaxable,
        BigDecimal fraisFormule,
        String ajustement,
        BigDecimal fraisAvantArrondi,
        Arrondi arrondi,
        BigDecimal frais,
        String details,
        LocalDate dateCalcul,
        Correspondance source,
        Correspondance destination,
        BaseCalculFrais baseCalcul,
        PeriodeCumul cumulPeriod,
        ScopeCumul cumulScope
) {

    public ResultatFrais(BigDecimal montant, String devise, UUID grilleId, String grilleCode, int versionGrille,
                         TrancheRetenue tranche, String formuleCode, int versionFormule, Map<String, Object> parametres,
                         BigDecimal baseTaxable, BigDecimal fraisFormule, String ajustement, BigDecimal fraisAvantArrondi,
                         Arrondi arrondi, BigDecimal frais, String details, LocalDate dateCalcul) {
        this(montant, devise, grilleId, grilleCode, versionGrille, tranche, formuleCode, versionFormule, parametres,
                baseTaxable, fraisFormule, ajustement, fraisAvantArrondi, arrondi, frais, details, dateCalcul,
                null, null, null, null, null);
    }

    public ResultatFrais avecCorrespondances(Correspondance source, Correspondance destination) {
        return new ResultatFrais(montant, devise, grilleId, grilleCode, versionGrille, tranche, formuleCode,
                versionFormule, parametres, baseTaxable, fraisFormule, ajustement, fraisAvantArrondi, arrondi, frais,
                details, dateCalcul, source, destination, baseCalcul, cumulPeriod, cumulScope);
    }

    public ResultatFrais avecAssiette(BaseCalculFrais base, PeriodeCumul period, ScopeCumul scope) {
        return new ResultatFrais(montant, devise, grilleId, grilleCode, versionGrille, tranche, formuleCode,
                versionFormule, parametres, baseTaxable, fraisFormule, ajustement, fraisAvantArrondi, arrondi, frais,
                details, dateCalcul, source, destination, base, period, scope);
    }

    public record TrancheRetenue(int ordre, BigDecimal montantMin, BigDecimal montantMax,
                                 BigDecimal fraisMin, BigDecimal fraisMax) {
    }

    public record Arrondi(int echelle, RoundingMode mode) {
    }

    /**
     * Ce que la grille vise d'un côté et par où l'opération y correspond.
     *
     * @param cible code de la zone ou de l'unité visée ; vide pour « toutes »
     * @param via   membre par lequel un regroupement visé couvre l'opération ; vide sinon
     */
    public record Correspondance(CibleReseau.Niveau niveau, String cible, String via) {

        public static final Correspondance TOUTES = new Correspondance(CibleReseau.Niveau.TOUTES, null, null);
    }
}
