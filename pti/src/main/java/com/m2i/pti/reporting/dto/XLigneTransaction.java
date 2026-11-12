package com.m2i.pti.reporting.dto;

import com.m2i.pti.reporting.util.ReportHeader;

import java.util.List;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

public class XLigneTransaction {
    private String transaction;
    private ZonedDateTime dateTrans;
    private BigDecimal montantDebit;
    private BigDecimal montantCredit;
    private String description;
    private BigDecimal soldeAvant;
    private BigDecimal soldeCourant;
    private String type;



    public String getTransaction() {
        return this.transaction;
    }

    public void setTransaction(String transaction) {
        this.transaction = transaction;
    }

    public ZonedDateTime getDateTrans() {
        return this.dateTrans;
    }

    public void setDateTrans(ZonedDateTime dateTrans) {
        this.dateTrans = dateTrans;
    }

    public BigDecimal getMontantDebit() {
        return this.montantDebit;
    }

    public void setMontantDebit(BigDecimal montantDebit) {
        this.montantDebit = montantDebit;
    }

    public BigDecimal getMontantCredit() {
        return this.montantCredit;
    }

    public void setMontantCredit(BigDecimal montantCredit) {
        this.montantCredit = montantCredit;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getSoldeAvant() {
        return this.soldeAvant;
    }

    public void setSoldeAvant(BigDecimal soldeAvant) {
        this.soldeAvant = soldeAvant;
    }

    public BigDecimal getSoldeCourant() {
        return this.soldeCourant;
    }

    public void setSoldeCourant(BigDecimal soldeCourant) {
        this.soldeCourant = soldeCourant;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }
    



    @Override
    public String toString() {
        return "{" +
            " transaction='" + getTransaction() + "'" +
            ", dateTrans='" + getDateTrans() + "'" +
            ", montantDebit='" + getMontantDebit() + "'" +
            ", montantCredit='" + getMontantCredit() + "'" +
            ", description='" + getDescription() + "'" +
            ", soldeAvant='" + getSoldeAvant() + "'" +
            ", soldeCourant='" + getSoldeCourant() + "'" +
            ", type='" + getType() + "'" +
            "}";
    }
    
    



}