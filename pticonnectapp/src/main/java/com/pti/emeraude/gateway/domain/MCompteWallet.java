package com.pti.emeraude.gateway.domain;

import com.pti.emeraude.gateway.enumeration.MTypeCompte;

public class MCompteWallet {

    private String numeroCompte;
    private String codeAgence;
    private String codeBanque;
    private MTypeCompte typeCompte;
    private String codeOperateur;
    private String libelleOperateur;
    private Long idOperateur;
    private String codeFacturier;
    private String alias;

    public String getCodeBanque() {
        return codeBanque;
    }

    public void setCodeBanque(String codeBanque) {
        this.codeBanque = codeBanque;
    }

    public MTypeCompte getTypeCompte() {
        return typeCompte;
    }

    public void setTypeCompte(MTypeCompte typeCompte) {
        this.typeCompte = typeCompte;
    }

    public String getNumeroCompte() {
        return numeroCompte;
    }

    public void setNumeroCompte(String numeroCompte) {
        this.numeroCompte = numeroCompte;
    }

    public String getCodeAgence() {
        return codeAgence;
    }

    public void setCodeAgence(String codeAgence) {
        this.codeAgence = codeAgence;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getCodeOperateur() {
        return codeOperateur;
    }

    public void setCodeOperateur(String codeOperateur) {
        this.codeOperateur = codeOperateur;
    }

    public String getLibelleOperateur() {
        return libelleOperateur;
    }

    public void setLibelleOperateur(String libelleOperateur) {
        this.libelleOperateur = libelleOperateur;
    }

    public Long getIdOperateur() {
        return idOperateur;
    }

    public void setIdOperateur(Long idOperateur) {
        this.idOperateur = idOperateur;
    }

    public String getCodeFacturier() {
        return codeFacturier;
    }

    public void setCodeFacturier(String codeFacturier) {
        this.codeFacturier = codeFacturier;
    }
}
