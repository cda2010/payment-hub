package com.m2i.pti.service.dto;

public class RelationDto {
    private Long id;

    private FournisseurServiceDTO fseSource;

    private FournisseurServiceDTO fseDestination;

    private String libelle;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public FournisseurServiceDTO getFseSource() {
        return fseSource;
    }

    public void setFseSource(FournisseurServiceDTO fseSource) {
        this.fseSource = fseSource;
    }

    public FournisseurServiceDTO getFseDestination() {
        return fseDestination;
    }

    public void setFseDestination(FournisseurServiceDTO fseDestination) {
        this.fseDestination = fseDestination;
    }
}