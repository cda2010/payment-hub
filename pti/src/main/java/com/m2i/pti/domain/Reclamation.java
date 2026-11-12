package com.m2i.pti.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Objects;
import java.time.ZonedDateTime;

import com.m2i.pti.domain.enumeration.EtatReclamation;

import com.m2i.pti.domain.enumeration.QualiteReclamation;

/**
 * A Reclamation.
 */
@Entity
@Table(name = "reclamation")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Reclamation extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_heure_creation")
    private ZonedDateTime dateHeureCreation;

    @Column(name = "contenu")
    private String contenu;

    @Enumerated(EnumType.STRING)
    @Column(name = "etat")
    private EtatReclamation etat;

    @Column(name = "telephone")
    private String telephone;

    @Column(name = "code_auteur")
    private String codeAuteur;

    @Column(name = "date_modification")
    private ZonedDateTime dateModification;

    @Column(name = "solution")
    private String solution;

    @Column(name = "tache")
    private String tache;

    @Column(name = "document")
    private String document;

    @Column(name = "code_acteur")
    private String codeActeur;

    @Column(name = "attribue_a")
    private String attribueA;

    @Enumerated(EnumType.STRING)
    @Column(name = "qualite_auteur")
    private QualiteReclamation qualiteAuteur;

    @ManyToOne
   // @JsonIgnoreProperties("reclamations")
    private TypeReclamation typeReclamation;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ZonedDateTime getDateHeureCreation() {
        return dateHeureCreation;
    }

    public Reclamation dateHeureCreation(ZonedDateTime dateHeureCreation) {
        this.dateHeureCreation = dateHeureCreation;
        return this;
    }

    public String getCodeAuteur() {
        return codeAuteur;
    }

    public void setCodeAuteur(String codeAuteur) {
        this.codeAuteur = codeAuteur;
    }

    public void setDateHeureCreation(ZonedDateTime dateHeureCreation) {
        this.dateHeureCreation = dateHeureCreation;
    }

    public String getContenu() {
        return contenu;
    }

    public Reclamation contenu(String contenu) {
        this.contenu = contenu;
        return this;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public EtatReclamation getEtat() {
        return etat;
    }

    public Reclamation etat(EtatReclamation etat) {
        this.etat = etat;
        return this;
    }

    public void setEtat(EtatReclamation etat) {
        this.etat = etat;
    }

    public String getTelephone() {
        return telephone;
    }

    public Reclamation telephone(String telephone) {
        this.telephone = telephone;
        return this;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public ZonedDateTime getDateModification() {
        return dateModification;
    }

    public Reclamation dateModification(ZonedDateTime dateModification) {
        this.dateModification = dateModification;
        return this;
    }

    public void setDateModification(ZonedDateTime dateModification) {
        this.dateModification = dateModification;
    }

    public String getSolution() {
        return solution;
    }

    public Reclamation solution(String solution) {
        this.solution = solution;
        return this;
    }

    public void setSolution(String solution) {
        this.solution = solution;
    }

    public String getTache() {
        return tache;
    }

    public Reclamation tache(String tache) {
        this.tache = tache;
        return this;
    }

    public void setTache(String tache) {
        this.tache = tache;
    }

    public String getDocument() {
        return document;
    }

    public Reclamation document(String document) {
        this.document = document;
        return this;
    }

    public void setDocument(String document) {
        this.document = document;
    }

    public String getCodeActeur() {
        return codeActeur;
    }

    public Reclamation codeActeur(String codeActeur) {
        this.codeActeur = codeActeur;
        return this;
    }

    public void setCodeActeur(String codeActeur) {
        this.codeActeur = codeActeur;
    }

    public String getAttribueA() {
        return attribueA;
    }

    public Reclamation attribueA(String attribueA) {
        this.attribueA = attribueA;
        return this;
    }

    public void setAttribueA(String attribueA) {
        this.attribueA = attribueA;
    }

    public QualiteReclamation getQualiteAuteur() {
        return qualiteAuteur;
    }

    public Reclamation qualiteAuteur(QualiteReclamation qualiteAuteur) {
        this.qualiteAuteur = qualiteAuteur;
        return this;
    }

    public void setQualiteAuteur(QualiteReclamation qualiteAuteur) {
        this.qualiteAuteur = qualiteAuteur;
    }

    public TypeReclamation getTypeReclamation() {
        return typeReclamation;
    }

    public Reclamation typeReclamation(TypeReclamation typeReclamation) {
        this.typeReclamation = typeReclamation;
        return this;
    }

    public void setTypeReclamation(TypeReclamation typeReclamation) {
        this.typeReclamation = typeReclamation;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Reclamation)) {
            return false;
        }
        return id != null && id.equals(((Reclamation) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "Reclamation{" +
            "id=" + getId() +
            ", dateHeureCreation='" + getDateHeureCreation() + "'" +
            ", contenu='" + getContenu() + "'" +
            ", etat='" + getEtat() + "'" +
            ", telephone=" + getTelephone() +
            ", dateModification='" + getDateModification() + "'" +
            ", solution=" + getSolution() +
            ", tache=" + getTache() +
            ", document='" + getDocument() + "'" +
            ", codeActeur='" + getCodeActeur() + "'" +
            ", attribueA='" + getAttribueA() + "'" +
            ", qualiteAuteur='" + getQualiteAuteur() + "'" +
            "}";
    }
}
