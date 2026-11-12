package com.gateway.payment.commission.formule.predefinies;

import com.gateway.payment.commission.formule.CommissionFormula;
import com.gateway.payment.commission.formule.ResultatFormule;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class FixedCommissionFormula implements CommissionFormula {

    @Override
    public String code() {
        return "FIXED";
    }

    @Override
    public int version() {
        return 1;
    }

    @Override
    public ResultatFormule calculer(BigDecimal base, Map<String, BigDecimal> params) {
        BigDecimal fixe = require(params, "montantFixe");
        return new ResultatFormule(base, fixe, "FIXED montantFixe=" + fixe);
    }

    private static BigDecimal require(Map<String, BigDecimal> params, String key) {
        BigDecimal v = params.get(key);
        if (v == null) {
            throw new IllegalArgumentException("Paramètre requis : " + key);
        }
        return v;
    }
}
