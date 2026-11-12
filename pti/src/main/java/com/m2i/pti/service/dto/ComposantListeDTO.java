package com.m2i.pti.service.dto;

import com.m2i.pti.domain.enumeration.TypeComposant;

public class ComposantListeDTO {

    private Long id;
    private String label;
    private String type;
    private String typeComposant;

    public ComposantListeDTO(Long id, String label, String type, TypeComposant typeComposant) {
        this.id = id;
        this.label = label;
        this.type = type;
        this.typeComposant = typeComposant == null ? null : typeComposant.name();
    }

    public Long getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String getType() {
        return type;
    }

    public String getTypeComposant() {
        return typeComposant;
    }
}
