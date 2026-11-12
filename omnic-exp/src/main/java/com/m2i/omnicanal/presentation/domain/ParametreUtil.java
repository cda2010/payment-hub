package com.m2i.omnicanal.presentation.domain;


import com.m2i.omnicanal.model.enumeration.ParamatreType;
import com.m2i.omnicanal.model.enumeration.ProprieteApi;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;



/**
 * A Parametre.
 */


public   class  ParametreUtil implements Serializable {

    private static final long serialVersionUID = 1L;

 
    private Long id;

 
    private String nomParametre;
    private String codeParametre;
    private ParamatreType typeParametre;
    private ProprieteApi parametreApi;
      
    private String valeur;
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNomParametre() {
        return nomParametre;
    }

   

    public void setNomParametre(String nomParametre) {
        this.nomParametre = nomParametre;
    }

    public ParamatreType getTypeParametre() {
        return typeParametre;
    }

    

    public void setTypeParametre(ParamatreType typeParametre) {
        this.typeParametre = typeParametre;
    }

    public String getCodeParametre() {
        return codeParametre;
    }

    public void setCodeParametre(String codeParametre) {
        this.codeParametre = codeParametre;
    }

    public String getValeur() {
        return valeur;
    }

    public void setValeur(String valeur) {
        this.valeur = valeur;
    }

    public ProprieteApi getParametreApi() {
        return parametreApi;
    }

    public void setParametreApi(ProprieteApi parametreApi) {
        this.parametreApi = parametreApi;
    }

    
}
