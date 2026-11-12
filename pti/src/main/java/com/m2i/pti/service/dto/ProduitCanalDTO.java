package com.m2i.pti.service.dto;


import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

/**
 * A DTO for the ProduitCanal entity.
 */
public class ProduitCanalDTO implements Serializable {

    private Long id;

    private Long canalId;

    private Long produitId;

    private String canalLibelle;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCanalId() {
        return canalId;
    }

    public void setCanalId(Long canalId) {
        this.canalId = canalId;
    }

    public Long getProduitId() {
        return produitId;
    }

    public void setProduitId(Long produitId) {
        this.produitId = produitId;
    }

    public String getCanalLibelle() {
        return canalLibelle;
    }

    public void setCanalLibelle(String canalLibelle) {
        this.canalLibelle = canalLibelle;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ProduitCanalDTO produitCanalDTO = (ProduitCanalDTO) o;
        if(produitCanalDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), produitCanalDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "ProduitCanalDTO{" +
            "id=" + getId() +
            "}";
    }
}
