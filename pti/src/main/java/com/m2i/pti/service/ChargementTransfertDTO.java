package com.m2i.pti.service;

import jakarta.persistence.Column;
import jakarta.persistence.Lob;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;

public class ChargementTransfertDTO implements Serializable {
    private Long id;

    private String nomFichier;

    private Double montantTotal;

    private String compteDebit;

    private Integer typeTransfert;

    private String codePays;

    private String motifOperation;

    private String description;

    private Double montantEntrp;

    private LocalDate dateChargement;

    private ZonedDateTime dateFichier;

    private String reference;

    private String keyPublic;

    private String signatureFichier;

    private String hashFichier;

    private Integer nombreLigne;

    private Long clientId;

    private Integer nombreLigneCharge;

    private Integer nombreLigneErreur;

    private String statut;

    private Instant createdDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNomFichier() {
        return nomFichier;
    }

    public void setNomFichier(String nomFichier) {
        this.nomFichier = nomFichier;
    }

    public Double getMontantTotal() {
        return montantTotal;
    }

    public void setMontantTotal(Double montantTotal) {
        this.montantTotal = montantTotal;
    }

    public String getCompteDebit() {
        return compteDebit;
    }

    public void setCompteDebit(String compteDebit) {
        this.compteDebit = compteDebit;
    }

    public Integer getTypeTransfert() {
        return typeTransfert;
    }

    public void setTypeTransfert(Integer typeTransfert) {
        this.typeTransfert = typeTransfert;
    }

    public String getCodePays() {
        return codePays;
    }

    public void setCodePays(String codePays) {
        this.codePays = codePays;
    }

    public String getMotifOperation() {
        return motifOperation;
    }

    public void setMotifOperation(String motifOperation) {
        this.motifOperation = motifOperation;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getMontantEntrp() {
        return montantEntrp;
    }

    public void setMontantEntrp(Double montantEntrp) {
        this.montantEntrp = montantEntrp;
    }

    public LocalDate getDateChargement() {
        return dateChargement;
    }

    public void setDateChargement(LocalDate dateChargement) {
        this.dateChargement = dateChargement;
    }

    public ZonedDateTime getDateFichier() {
        return dateFichier;
    }

    public void setDateFichier(ZonedDateTime dateFichier) {
        this.dateFichier = dateFichier;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getKeyPublic() {
        return keyPublic;
    }

    public void setKeyPublic(String keyPublic) {
        this.keyPublic = keyPublic;
    }

    public String getSignatureFichier() {
        return signatureFichier;
    }

    public void setSignatureFichier(String signatureFichier) {
        this.signatureFichier = signatureFichier;
    }

    public String getHashFichier() {
        return hashFichier;
    }

    public void setHashFichier(String hashFichier) {
        this.hashFichier = hashFichier;
    }

    public Integer getNombreLigne() {
        return nombreLigne;
    }

    public void setNombreLigne(Integer nombreLigne) {
        this.nombreLigne = nombreLigne;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public Integer getNombreLigneCharge() {
        return nombreLigneCharge;
    }

    public void setNombreLigneCharge(Integer nombreLigneCharge) {
        this.nombreLigneCharge = nombreLigneCharge;
    }

    public Integer getNombreLigneErreur() {
        return nombreLigneErreur;
    }

    public void setNombreLigneErreur(Integer nombreLigneErreur) {
        this.nombreLigneErreur = nombreLigneErreur;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }
}
