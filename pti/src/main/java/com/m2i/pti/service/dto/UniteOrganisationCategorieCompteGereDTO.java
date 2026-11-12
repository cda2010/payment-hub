package com.m2i.pti.service.dto;

import java.io.Serializable;

import com.m2i.pti.domain.UniteOrganisation;
import com.m2i.pti.domain.enumeration.CategorieCompte;

public class UniteOrganisationCategorieCompteGereDTO implements Serializable{
    private Long id;
    
    private UniteOrganisation uniteOrganisation;

    private CategorieCompte categorieCompte;
  

    public UniteOrganisation getUniteOrganisation() {
        return uniteOrganisation;
    }
    public void setUniteOrganisation(UniteOrganisation uniteOrganisation) {
        this.uniteOrganisation = uniteOrganisation;
    }
    public CategorieCompte getCategorieCompte() {
        return categorieCompte;
    }
    public void setCategorieCompte(CategorieCompte categorieCompte) {
        this.categorieCompte = categorieCompte;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
   
    
}
