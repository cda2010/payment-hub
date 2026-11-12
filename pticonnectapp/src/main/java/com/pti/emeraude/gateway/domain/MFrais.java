package com.pti.emeraude.gateway.domain;

import java.math.BigDecimal;

public class MFrais extends StandardResponse {

    private BigDecimal fraisOperation;
    private BigDecimal taxeOPeration;
    private String responsable;

    public BigDecimal getFraisOperation() {
        return fraisOperation;
    }

    public void setFraisOperation(BigDecimal fraisOperation) {
        this.fraisOperation = fraisOperation;
    }

    public BigDecimal getTaxeOPeration() {
        return taxeOPeration;
    }

    public void setTaxeOPeration(BigDecimal taxeOPeration) {
        this.taxeOPeration = taxeOPeration;
    }

    public String getResponsable() {
        return responsable;
    }

    public void setResponsable(String responsable) {
        this.responsable = responsable;
    }

}
