package com.m2i.pti.service.dto;


import com.m2i.pti.domain.enumeration.Acteur;
import com.m2i.pti.domain.enumeration.TypeTarif;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A DTO for the RepartitionFrais entity.
 */
public class RepartitionFraisDTO implements Serializable {

    private Long id;

    private Double min;

    private Double max;

    private TypeTarif typeTarif;

    private Acteur acteur;

    private String typeServiceLibelle;

    @NotNull
    private Double valeur;

    private Long typeServiceId;

    private Long fseSourceId;

    private Long fseDestId;

    private String fseSrcLibelle;

    private String fseAcqLibelle;

    private LocalDate dateDebut;

    private LocalDate dateFin;

    private Integer poidsNum;

    private Boolean activated;
    private Boolean surFrais;

    public Boolean getActivated() {
        return activated;
    }

    public void setActivated(Boolean activated) {
        this.activated = activated;
    }

    public Integer getPoidsNum() {
        return poidsNum;
    }

    public void setPoidsNum(Integer poidsNum) {
        this.poidsNum = poidsNum;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }

    public String getFseSrcLibelle() {
        return fseSrcLibelle;
    }

    public void setFseSrcLibelle(String fseSrcLibelle) {
        this.fseSrcLibelle = fseSrcLibelle;
    }

    public String getFseAcqLibelle() {
        return fseAcqLibelle;
    }

    public void setFseAcqLibelle(String fseAcqLibelle) {
        this.fseAcqLibelle = fseAcqLibelle;
    }

    public Long getFseSourceId() {
        return fseSourceId;
    }

    public void setFseSourceId(Long fseSourceId) {
        this.fseSourceId = fseSourceId;
    }

    public Long getFseDestId() {
        return fseDestId;
    }

    public void setFseDestId(Long fseDestId) {
        this.fseDestId = fseDestId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTypeServiceLibelle() {
        return typeServiceLibelle;
    }

    public void setTypeServiceLibelle(String typeServiceLibelle) {
        this.typeServiceLibelle = typeServiceLibelle;
    }

    public Double getMin() {
        return min;
    }

    public void setMin(Double min) {
        this.min = min;
    }

    public Double getMax() {
        return max;
    }

    public void setMax(Double max) {
        this.max = max;
    }

    public TypeTarif getTypeTarif() {
        return typeTarif;
    }

    public void setTypeTarif(TypeTarif typeTarif) {
        this.typeTarif = typeTarif;
    }

    public Acteur getActeur() {
        return acteur;
    }

    public void setActeur(Acteur acteur) {
        this.acteur = acteur;
    }

    public Double getValeur() {
        return valeur;
    }

    public void setValeur(Double valeur) {
        this.valeur = valeur;
    }

    public Long getTypeServiceId() {
        return typeServiceId;
    }

    public void setTypeServiceId(Long typeServiceId) {
        this.typeServiceId = typeServiceId;
    }

    public Boolean getSurFrais() {
        return surFrais;
    }

    public void setSurFrais(Boolean surFrais) {
        this.surFrais = surFrais;
    }
    

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RepartitionFraisDTO repartitionFraisDTO = (RepartitionFraisDTO) o;
        if(repartitionFraisDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), repartitionFraisDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "RepartitionFraisDTO{" +
            "id=" + getId() +
            ", min='" + getMin() + "'" +
            ", max='" + getMax() + "'" +
            ", typeTarif='" + getTypeTarif() + "'" +
            ", acteur='" + getActeur() + "'" +
            ", valeur='" + getValeur() + "'" +
            "}";
    }
}
