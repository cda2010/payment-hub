package com.m2i.pti.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

import com.m2i.pti.domain.enumeration.TypeLignePersonne;
import java.time.LocalDate;

/**
 * A LigneChargementCompte.
 */
@Entity
@Table(name = "ligne_chargement_compte")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class LigneChargementCompte extends LigneChargement implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom")
    private String nom;

    @Column(name = "prenom")
    private String prenom;

    @Column(name = "telephone")
    private String telephone;

    @Column(name = "date_naissance")
    private String dateNaissance;

    @Column(name = "lieu_naissance")
    private String lieuNaissance;

    @Column(name = "nom_mere")
    private String nomMere;

    @Column(name = "personne_prevenir")
    private String personnePrevenir;

    @Column(name = "email")
    private String email;

    @Column(name = "adresse")
    private String adresse;

    @Column(name = "type_personne")
    private String typePersonne;

    @ManyToOne
    private ChargementCompte chargementCompte;
 @Column(name = "sexe")
    private String sexe;
    @Column(name = "code_pays")
    private Integer codePays;
    @Column(name = "reference")
    private String reference;
    @Column(name = "code_type_piece")
    private String codeTypePiece;
    @Column(name = "lieu_delivrance")
    private String lieuDelivrance;
    @Column(name = "dateDelivrance")
    private LocalDate dateDelivrance;
    @Column(name = "dateExpiration")
    private LocalDate dateExpiration;
    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove

    public String getSexe() {
        return sexe;
    }

    public void setSexe(String sexe) {
        this.sexe = sexe;
    }

    public Integer getCodePays() {
        return codePays;
    }

    public void setCodePays(Integer codePays) {
        this.codePays = codePays;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getCodeTypePiece() {
        return codeTypePiece;
    }

    public void setCodeTypePiece(String codeTypePiece) {
        this.codeTypePiece = codeTypePiece;
    }

    public String getLieuDelivrance() {
        return lieuDelivrance;
    }

    public void setLieuDelivrance(String lieuDelivrance) {
        this.lieuDelivrance = lieuDelivrance;
    }

    public LocalDate getDateDelivrance() {
        return dateDelivrance;
    }

    public void setDateDelivrance(LocalDate dateDelivrance) {
        this.dateDelivrance = dateDelivrance;
    }

    public LocalDate getDateExpiration() {
        return dateExpiration;
    }

    public void setDateExpiration(LocalDate dateExpiration) {
        this.dateExpiration = dateExpiration;
    }
   
    
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public LigneChargementCompte nom(String nom) {
        this.nom = nom;
        return this;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public LigneChargementCompte prenom(String prenom) {
        this.prenom = prenom;
        return this;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getTelephone() {
        return telephone;
    }

    public LigneChargementCompte telephone(String telephone) {
        this.telephone = telephone;
        return this;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getDateNaissance() {
        return dateNaissance;
    }

    public LigneChargementCompte dateNaissance(String dateNaissance) {
        this.dateNaissance = dateNaissance;
        return this;
    }

    public void setDateNaissance(String dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    public String getLieuNaissance() {
        return lieuNaissance;
    }

    public LigneChargementCompte lieuNaissance(String lieuNaissance) {
        this.lieuNaissance = lieuNaissance;
        return this;
    }

    public void setLieuNaissance(String lieuNaissance) {
        this.lieuNaissance = lieuNaissance;
    }

    public String getNomMere() {
        return nomMere;
    }

    public LigneChargementCompte nomMere(String nomMere) {
        this.nomMere = nomMere;
        return this;
    }

    public void setNomMere(String nomMere) {
        this.nomMere = nomMere;
    }

    public String getPersonnePrevenir() {
        return personnePrevenir;
    }

    public LigneChargementCompte personnePrevenir(String personnePrevenir) {
        this.personnePrevenir = personnePrevenir;
        return this;
    }

    public void setPersonnePrevenir(String personnePrevenir) {
        this.personnePrevenir = personnePrevenir;
    }

    public String getEmail() {
        return email;
    }

    public LigneChargementCompte email(String email) {
        this.email = email;
        return this;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAdresse() {
        return adresse;
    }

    public LigneChargementCompte adresse(String adresse) {
        this.adresse = adresse;
        return this;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getTypePersonne() {
        return typePersonne;
    }

    public LigneChargementCompte typePersonne(String typePersonne) {
        this.typePersonne = typePersonne;
        return this;
    }

    public void setTypePersonne(String typePersonne) {
        this.typePersonne = typePersonne;
    }

    public ChargementCompte getChargementCompte() {
        return chargementCompte;
    }

    public LigneChargementCompte chargementCompte(ChargementCompte chargementCompte) {
        this.chargementCompte = chargementCompte;
        return this;
    }

    public void setChargementCompte(ChargementCompte chargementCompte) {
        this.chargementCompte = chargementCompte;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LigneChargementCompte ligneChargementCompte = (LigneChargementCompte) o;
        if (ligneChargementCompte.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), ligneChargementCompte.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "LigneChargementCompte{" +
            "id=" + getId() +
            ", nom='" + getNom() + "'" +
            ", prenom='" + getPrenom() + "'" +
            ", telephone='" + getTelephone() + "'" +
            ", dateNaissance='" + getDateNaissance() + "'" +
            ", lieuNaissance='" + getLieuNaissance() + "'" +
            ", nomMere='" + getNomMere() + "'" +
            ", personnePrevenir='" + getPersonnePrevenir() + "'" +
            ", email='" + getEmail() + "'" +
            ", adresse='" + getAdresse() + "'" +
            ", typePersonne='" + getTypePersonne() + "'" +
            "}";
    }
}
