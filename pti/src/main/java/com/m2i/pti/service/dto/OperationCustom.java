package com.m2i.pti.service.dto;

import java.math.BigDecimal;

public class OperationCustom {
    BigDecimal montantOp;

    public OperationCustom(BigDecimal montantOp) {
        this.montantOp = montantOp;
    }

    public BigDecimal getMontantOp() {
        return montantOp;
    }

    public void setMontantOp(BigDecimal montantOp) {
        this.montantOp = montantOp;
    }
}
