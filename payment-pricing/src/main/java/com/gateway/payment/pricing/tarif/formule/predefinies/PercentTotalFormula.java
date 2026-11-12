package com.gateway.payment.pricing.tarif.formule.predefinies;

import com.gateway.payment.pricing.tarif.formule.FeeFormula;
import com.gateway.payment.pricing.tarif.formule.ParametreAttendu;
import com.gateway.payment.pricing.tarif.formule.ParametresValides;
import com.gateway.payment.pricing.tarif.formule.ResultatFormule;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

import static com.gateway.payment.pricing.tarif.formule.predefinies.Montants.texte;

/** F = montant × taux + fraisFixes (fraisFixes facultatifs, 0 par défaut). */
@Component
public class PercentTotalFormula implements FeeFormula {

    @Override
    public String code() {
        return "PERCENT_TOTAL";
    }

    @Override
    public int version() {
        return 1;
    }

    @Override
    public List<ParametreAttendu> parametres() {
        return List.of(
                ParametreAttendu.decimalObligatoire("taux"),
                ParametreAttendu.decimalFacultatif("fraisFixes"));
    }

    @Override
    public ResultatFormule calculer(BigDecimal montant, ParametresValides p) {
        BigDecimal taux = p.decimal("taux");
        BigDecimal fixes = p.decimalOuZero("fraisFixes");
        BigDecimal frais = montant.multiply(taux).add(fixes);
        String detail = texte(montant) + " * " + texte(taux)
                + (fixes.signum() == 0 ? "" : " + " + texte(fixes));
        return new ResultatFormule(montant, frais, detail);
    }
}
