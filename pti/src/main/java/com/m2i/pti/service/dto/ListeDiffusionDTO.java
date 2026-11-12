package com.m2i.pti.service.dto;


import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

/**
 * A DTO for the ListeDiffusion entity.
 */
public class ListeDiffusionDTO implements Serializable {

    private Long id;

    private String nomListe;
    private DetailListeDiffusionDTO contacts[];

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNomListe() {
        return nomListe;
    }

    public void setNomListe(String nomListe) {
        this.nomListe = nomListe;
    }

    public DetailListeDiffusionDTO[] getContacts() {
        return contacts;
    }

    public void setContacts(DetailListeDiffusionDTO[] contacts) {
        this.contacts = contacts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ListeDiffusionDTO listeDiffusionDTO = (ListeDiffusionDTO) o;
        if(listeDiffusionDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), listeDiffusionDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "ListeDiffusionDTO{" +
            "id=" + getId() +
            ", nomListe='" + getNomListe() + "'" +
            "}";
    }
}
