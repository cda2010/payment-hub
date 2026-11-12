package com.m2i.pti.reporting.dto;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public class XTransactionDTO {
    public String logo;
    public String type;
    public BigDecimal montant;
    public String statut;
    public String codeOperation;
    public String service;
    public BigDecimal soldeCourant;
    public BigDecimal soldeApres;
    public String etat;
    public String motifOp;
    public String valeurCompte;
    public ZonedDateTime dateTransaction;
    public String libelleOrg;
    public String compteCible;
    public String etiquette;
    public String typeUO;

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

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getCodeOperation() {
        return codeOperation;
    }

    public void setCodeOperation(String codeOperation) {
        this.codeOperation = codeOperation;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
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

    public String getEtat() {
        return etat;
    }

    public void setEtat(String etat) {
        this.etat = etat;
    }

    public String getMotifOp() {
        return motifOp;
    }

    public void setMotifOp(String motifOp) {
        this.motifOp = motifOp;
    }

    public String getValeurCompte() {
        return valeurCompte;
    }

    public void setValeurCompte(String valeurCompte) {
        this.valeurCompte = valeurCompte;
    }

    public ZonedDateTime getDateTransaction() {
        return dateTransaction;
    }

    public void setDateTransaction(ZonedDateTime dateTransaction) {
        this.dateTransaction = dateTransaction;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getLibelleOrg() {
        return libelleOrg;
    }

    public void setLibelleOrg(String libelleOrg) {
        this.libelleOrg = libelleOrg;
    }

    public String getCompteCible() {
        return compteCible;
    }

    public void setCompteCible(String compteCible) {
        this.compteCible = compteCible;
    }

    public String getEtiquette() {
        return etiquette;
    }

    public void setEtiquette(String etiquette) {
        this.etiquette = etiquette;
    }

    public String getTypeUO() {
        return typeUO;
    }

    public void setTypeUO(String typeUO) {
        this.typeUO = typeUO;
    }
}
