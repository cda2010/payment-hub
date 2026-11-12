/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.domain;


import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import jakarta.persistence.*;

/**
 *
 * @author cda
 */
@MappedSuperclass
public abstract class Chargement extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //AUTO
    private Long id;

    @Column(name = "nom_fichier")
    private String nomFichier;

    @Column(name = "date_chargement")
    private LocalDate dateChargement;
    @Column(name = "date_fichier")
    private ZonedDateTime dateFichier;
    @Column(name = "reference")
    private String reference;


    @Column(name = "key_public")
    private String keyPublic;

  

    @Column(name = "signature_fichier")
    private String signatureFichier;
    @Column(name = "hash_fichier")
    private String hashFichier;
    @Column(name = "nombre_ligne")
    private Integer nombreLigne;

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    @Column(name = "nombre_ligne_charge")
    private Integer nombreLigneCharge;

    @Column(name = "nombre_ligne_erreur")
    private Integer nombreLigneErreur;
    @Column(name = "statut")
    private String statut;

    @Column(name="validateur")
    private String validateur;

    @Column(name="verificateur")
    private String verificateur;

    @Column(name="valider")
    private Boolean estValider;

    @Column(name="estVerifier")
    private Boolean estVerifier;

    @ManyToOne
    private Client client;
    
    @ManyToOne
    private UniteOrganisation uniteOrganisation;

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

    public String getValidateur() {
        return validateur;
    }

    public void setValidateur(String validateur) {
        this.validateur = validateur;
    }

    public Boolean getEstValider() {
        return estValider;
    }

    public void setEstValider(Boolean estValider) {
        this.estValider = estValider;
    }

    public String getVerificateur() {
        return verificateur;
    }

    public void setVerificateur(String verificateur) {
        this.verificateur = verificateur;
    }

    public Boolean getEstVerifier() {
        return estVerifier;
    }

    public void setEstVerifier(Boolean estVerifier) {
        this.estVerifier = estVerifier;
    }
    
    public UniteOrganisation getUniteOrganisation() {
        return uniteOrganisation;
    }

    public void setUniteOrganisation(UniteOrganisation uniteOrganisation) {
        this.uniteOrganisation = uniteOrganisation;
    }
}
