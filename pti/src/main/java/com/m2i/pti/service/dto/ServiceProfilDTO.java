package com.m2i.pti.service.dto;


import com.m2i.pti.domain.enumeration.TypeProfilService;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

/**
 * A DTO for the ServiceProfil entity.
 */
public class ServiceProfilDTO implements Serializable {

    private Long id;

    @NotNull
    private String nom;

    private String description;

    TypeProfilService typeProfilService;

    private Set<TypeServiceDTO> typeServices = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<TypeServiceDTO> getTypeServices() {
        return typeServices;
    }

    public void setTypeServices(Set<TypeServiceDTO> typeServices) {
        this.typeServices = typeServices;
    }

    public TypeProfilService getTypeProfilService() {
        return typeProfilService;
    }

    public void setTypeProfilService(TypeProfilService typeProfilService) {
        this.typeProfilService = typeProfilService;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ServiceProfilDTO serviceProfilDTO = (ServiceProfilDTO) o;
        if(serviceProfilDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), serviceProfilDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "ServiceProfilDTO{" +
            "id=" + getId() +
            ", nom='" + getNom() + "'" +
            ", description='" + getDescription() + "'" +
            "}";
    }
}
