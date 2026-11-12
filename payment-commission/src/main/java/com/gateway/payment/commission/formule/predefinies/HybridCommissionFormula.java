package com.gateway.payment.commission.formule.predefinies;

import com.gateway.payment.commission.formule.CommissionFormula;
import com.gateway.payment.commission.formule.ResultatFormule;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Component
public class HybridCommissionFormula implements CommissionFormula {

    @Override
    public String code() {
        return "HYBRID";
    }

    @Override
    public int version() {
        return 1;
    }

    @Override
    public ResultatFormule calculer(BigDecimal base, Map<String, BigDecimal> params) {
        BigDecimal taux = require(params, "taux");
        BigDecimal fixe = params.getOrDefault("montantFixe", BigDecimal.ZERO);
        BigDecimal c = base.multiply(taux).add(fixe).setScale(4, RoundingMode.HALF_UP);
        return new ResultatFormule(base, c, "HYBRID base×" + taux + "+" + fixe);
    }

    private static BigDecimal require(Map<String, BigDecimal> params, String key) {
        BigDecimal v = params.get(key);
        if (v == null) {
            throw new IllegalArgumentException("Paramètre requis : " + key);
        }
        return v;
    }
}
