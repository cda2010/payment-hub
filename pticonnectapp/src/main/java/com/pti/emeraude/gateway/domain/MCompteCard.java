package com.pti.emeraude.gateway.domain;

import com.pti.emeraude.gateway.enumeration.MTypeCompte;

public class MCompteCard extends StandardResponse {

    private String teneurCompte;
    private String numeroCompte;
    private String codeAgence;
    private String codeBanque;
    private String rib;
    private MTypeCompte typeCompte;
    private String codeOperateur;
    private String las4Digit;
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

    public String getTeneurCompte() {
        return teneurCompte;
    }

    public void setTeneurCompte(String teneurCompte) {
        this.teneurCompte = teneurCompte;
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

    public String getRib() {
        return rib;
    }

    public void setRib(String rib) {
        this.rib = rib;
    }

   

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getLas4Digit() {
        return las4Digit;
    }

    public void setLas4Digit(String las4Digit) {
        this.las4Digit = las4Digit;
    }

    public String getCodeOperateur() {
        return codeOperateur;
    }

    public void setCodeOperateur(String codeOperateur) {
        this.codeOperateur = codeOperateur;
    }
}
