package com.gateway.payment.pricing.tarif.formule.predefinies;

import com.gateway.payment.pricing.tarif.formule.FeeFormula;
import com.gateway.payment.pricing.tarif.formule.ParametreAttendu;
import com.gateway.payment.pricing.tarif.formule.ParametresValides;
import com.gateway.payment.pricing.tarif.formule.ResultatFormule;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

import static com.gateway.payment.pricing.tarif.formule.predefinies.Montants.texte;

/** F = montant × taux + fraisFixes */
@Component
public class AxPlusYFormula implements FeeFormula {

    @Override
    public String code() {
        return "AX_PLUS_Y";
    }

    @Override
    public int version() {
        return 1;
    }

    @Override
    public List<ParametreAttendu> parametres() {
        return List.of(
                ParametreAttendu.decimalObligatoire("taux"),
                ParametreAttendu.decimalObligatoire("fraisFixes"));
    }

    @Override
    public ResultatFormule calculer(BigDecimal montant, ParametresValides p) {
        BigDecimal taux = p.decimal("taux");
        BigDecimal fixes = p.decimal("fraisFixes");
        BigDecimal frais = montant.multiply(taux).add(fixes);
        return new ResultatFormule(montant, frais, texte(montant) + " * " + texte(taux) + " + " + texte(fixes));
    }
}
