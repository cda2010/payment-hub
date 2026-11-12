package com.pti.emeraude.gateway.domain;


import com.pti.emeraude.gateway.enumeration.MTypeOrganisation;
import java.util.List;

public class MOrganisation {
    private String codeUo;
    private String nom;
    private String codeFse;
    private String codeBanque;
    private String codePere;
        private MTypeOrganisation typeDuPere;
    private String codePays;
    private MTypeOrganisation type;
    
    private MDevise devise;
       private String codeGroupe;
    private List<MDevise> mListeDevise;
      private String codePvDef;
    public MOrganisation() {
    }

    public MOrganisation(String codeUo, String nom, String codeFse, String codeBanque, String codePere, MTypeOrganisation typeDuPere, String codePays, MTypeOrganisation type, MDevise devise, String codeGroupe, List<MDevise> mListeDevise) {
        this.codeUo = codeUo;
        this.nom = nom;
        this.codeFse = codeFse;
        this.codeBanque = codeBanque;
        this.codePere = codePere;
        this.typeDuPere = typeDuPere;
        this.codePays = codePays;
        this.type = type;
        this.devise = devise;
        this.codeGroupe = codeGroupe;
        this.mListeDevise = mListeDevise;
    }

    public MOrganisation(String codeUo, String nom, String codeFse, String codeBanque, String codePere, String codePays, MTypeOrganisation type,MTypeOrganisation typeDuPere) {
        this.codeUo = codeUo;
        this.nom = nom;
        this.codeFse = codeFse;
        this.codeBanque = codeBanque;
        this.codePere = codePere;
        this.codePays = codePays;
        this.type = type;
        this.typeDuPere=typeDuPere;
    }
    public String getCodePere() {
        return codePere;
    }

    public void setCodePere(String codePere) {
        this.codePere = codePere;
    }

    public String getCodeUo() {
        return codeUo;
    }

    public void setCodeUo(String codeUo) {
        this.codeUo = codeUo;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getCodeFse() {
        return codeFse;
    }

    public void setCodeFse(String codeFse) {
        this.codeFse = codeFse;
    }

    public String getCodeBanque() {
        return codeBanque;
    }

    public void setCodeBanque(String codeBanque) {
        this.codeBanque = codeBanque;
    }

    public MTypeOrganisation getType() {
        return type;
    }

    public void setType(MTypeOrganisation type) {
        this.type = type;
    }

    public String getCodePays() {
        return codePays;
    }

    public void setCodePays(String codePays) {
        this.codePays = codePays;
    }

    public MTypeOrganisation getTypeDuPere() {
        return typeDuPere;
    }

    public void setTypeDuPere(MTypeOrganisation typeDuPere) {
        this.typeDuPere = typeDuPere;
    }

    public MDevise getDevise() {
        return devise;
    }

    public void setDevise(MDevise devise) {
        this.devise = devise;
    }

    public List<MDevise> getmListeDevise() {
        return mListeDevise;
    }

    public void setmListeDevise(List<MDevise> mListeDevise) {
        this.mListeDevise = mListeDevise;
    }

    public String getCodeGroupe() {
        return codeGroupe;
    }

    public void setCodeGroupe(String codeGroupe) {
        this.codeGroupe = codeGroupe;
    }

    @Override
    public String toString() {
        return "MOrganisation{" + "codeUo=" + codeUo + ", nom=" + nom + ", codeFse=" + codeFse + ", codeBanque=" + codeBanque + ", codePere=" + codePere + ", typeDuPere=" + typeDuPere + ", codePays=" + codePays + ", type=" + type + ", devise=" + devise + ", codeGroupe=" + codeGroupe + ", mListeDevise=" + mListeDevise + ", codePvDef=" + codePvDef + '}';
    }

    

    public String getCodePvDef() {
        return codePvDef;
    }

    public void setCodePvDef(String codePvDef) {
        this.codePvDef = codePvDef;
    }
    
}
