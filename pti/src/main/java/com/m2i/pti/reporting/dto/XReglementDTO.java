package com.m2i.pti.reporting.dto;


import java.util.List;

public class XReglementDTO {
    public List<XLigneReglementDTO> ligneReglementValide;
    public List<XLigneReglementDTO> ligneReglementNonValide;
    public Integer nbrOrganisation;
    public Double montantTotal;
    public String createBy;
    public String lastModifiedBy;
    public String logo;
    public Double totalRegle;
    public Double totalNonRegle;

    public List<XLigneReglementDTO> getLigneReglementValide() {
        return ligneReglementValide;
    }

    public void setLigneReglementValide(List<XLigneReglementDTO> ligneReglementValide) {
        this.ligneReglementValide = ligneReglementValide;
    }

    public List<XLigneReglementDTO> getLigneReglementNonValide() {
        return ligneReglementNonValide;
    }

    public void setLigneReglementNonValide(List<XLigneReglementDTO> ligneReglementNonValide) {
        this.ligneReglementNonValide = ligneReglementNonValide;
    }

    public Integer getNbrOrganisation() {
        return nbrOrganisation;
    }

    public void setNbrOrganisation(Integer nbrOrganisation) {
        this.nbrOrganisation = nbrOrganisation;
    }

    public Double getMontantTotal() {
        return montantTotal;
    }

    public void setMontantTotal(Double montantTotal) {
        this.montantTotal = montantTotal;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public Double getTotalRegle() {
        return totalRegle;
    }

    public void setTotalRegle(Double totalRegle) {
        this.totalRegle = totalRegle;
    }

    public Double getTotalNonRegle() {
        return totalNonRegle;
    }

    public void setTotalNonRegle(Double totalNonRegle) {
        this.totalNonRegle = totalNonRegle;
    }
}
