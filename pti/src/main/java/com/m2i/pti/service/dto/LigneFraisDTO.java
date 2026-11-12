package com.m2i.pti.service.dto;


import java.io.Serializable;
import java.util.Objects;

import com.m2i.pti.domain.enumeration.Action;
import com.m2i.pti.domain.enumeration.TypeTarif;

/**
 * A DTO for the LigneFrais entity.
 */
public class LigneFraisDTO implements Serializable {

    private Long id;

    private TypeTarif typeFrais;

    private Double maximun;

    private Double minimun;
      private Double baseCalcul;

    private Double valeur;

    private Long tableauFraisId;

    private Action action;

    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TypeTarif getTypeFrais() {
        return typeFrais;
    }

    public void setTypeFrais(TypeTarif typeFrais) {
        this.typeFrais = typeFrais;
    }

    public Double getMaximun() {
        return maximun;
    }

    public void setMaximun(Double maximun) {
        this.maximun = maximun;
    }

    public Double getMinimun() {
        return minimun;
    }

    public void setMinimun(Double minimun) {
        this.minimun = minimun;
    }

    public Double getValeur() {
        return valeur;
    }

    public void setValeur(Double valeur) {
        this.valeur = valeur;
    }

    public Long getTableauFraisId() {
        return tableauFraisId;
    }

    public void setTableauFraisId(Long tableauFraisId) {
        this.tableauFraisId = tableauFraisId;
    }

    public Double getBaseCalcul() {
        return baseCalcul;
    }

    public void setBaseCalcul(Double baseCalcul) {
        this.baseCalcul = baseCalcul;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LigneFraisDTO ligneFraisDTO = (LigneFraisDTO) o;
        if(ligneFraisDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), ligneFraisDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "LigneFraisDTO{" +
            "id=" + getId() +
            ", typeFrais='" + getTypeFrais() + "'" +
            ", maximun=" + getMaximun() +
            ", minimun=" + getMinimun() +
            ", valeur=" + getValeur() +
            "}";
    }
}
