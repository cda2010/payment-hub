package com.m2i.pti.service.dto;


import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

import com.m2i.pti.domain.GroupRepartition;
import com.m2i.pti.domain.enumeration.DestinationProfilActeur;
import com.m2i.pti.domain.enumeration.TypeProfilActeur;

/**
 * A DTO for the ProfilActeur entity.
 */
public class ProfilActeurDTO implements Serializable {

    private Long id;
  
    @NotNull
    private String nom;
  
    @NotNull
    private String description;
    private String codeFse;
    private Boolean active;
    private DestinationProfilActeur destineA;

    private TypeProfilActeur type;

    private Set<HoraireAuthentificationDTO> horaireAuthentifications = new HashSet<>();

    private Set<TableauFraisDTO> tableauFrais = new HashSet<>();

    private Set<TableauCumulDTO> tableauCumuls = new HashSet<>();

    private Set<GroupRepartition> groupRepartitions = new HashSet<>();

    private Set<TableauRepartitionCommissionDTO> tableauRepartitionCommissions = new HashSet<>();

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

    public Boolean isActive() {
        return active;
    }

    public String getCodeFse() {
        return codeFse;
    }

    public void setCodeFse(String codeFse) {
        this.codeFse = codeFse;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public TypeProfilActeur getType() {
        return type;
    }

    public void setType(TypeProfilActeur type) {
        this.type = type;
    }

    public Set<HoraireAuthentificationDTO> getHoraireAuthentifications() {
        return horaireAuthentifications;
    }

    public void setHoraireAuthentifications(Set<HoraireAuthentificationDTO> horaireAuthentifications) {
        this.horaireAuthentifications = horaireAuthentifications;
    }

    public DestinationProfilActeur getDestineA() {
        return destineA;
    }

    public void setDestineA(DestinationProfilActeur destineA) {
        this.destineA = destineA;
    }

    public Set<TableauFraisDTO> getTableauFrais() {
        return tableauFrais;
    }

    public void setTableauFrais(Set<TableauFraisDTO> tableauFrais) {
        this.tableauFrais = tableauFrais;
    }

    public Set<TableauCumulDTO> getTableauCumuls() {
        return tableauCumuls;
    }

    public void setTableauCumuls(Set<TableauCumulDTO> tableauCumuls) {
        this.tableauCumuls = tableauCumuls;
    }

    public Set<TableauRepartitionCommissionDTO> getTableauRepartitionCommissions() {
        return tableauRepartitionCommissions;
    }

    public void setTableauRepartitionCommissions(Set<TableauRepartitionCommissionDTO> tableauRepartitionCommissions) {
        this.tableauRepartitionCommissions = tableauRepartitionCommissions;
    }

    public Set<GroupRepartition
            > getGroupRepartitions() {
        return groupRepartitions;
    }

    public void setGroupRepartitions(Set<GroupRepartition> groupRepartitions) {
        this.groupRepartitions = groupRepartitions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ProfilActeurDTO profilActeurDTO = (ProfilActeurDTO) o;
        if(profilActeurDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), profilActeurDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "ProfilActeurDTO{" +
            "id=" + getId() +
            ", nom='" + getNom() + "'" +
            ", description='" + getDescription() + "'" +
            ", active='" + isActive() + "'" +
            ", type='" + getType() + "'" +
            "}";
    }
}
