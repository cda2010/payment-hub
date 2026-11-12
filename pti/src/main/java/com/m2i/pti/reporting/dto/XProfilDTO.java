package com.m2i.pti.reporting.dto;

import java.util.List;

public class XProfilDTO {
    public String profilLibelle;
    public String nomRole;
    public String descriptionRole;

    public List<String> roles;

    public String getProfilLibelle() {
        return profilLibelle;
    }

    public void setProfilLibelle(String profilLibelle) {
        this.profilLibelle = profilLibelle;
    }

    public String getNomRole() {
        return nomRole;
    }

    public void setNomRole(String nomRole) {
        this.nomRole = nomRole;
    }

    public String getDescriptionRole() {
        return descriptionRole;
    }

    public void setDescriptionRole(String descriptionRole) {
        this.descriptionRole = descriptionRole;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    @Override
    public String toString() {
        return "XProfilDTO{" +
                "profilLibelle='" + profilLibelle + '\'' +
                ", nomRole='" + nomRole + '\'' +
                '}';
    }
}
