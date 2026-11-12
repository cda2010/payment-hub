package com.m2i.pti.service.dto;


import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the EtiquetteCompte entity.
 */
public class EtiquetteCompteDTO implements Serializable {

    private Long id;

    private String code;
  
    private Boolean obligatoire;

    private Boolean transversal;
  private Boolean estDebiteur;
    private Integer niveau;
    private  String libelle;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Boolean isObligatoire() {
        return obligatoire;
    }

    public void setObligatoire(Boolean obligatoire) {
        this.obligatoire = obligatoire;
    }

    public Boolean isTransversal() {
        return transversal;
    }

    public Integer getNiveau() {
        return niveau;
    }

    public void setNiveau(Integer niveau) {
        this.niveau = niveau;
    }

    public void setTransversal(Boolean transversal) {
        this.transversal = transversal;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public Boolean getEstDebiteur() {
        return estDebiteur;
    }

    public void setEstDebiteur(Boolean estDebiteur) {
        this.estDebiteur = estDebiteur;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        EtiquetteCompteDTO etiquetteCompteDTO = (EtiquetteCompteDTO) o;
        if(etiquetteCompteDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), etiquetteCompteDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "EtiquetteCompteDTO{" +
            "id=" + getId() +
            ", code='" + getCode() + "'" +
                   ", libelle='" + getLibelle() + "'" +
            ", obligatoire='" + isObligatoire() + "'" +
            "}";
    }
}
