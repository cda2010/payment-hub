package com.m2i.pti.batch.reader;

import java.time.Instant;

public class LigneReglementTransDTO {
    public Long id;
    public Double montantCommission;
    public Double soldeCompteOp;
    public Boolean valide;
    public Long reglementId;
    public String libelleUO;
    public Long operationId;
    public String createdBy;
    public Instant createdDate;
    public String lastModifiedBy;
    public Instant lastModifiedDate;
    public Boolean reglementStatut;
    public Double soldeCompteOpApres;
    public Boolean estDetacher;


    public LigneReglementTransDTO(Long id, Double montantCommission, Double soldeCompteOp, Boolean valide, Long reglementId, Long operationId, Boolean reglementStatut, Double soldeCompteOpApres, Boolean estDetacher) {
        this.id = id;
        this.montantCommission = montantCommission;
        this.soldeCompteOp = soldeCompteOp;
        this.valide = valide;
        this.reglementId = reglementId;
        this.operationId = operationId;
        this.reglementStatut = reglementStatut;
        this.soldeCompteOpApres = soldeCompteOpApres;
        this.estDetacher = estDetacher;
    }
    public  LigneReglementTransDTO(){

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getMontantCommission() {
        return montantCommission;
    }

    public void setMontantCommission(Double montantCommission) {
        this.montantCommission = montantCommission;
    }

    public Double getSoldeCompteOp() {
        return soldeCompteOp;
    }

    public void setSoldeCompteOp(Double soldeCompteOp) {
        this.soldeCompteOp = soldeCompteOp;
    }

    public Boolean getValide() {
        return valide;
    }

    public void setValide(Boolean valide) {
        this.valide = valide;
    }

    public Long getReglementId() {
        return reglementId;
    }

    public void setReglementId(Long reglementId) {
        this.reglementId = reglementId;
    }

    public String getLibelleUO() {
        return libelleUO;
    }

    public void setLibelleUO(String libelleUO) {
        this.libelleUO = libelleUO;
    }

    public Long getOperationId() {
        return operationId;
    }

    public void setOperationId(Long operationId) {
        this.operationId = operationId;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public Instant getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Instant lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public Boolean getReglementStatut() {
        return reglementStatut;
    }

    public void setReglementStatut(Boolean reglementStatut) {
        this.reglementStatut = reglementStatut;
    }

    public Double getSoldeCompteOpApres() {
        return soldeCompteOpApres;
    }

    public void setSoldeCompteOpApres(Double soldeCompteOpApres) {
        this.soldeCompteOpApres = soldeCompteOpApres;
    }

    public Boolean getEstDetacher() {
        return estDetacher;
    }

    public void setEstDetacher(Boolean estDetacher) {
        this.estDetacher = estDetacher;
    }
}
