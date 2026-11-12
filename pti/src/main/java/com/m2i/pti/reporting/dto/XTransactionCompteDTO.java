package com.m2i.pti.reporting.dto;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public class XTransactionCompteDTO {

    public String type;
    public BigDecimal montant;
    public String codeOperation;
    public BigDecimal soldeCourant;
    public BigDecimal soldeApres;
    public String compteSrc;
    public String compteDest;
    public String date;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }

    public String getCodeOperation() {
        return codeOperation;
    }

    public void setCodeOperation(String codeOperation) {
        this.codeOperation = codeOperation;
    }

    public BigDecimal getSoldeCourant() {
        return soldeCourant;
    }

    public void setSoldeCourant(BigDecimal soldeCourant) {
        this.soldeCourant = soldeCourant;
    }

    public BigDecimal getSoldeApres() {
        return soldeApres;
    }

    public void setSoldeApres(BigDecimal soldeApres) {
        this.soldeApres = soldeApres;
    }

    public String getCompteSrc() {
        return compteSrc;
    }

    public void setCompteSrc(String compteSrc) {
        this.compteSrc = compteSrc;
    }

    public String getCompteDest() {
        return compteDest;
    }

    public void setCompteDest(String compteDest) {
        this.compteDest = compteDest;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
