package com.pti.emeraude.gateway.domain;

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
    public void setLibelleTypeReclamation(String libelleTypeReclamation) {
        this.libelleTypeReclamation = libelleTypeReclamation;
    }
  public MTypeReclamation libelleTypeReclamation(String libelleTypeReclamation) {
        this.libelleTypeReclamation = libelleTypeReclamation;
        return this;
    }

  


}
