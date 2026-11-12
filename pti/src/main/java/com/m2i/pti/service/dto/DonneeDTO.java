package com.m2i.pti.service.dto;

public class DonneeDTO {

    Long id;
    String libelle;

    public DonneeDTO() {

    }
    public DonneeDTO(Long id, String libelle) {
        this.id = id ;
        this.libelle = libelle ;
    }

    public Long getId() {
        return id;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }
}
