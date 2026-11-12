package com.m2i.pti.service.dto;

import com.m2i.pti.domain.enumeration.ParamatreType;
import com.m2i.pti.domain.enumeration.ProprieteApi;

public class ParametreUserDTO {
    private Long id;
    private String codeParametre;
    private ParamatreType typeParametre;
    private String nomParametre;
    private String valeur;
    private InterfaceUtilisateur interfaceUtilisateur;
    private ProprieteApi parametreApi;


    public ParametreUserDTO(Long id, String codeParametre, ParamatreType typeParametre, String nomParametre, String valeur, InterfaceUtilisateur interfaceUtilisateur, ProprieteApi parametreApi) {
        this.id = id;
        this.codeParametre = codeParametre;
        this.typeParametre = typeParametre;
        this.nomParametre = nomParametre;
        this.valeur = valeur;
        this.interfaceUtilisateur = interfaceUtilisateur;
        this.parametreApi = parametreApi;
    }

    public ParametreUserDTO(){

    }

    public ParamatreType getTypeParametre() {
        return typeParametre;
    }

    public void setTypeParametre(ParamatreType typeParametre) {
        this.typeParametre = typeParametre;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodeParametre() {
        return codeParametre;
    }

    public void setCodeParametre(String codeParametre) {
        this.codeParametre = codeParametre;
    }



    public String getNomParametre() {
        return nomParametre;
    }

    public void setNomParametre(String nomParametre) {
        this.nomParametre = nomParametre;
    }

    public String getValeur() {
        return valeur;
    }

    public void setValeur(String valeur) {
        this.valeur = valeur;
    }

    public InterfaceUtilisateur getInterfaceUtilisateur() {
        return interfaceUtilisateur;
    }

    public void setInterfaceUtilisateur(InterfaceUtilisateur interfaceUtilisateur) {
        this.interfaceUtilisateur = interfaceUtilisateur;
    }

    public ProprieteApi getParametreApi() {
        return parametreApi;
    }

    public void setParametreApi(ProprieteApi parametreApi) {
        this.parametreApi = parametreApi;
    }
}
