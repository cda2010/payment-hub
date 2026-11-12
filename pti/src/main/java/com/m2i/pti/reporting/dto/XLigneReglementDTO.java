package com.m2i.pti.reporting.dto;

public class XLigneReglementDTO {
    public String organisation;
    public Double montantCommission;
    public Double soldeCptCommission;
    public Double soldeCompteOp;
    public Double soldeCompteOpApres;
    public String erreur;


    public String getOrganisation() {
        return organisation;
    }

    public void setOrganisation(String organisation) {
        this.organisation = organisation;
    }

    public Double getMontantCommission() {
        return montantCommission;
    }

    public void setMontantCommission(Double montantCommission) {
        this.montantCommission = montantCommission;
    }

    public Double getSoldeCptCommission() {
        return soldeCptCommission;
    }

    public void setSoldeCptCommission(Double soldeCptCommission) {
        this.soldeCptCommission = soldeCptCommission;
    }

    public Double getSoldeCompteOp() {
        return soldeCompteOp;
    }

    public void setSoldeCompteOp(Double soldeCompteOp) {
        this.soldeCompteOp = soldeCompteOp;
    }

    public String getErreur() {
        return erreur;
    }

    public void setErreur(String erreur) {
        this.erreur = erreur;
    }

    public Double getSoldeCompteOpApres() {
        return soldeCompteOpApres;
    }

    public void setSoldeCompteOpApres(Double soldeCompteOpApres) {
        this.soldeCompteOpApres = soldeCompteOpApres;
    }
}
