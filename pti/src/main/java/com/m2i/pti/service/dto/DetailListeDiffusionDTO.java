package com.m2i.pti.service.dto;


import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

/**
 * A DTO for the DetailListeDiffusion entity.
 */
public class DetailListeDiffusionDTO implements Serializable {

    private Long id;

    private String numero;

    private Long listeId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public Long getListeId() {
        return listeId;
    }

    public void setListeId(Long listeDiffusionId) {
        this.listeId = listeDiffusionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DetailListeDiffusionDTO detailListeDiffusionDTO = (DetailListeDiffusionDTO) o;
        if(detailListeDiffusionDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), detailListeDiffusionDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "DetailListeDiffusionDTO{" +
            "id=" + getId() +
            ", numero='" + getNumero() + "'" +
            "}";
    }
}
