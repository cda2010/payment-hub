package com.gateway.payment.pricing.tarif.formule.predefinies;

import com.gateway.payment.pricing.tarif.formule.FeeFormula;
import com.gateway.payment.pricing.tarif.formule.ParametreAttendu;
import com.gateway.payment.pricing.tarif.formule.ParametresValides;
import com.gateway.payment.pricing.tarif.formule.ResultatFormule;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

import static com.gateway.payment.pricing.tarif.formule.predefinies.Montants.texte;

/** F = montantFixe */
@Component
public class FixedFormula implements FeeFormula {

    @Override
    public String code() {
        return "FIXED";
    }

    @Override
    public int version() {
        return 1;
    }

    @Override
    public List<ParametreAttendu> parametres() {
        return List.of(ParametreAttendu.decimalObligatoire("montantFixe"));
    }

    @Override
    public ResultatFormule calculer(BigDecimal montant, ParametresValides p) {
        BigDecimal frais = p.decimal("montantFixe");
        return new ResultatFormule(montant, frais, texte(frais));
    }
}
