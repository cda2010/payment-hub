package com.m2i.pti.service.dto;

import com.m2i.pti.domain.enumeration.TypeCanal;

/**
 * A Canal Application
 */

public class CanalApplicationDTO {

    private Long id;

    private String libelleCanal;
    
    private TypeCanal typeCanal;

    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLibelleCanal() {
        return libelleCanal;
    }

    public void setLibelleCanal(String libelleCanal) {
        this.libelleCanal = libelleCanal;
    }

    public TypeCanal getTypeCanal() {
        return typeCanal;
    }

    public void setTypeCanalApplication(TypeCanal typeCanal) {
        this.typeCanal = typeCanal;
    }

    public CanalApplicationDTO(Long id, String libelleCanal, String typeCanal) {
        this.id = id;
        this.libelleCanal = libelleCanal;
        this.typeCanal = typeCanal!=null? TypeCanal.valueOf(typeCanal):null;
    }

    public CanalApplicationDTO() {

    }

    
}
