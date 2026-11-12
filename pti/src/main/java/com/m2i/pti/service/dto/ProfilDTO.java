package com.m2i.pti.service.dto;


import com.m2i.pti.domain.Authority;
import com.m2i.pti.domain.Profil;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A DTO for the Profil entity.
 */
public class ProfilDTO implements Serializable {

    private Long id;

    private String nom;

    private String description;

    private Long organisationId;

    private String organisationNom;

    private Long typeUOId;

    private String typeUONom;

    private Set<String> authorities;

    private Boolean profilsChange;

    public ProfilDTO() {
    }

    public ProfilDTO(Profil profil) {
        this(profil.getId(),profil.getNom(),profil.getDescription(),profil.getOrganisation().getId(),profil.getOrganisation().getLibelleUniteOrganisation(),
            profil.getRoles().stream().map(Authority::getName)
                .collect(Collectors.toSet()));
    }

    public ProfilDTO(Long id, String nom, String description, Long organisationId, String organisationNom, Set<String> authorities) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.organisationId = organisationId;
        this.organisationNom = organisationNom;
        this.authorities = authorities;
    }
    public ProfilDTO(Long id, String nom, String description, Long typeUOId, String typeUONom) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.typeUOId = typeUOId;
        this.typeUONom = typeUONom;
    }



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

    public Long getOrganisationId() {
        return organisationId;
    }

    public void setOrganisationId(Long organisationId) {
        this.organisationId = organisationId;
    }

    public String getOrganisationNom() {
        return organisationNom;
    }

    public void setOrganisationNom(String organisationNom) {
        this.organisationNom = organisationNom;
    }

    public Set<String> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(Set<String> authorities) {
        this.authorities = authorities;
    }

    public Boolean getProfilsChange() {
        return profilsChange;
    }

    public void setProfilsChange(Boolean profilsChange) {
        this.profilsChange = profilsChange;
    }

    public Long getTypeUOId() {
        return typeUOId;
    }

    public void setTypeUOId(Long typeUOId) {
        this.typeUOId = typeUOId;
    }

    public String getTypeUONom() {
        return typeUONom;
    }

    public void setTypeUONom(String typeUONom) {
        this.typeUONom = typeUONom;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ProfilDTO profilDTO = (ProfilDTO) o;
        if(profilDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), profilDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "ProfilDTO{" +
            "id=" + getId() +
            ", nom='" + getNom() + "'" +
            ", description='" + getDescription() + "'" +
            "}";
    }
}
