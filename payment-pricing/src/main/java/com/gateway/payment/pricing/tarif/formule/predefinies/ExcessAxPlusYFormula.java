package com.gateway.payment.pricing.tarif.formule.predefinies;

import com.gateway.payment.pricing.tarif.formule.FeeFormula;
import com.gateway.payment.pricing.tarif.formule.ParametreAttendu;
import com.gateway.payment.pricing.tarif.formule.ParametresValides;
import com.gateway.payment.pricing.tarif.formule.ResultatFormule;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

import static com.gateway.payment.pricing.tarif.formule.predefinies.Montants.texte;

/** Si montant ≤ seuil : F = 0 ; sinon F = (montant − seuil) × taux + fraisFixes. */
@Component
public class ExcessAxPlusYFormula implements FeeFormula {

    @Override
    public String code() {
        return "EXCESS_AX_PLUS_Y";
    }

    @Override
    public int version() {
        return 1;
    }

    @Override
    public List<ParametreAttendu> parametres() {
        return List.of(
                ParametreAttendu.decimalObligatoire("seuil"),
                ParametreAttendu.decimalObligatoire("taux"),
                ParametreAttendu.decimalFacultatif("fraisFixes"));
    }

    @Override
    public ResultatFormule calculer(BigDecimal montant, ParametresValides p) {
        BigDecimal seuil = p.decimal("seuil");
        BigDecimal taux = p.decimal("taux");
        BigDecimal fixes = p.decimalOuZero("fraisFixes");
        if (montant.compareTo(seuil) <= 0) {
            return new ResultatFormule(BigDecimal.ZERO, BigDecimal.ZERO,
                    texte(montant) + " <= seuil " + texte(seuil) + " : 0");
        }
        BigDecimal base = montant.subtract(seuil);
        BigDecimal frais = base.multiply(taux).add(fixes);
        String detail = "(" + texte(montant) + " - " + texte(seuil) + ") * " + texte(taux)
                + (fixes.signum() == 0 ? "" : " + " + texte(fixes));
        return new ResultatFormule(base, frais, detail);
    }
}
