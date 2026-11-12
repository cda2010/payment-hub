package com.gateway.payment.commission.formule;

import java.math.BigDecimal;
import java.util.Map;

/** Implémentation approuvée d'une formule de commission (code + version). */
public interface CommissionFormula {

    String code();

    int version();

    ResultatFormule calculer(BigDecimal base, Map<String, BigDecimal> params);
}
