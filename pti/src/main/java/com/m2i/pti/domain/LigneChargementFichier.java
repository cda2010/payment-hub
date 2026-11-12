package com.m2i.pti.domain;

import com.m2i.pti.domain.enumeration.StatutLigneChargement;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "ligne_chargement_fichier")
public class LigneChargementFichier extends LigneChargement implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom")
    private String nom;

    @Column(name = "telephone")
    private String telephone;

    @Column(name = "type")
    private String type;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_ligne")
    private StatutLigneChargement statutLigne;

    @Column(name = "error")
    private String error;

    @Column(name = "info")
    private String info;

    @Column(name = "identifiant")
    private String identifiant;
    @ManyToOne
    private ChargementFichier chargementFichier;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    @Override
    public StatutLigneChargement getStatutLigne() {
        return statutLigne;
    }

    @Override
    public void setStatutLigne(StatutLigneChargement statutLigne) {
        this.statutLigne = statutLigne;
    }

    @Override
    public String getError() {
        return error;
    }

    @Override
    public void setError(String error) {
        this.error = error;
    }

    public ChargementFichier getChargementFichier() {
        return chargementFichier;
    }

    public void setChargementFichier(ChargementFichier chargementFichier) {
        this.chargementFichier = chargementFichier;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getIdentifiant() {
        return identifiant;
    }

    public void setIdentifiant(String identifiant) {
        this.identifiant = identifiant;
    }
}
