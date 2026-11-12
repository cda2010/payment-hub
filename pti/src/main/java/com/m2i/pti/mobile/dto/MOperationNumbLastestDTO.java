/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.mobile.dto;

import java.math.BigDecimal;

/**
 *
 * @author cda
 */
public class MOperationNumbLastestDTO {

    private String codeOperation;
    private BigDecimal montantOperation;
    private String description;

    public String getCodeOperation() {
        return codeOperation;
    }

    public void setCodeOperation(String codeOperation) {
        this.codeOperation = codeOperation;
    }

    public BigDecimal getMontantOperation() {
        return montantOperation;
    }

    public void setMontantOperation(BigDecimal montantOperation) {
        this.montantOperation = montantOperation;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public MOperationNumbLastestDTO(String codeOperation, BigDecimal montantOperation, String description) {
        this.codeOperation = codeOperation;
        this.montantOperation = montantOperation;
        this.description = description;
    }

}
