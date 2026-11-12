/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.m2i.pti.batch.reader;

/**
 *
 * @author USER_M2I
 */
public class LigneChargementTransDTO {
    Long idLigneChargement;
Long idOperation;
Long compteId;

    public LigneChargementTransDTO() {
    }

    public LigneChargementTransDTO(Long idLigneChargement, Long idOperation, Long compteId) {
        this.idLigneChargement = idLigneChargement;
        this.idOperation = idOperation;
        this.compteId = compteId;
    }

    public Long getIdLigneChargement() {
        return idLigneChargement;
    }

    public void setIdLigneChargement(Long idLigneChargement) {
        this.idLigneChargement = idLigneChargement;
    }

    public void setIdOperation(Long idOperation) {
        this.idOperation = idOperation;
    }

    public Long getIdOperation() {
        return idOperation;
    }

    

    public Long getCompteId() {
        return compteId;
    }

    public void setCompteId(Long compteId) {
        this.compteId = compteId;
    }

    @Override
    public String toString() {
        return "LigneChargementTransDTO{" + "idLigneChargement=" + idLigneChargement + ", idOperation=" + idOperation + ", compteId=" + compteId + '}';
    }
    
}
