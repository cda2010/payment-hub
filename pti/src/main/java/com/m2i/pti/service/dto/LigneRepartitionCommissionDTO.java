package com.m2i.pti.service.dto;


import com.m2i.pti.domain.enumeration.TypeTarif;

import jakarta.persistence.Column;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * A DTO for the LigneRepartitionCommission entity.
 */
public class LigneRepartitionCommissionDTO implements Serializable {

    private Long id;

    private Integer niveau;

    private Double valeur;
    private BigDecimal somme;
    private TypeTarif typeTarif;
    private BigDecimal min;
    private BigDecimal max;
    private Boolean surFrais;
    private String niveauLibelle;
   private String typeIntervalle;
    private Long tableauRepartitionCommissionId;

    public LigneRepartitionCommissionDTO() {
    }

    public LigneRepartitionCommissionDTO(Long id, Integer niveau, Double valeur, BigDecimal somme, Integer typeTarif, BigDecimal min, BigDecimal max, Boolean surFrais, Long tableauRepartitionCommissionId) {
        this.id = id;
        this.niveau = niveau;
        this.valeur = valeur;
        this.somme = somme;
        if (typeTarif==0){
            this.typeTarif = TypeTarif.FIXE;
        }
        if (typeTarif==1){
            this.typeTarif = TypeTarif.POURCENTAGE;
        }
        this.min = min;
        this.max = max;
        this.surFrais = surFrais;
        this.tableauRepartitionCommissionId = tableauRepartitionCommissionId;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getNiveau() {
        return niveau;
    }

    public void setNiveau(Integer niveau) {
        this.niveau = niveau;
    }

    public BigDecimal getSomme() {
        return somme;
    }

    public void setSomme(BigDecimal somme) {
        this.somme = somme;
    }

    public TypeTarif getTypeTarif() {
        return typeTarif;
    }

    public void setTypeTarif(TypeTarif typeTarif) {
        this.typeTarif = typeTarif;
    }

    public BigDecimal getMin() {
        return min;
    }

    public void setMin(BigDecimal min) {
        this.min = min;
    }

    public BigDecimal getMax() {
        return max;
    }

    public void setMax(BigDecimal max) {
        this.max = max;
    }

    public Boolean getSurFrais() {
        return surFrais;
    }

    public void setSurFrais(Boolean surFrais) {
        this.surFrais = surFrais;
    }

    public Double getValeur() {
        return valeur;
    }

    public void setValeur(Double valeur) {
        this.valeur = valeur;
    }

    public String getNiveauLibelle() {
        return niveauLibelle;
    }

    public void setNiveauLibelle(String niveauLibelle) {
        this.niveauLibelle = niveauLibelle;
    }

    public Long getTableauRepartitionCommissionId() {
        return tableauRepartitionCommissionId;
    }

    public void setTableauRepartitionCommissionId(Long tableauRepartitionCommissionId) {
        this.tableauRepartitionCommissionId = tableauRepartitionCommissionId;
    }

    public String getTypeIntervalle() {
        return typeIntervalle;
    }

    public void setTypeIntervalle(String typeIntervalle) {
        this.typeIntervalle = typeIntervalle;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LigneRepartitionCommissionDTO ligneRepartitionCommissionDTO = (LigneRepartitionCommissionDTO) o;
        if(ligneRepartitionCommissionDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), ligneRepartitionCommissionDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "LigneRepartitionCommissionDTO{" +
            "id=" + getId() +
            ", niveau=" + getNiveau() +
            ", valeur=" + getValeur() +
            "}";
    }
}
