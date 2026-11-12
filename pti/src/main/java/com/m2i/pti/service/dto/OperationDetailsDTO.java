/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.service.dto;

import java.io.Serializable;
import com.m2i.pti.domain.Commission;
import com.m2i.pti.domain.enumeration.StatutOperation;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

/**
 *
 * @author cda
 */
public class OperationDetailsDTO implements Serializable {

    private BigDecimal montantOp;
    private BigDecimal montantFrais;
    private BigDecimal montantFraisClient;

    private BigDecimal montantFraisEm;

    private BigDecimal montantFraisAcq;

    private BigDecimal montantTaxe;

    private String codeOperation;

    private ZonedDateTime dateOperation;

    private String statut;

    private String codeService;
    private String codeExterne;
    private String devisePaiementLibelle;
    private String description;
    private String motif;
    private String raisonFrais;
    private String raisonComm;
    private boolean smsEnvoyer;
    private boolean smsGenerer;
    private boolean fraisAppliquer;
    private boolean commissionGenerer;
    private List<Commission> listeCommission;

    public BigDecimal getMontantOp() {
        return montantOp;
    }

    public void setMontantOp(BigDecimal montantOp) {
        this.montantOp = montantOp;
    }

    public BigDecimal getMontantFrais() {
        return montantFrais;
    }

    public void setMontantFrais(BigDecimal montantFrais) {
        this.montantFrais = montantFrais;
    }

    public BigDecimal getMontantFraisClient() {
        return montantFraisClient;
    }

    public void setMontantFraisClient(BigDecimal montantFraisClient) {
        this.montantFraisClient = montantFraisClient;
    }

    public BigDecimal getMontantFraisEm() {
        return montantFraisEm;
    }

    public void setMontantFraisEm(BigDecimal montantFraisEm) {
        this.montantFraisEm = montantFraisEm;
    }

    public BigDecimal getMontantFraisAcq() {
        return montantFraisAcq;
    }

    public void setMontantFraisAcq(BigDecimal montantFraisAcq) {
        this.montantFraisAcq = montantFraisAcq;
    }

    public BigDecimal getMontantTaxe() {
        return montantTaxe;
    }

    public void setMontantTaxe(BigDecimal montantTaxe) {
        this.montantTaxe = montantTaxe;
    }

    public String getCodeOperation() {
        return codeOperation;
    }

    public void setCodeOperation(String codeOperation) {
        this.codeOperation = codeOperation;
    }

    public ZonedDateTime getDateOperation() {
        return dateOperation;
    }

    public void setDateOperation(ZonedDateTime dateOperation) {
        this.dateOperation = dateOperation;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getCodeService() {
        return codeService;
    }

    public void setCodeService(String codeService) {
        this.codeService = codeService;
    }

    public String getCodeExterne() {
        return codeExterne;
    }

    public void setCodeExterne(String codeExterne) {
        this.codeExterne = codeExterne;
    }

    public String getDevisePaiementLibelle() {
        return devisePaiementLibelle;
    }

    public void setDevisePaiementLibelle(String devisePaiementLibelle) {
        this.devisePaiementLibelle = devisePaiementLibelle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMotif() {
        return motif;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }

    public String getRaisonFrais() {
        return raisonFrais;
    }

    public void setRaisonFrais(String raisonFrais) {
        this.raisonFrais = raisonFrais;
    }

    public String getRaisonComm() {
        return raisonComm;
    }

    public void setRaisonComm(String raisonComm) {
        this.raisonComm = raisonComm;
    }

    public boolean isSmsEnvoyer() {
        return smsEnvoyer;
    }

    public void setSmsEnvoyer(boolean smsEnvoyer) {
        this.smsEnvoyer = smsEnvoyer;
    }

    public boolean isSmsGenerer() {
        return smsGenerer;
    }

    public void setSmsGenerer(boolean smsGenerer) {
        this.smsGenerer = smsGenerer;
    }

    public boolean isFraisAppliquer() {
        return fraisAppliquer;
    }

    public void setFraisAppliquer(boolean fraisAppliquer) {
        this.fraisAppliquer = fraisAppliquer;
    }

    public boolean isCommissionGenerer() {
        return commissionGenerer;
    }

    public void setCommissionGenerer(boolean commissionGenerer) {
        this.commissionGenerer = commissionGenerer;
    }

    public List<Commission> getListeCommission() {
        return listeCommission;
    }

    public void setListeCommission(List<Commission> listeCommission) {
        this.listeCommission = listeCommission;
    }

    
    
    
}
