package com.m2i.pti.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

import com.m2i.pti.domain.enumeration.StatutLigneChargement;

/**
 * A LigneChargementTransfert.
 */
@Entity
@Table(name = "ligne_chargement_transfert")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class LigneChargementTransfert extends LigneChargement implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom_ligne")
    private String nomLigne;

    @Column(name = "nom")
    private String nom;

    @Column(name = "prenom")
    private String prenom;

    @Column(name = "numero_compte")
    private String numeroCompte;

    @Column(name = "numero_telephone")
    private String numeroTelephone;

    @Column(name = "montant")
    private Double montant;

    @Column(name = "solde_apres")
    private Double soldeApres;

    @Column(name = "narration")
    private String narration;
    @Column(name = "compte_id")
    private Long compteId;
  @Column(name = "error")
    private String error;
  @Column(name = "code_operation")
    private String codeOperation;
    @Enumerated(EnumType.STRING)
    @Column(name = "statut_ligne")
    private StatutLigneChargement statutLigne;

    @ManyToOne
    private ChargementTransfert chargementTransfert;
    @Column(name = "description")
    private String description;
    public Double getSoldeAvant() {
        return soldeAvant;
    }

    public void setSoldeAvant(Double soldeAvant) {
        this.soldeAvant = soldeAvant;
    }

    @Column(name = "solde_avant")
    private Double soldeAvant;

    public Double getSoldeApres() {
        return soldeApres;
    }

    public void setSoldeApres(Double soldeApres) {
        this.soldeApres = soldeApres;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNarration() {
        return narration;
    }

    public void setNarration(String narration) {
        this.narration = narration;
    }

  
    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNomLigne() {
        return nomLigne;
    }

    public LigneChargementTransfert nomLigne(String nomLigne) {
        this.nomLigne = nomLigne;
        return this;
    }

    public void setNomLigne(String nomLigne) {
        this.nomLigne = nomLigne;
    }

    public String getNom() {
        return nom;
    }

    public LigneChargementTransfert nom(String nom) {
        this.nom = nom;
        return this;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public LigneChargementTransfert prenom(String prenom) {
        this.prenom = prenom;
        return this;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getNumeroCompte() {
        return numeroCompte;
    }

    public LigneChargementTransfert numeroCompte(String numeroCompte) {
        this.numeroCompte = numeroCompte;
        return this;
    }

    public void setNumeroCompte(String numeroCompte) {
        this.numeroCompte = numeroCompte;
    }

    public String getNumeroTelephone() {
        return numeroTelephone;
    }

    public LigneChargementTransfert numeroTelephone(String numeroTelephone) {
        this.numeroTelephone = numeroTelephone;
        return this;
    }

    public void setNumeroTelephone(String numeroTelephone) {
        this.numeroTelephone = numeroTelephone;
    }

    public Double getMontant() {
        return montant;
    }

    public LigneChargementTransfert montant(Double montant) {
        this.montant = montant;
        return this;
    }

    public void setMontant(Double montant) {
        this.montant = montant;
    }

    public StatutLigneChargement getStatutLigne() {
        return statutLigne;
    }

    public LigneChargementTransfert statutLigne(StatutLigneChargement statutLigne) {
        this.statutLigne = statutLigne;
        return this;
    }

    public void setStatutLigne(StatutLigneChargement statutLigne) {
        this.statutLigne = statutLigne;
    }

    public ChargementTransfert getChargementTransfert() {
        return chargementTransfert;
    }

    public LigneChargementTransfert chargementTransfert(ChargementTransfert chargementTransfert) {
        this.chargementTransfert = chargementTransfert;
        return this;
    }

    public void setChargementTransfert(ChargementTransfert chargementTransfert) {
        this.chargementTransfert = chargementTransfert;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    public Long getCompteId() {
        return compteId;
    }

    public void setCompteId(Long compteId) {
        this.compteId = compteId;
    }

    public String getCodeOperation() {
        return codeOperation;
    }

    public void setCodeOperation(String codeOperation) {
        this.codeOperation = codeOperation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LigneChargementTransfert ligneChargementTransfert = (LigneChargementTransfert) o;
        if (ligneChargementTransfert.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), ligneChargementTransfert.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "LigneChargementTransfert{"
                + "id=" + getId()
                + ", nomLigne='" + getNomLigne() + "'"
                + ", nom='" + getNom() + "'"
                + ", prenom='" + getPrenom() + "'"
                + ", numeroCompte='" + getNumeroCompte() + "'"
                + ", numeroTelephone='" + getNumeroTelephone() + "'"
                + ", montant='" + getMontant() + "'"
                + ", statutLigne='" + getStatutLigne() + "'"
                + ", error='" + getError() + "'"

                + "}";
    }
    private String padLeftZeros;

    public void setRrn(String padLeftZeros) {
        this.padLeftZeros = padLeftZeros;
    }
}
