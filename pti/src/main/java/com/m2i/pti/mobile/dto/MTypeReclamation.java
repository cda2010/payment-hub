package com.m2i.pti.mobile.dto;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;

import java.io.Serializable;

public class MTypeReclamation {

   
    private Long id;
    private String libelleTypeReclamation;
    public MTypeReclamation id(Long id) {
        this.id = id;
        return this;
    } 

 
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLibelleTypeReclamation() {
        return libelleTypeReclamation;
    }

    public MTypeReclamation libelleTypeReclamation(String libelleTypeReclamation) {
        this.libelleTypeReclamation = libelleTypeReclamation;
        return this;
    }

    public void setLibelleTypeReclamation(String libelleTypeReclamation) {
        this.libelleTypeReclamation = libelleTypeReclamation;
    }

  

  


}
