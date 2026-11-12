package com.m2i.pti.service.dto;

import com.m2i.pti.domain.Client;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class InfosuppDTO implements Serializable {

    private Long id;
    private String libelle;
    private String description;

    public void setId(Long id) {
        this.id = id;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public String getDescription() {
        return description;
    }

    public Long getId() {
        return id;
    }

    public String getLibelle() {
        return libelle;
    }
}
