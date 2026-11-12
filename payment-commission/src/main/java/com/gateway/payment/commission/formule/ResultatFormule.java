package com.gateway.payment.commission.formule;

import java.math.BigDecimal;

public record ResultatFormule(BigDecimal base, BigDecimal commission, String detail) {}
