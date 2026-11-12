package com.m2i.pti.reporting.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;

public class XOperationSuivisDTO {
    private String nomClient;
    private String prenomClient;
    private String typeService;
    private String statut;
    private String pieceBeneficiaire;
    private BigDecimal montantOperation;
    private BigDecimal montantFrais;
    private BigDecimal montantTaxe;
    private String dateOperation;
    private String motif;

    public String getNomClient() {
        return nomClient;
    }

    public void setNomClient(String nomClient) {
        this.nomClient = nomClient;
    }

    public String getTypeService() {
        return typeService;
    }

    public void setTypeService(String typeService) {
        this.typeService = typeService;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getPieceBeneficiaire() {
        return pieceBeneficiaire;
    }

    public void setPieceBeneficiaire(String pieceBeneficiaire) {
        this.pieceBeneficiaire = pieceBeneficiaire;
    }

    public BigDecimal getMontantOperation() {
        return montantOperation;
    }

    public void setMontantOperation(BigDecimal montantOperation) {
        this.montantOperation = montantOperation;
    }

    public BigDecimal getMontantFrais() {
        return montantFrais;
    }

    public void setMontantFrais(BigDecimal montantFrais) {
        this.montantFrais = montantFrais;
    }

    public BigDecimal getMontantTaxe() {
        return montantTaxe;
    }

    public void setMontantTaxe(BigDecimal montantTaxe) {
        this.montantTaxe = montantTaxe;
    }

    public String getDateOperation() {
        return dateOperation;
    }

    public void setDateOperation(String dateOperation) {
        this.dateOperation = dateOperation;
    }

    public String getMotif() {
        return motif;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }

    public String getPrenomClient() {
        return prenomClient;
    }

    public void setPrenomClient(String prenomClient) {
        this.prenomClient = prenomClient;
    }
}
