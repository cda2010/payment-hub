/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.mobile.dto;

/**
 *
 * @author cda
 */
public class ChargementTransfertMessageDTO {
    Long chargementId;
    Long operationId;
    Long compteId;

    public ChargementTransfertMessageDTO() {
    }

    public ChargementTransfertMessageDTO(Long chargementId, Long operationId, Long compteId) {
        this.chargementId = chargementId;
        this.operationId = operationId;
        this.compteId = compteId;
    }

    public Long getChargementId() {
        return chargementId;
    }

    public void setChargementId(Long chargementId) {
        this.chargementId = chargementId;
    }

    public Long getOperationId() {
        return operationId;
    }

    public void setOperationId(Long operationId) {
        this.operationId = operationId;
    }

    public Long getCompteId() {
        return compteId;
    }

    public void setCompteId(Long compteId) {
        this.compteId = compteId;
    }
    
}
