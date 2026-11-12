/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.mobile.dto;

import java.util.List;

/**
 *
 * @author cda
 */
public class MDataParameter {

   private String fournisseurMonnaieDef;
    private String codeUoDef;
    private String pointVenteDef;
    private String codeSecretaExclure;
    private List<MTypePiece> mListypePiece;
    private List<MBrancheActivite> MLbrancheActivite;
    private List<MListeCategorieFse> listeCategorieFse;
    private List<MGroupeUniteOrganisation> MgroupeUniteOrganisation;
    private List<MNationalite> MLNationalite;
    private List<MtypeDoc> mListTypeDoc;
    private List<MMotif> mListMotif;
    private List<MOrganisation> mListOrganisation;
    private List<MOrganisation> mListFse;
    private String longueurNumTelephone;
    private String isAuthentificationForte;
    private String ageLimite;
    private List<MListeReseauServiceFse> mListReseauFse;
        private String isCreateCompte;
    private String fseDigital;

    private String devise;

    public List<MTypePiece> getmListypePiece() {
        return mListypePiece;
    }

    public void setmListypePiece(List<MTypePiece> mListypePiece) {
        this.mListypePiece = mListypePiece;
    }

    public List<MtypeDoc> getmListTypeDoc() {
        return mListTypeDoc;
    }

    public void setmListTypeDoc(List<MtypeDoc> mListTypeDoc) {
        this.mListTypeDoc = mListTypeDoc;
    }

    public List<MMotif> getmListMotif() {
        return mListMotif;
    }

    public void setmListMotif(List<MMotif> mListMotif) {
        this.mListMotif = mListMotif;
    }

    public List<MOrganisation> getmListOrganisation() {
        return mListOrganisation;
    }

    public void setmListOrganisation(List<MOrganisation> mListOrganisation) {
        this.mListOrganisation = mListOrganisation;
    }

    public List<MOrganisation> getmListFse() {
        return mListFse;
    }

    public void setmListFse(List<MOrganisation> mListFse) {
        this.mListFse = mListFse;
    }

    @Override
    public String toString() {
        return "MDataParameter{" + "mListypePiece=" + mListypePiece + ", mListTypeDoc=" + mListTypeDoc + ", mListMotif=" + mListMotif + ", mListOrganisation=" + mListOrganisation + ", mListFse=" + mListFse + '}';
    }

    public String getFournisseurMonnaieDef() {
        return fournisseurMonnaieDef;
    }

    public void setFournisseurMonnaieDef(String fournisseurMonnaieDef) {
        this.fournisseurMonnaieDef = fournisseurMonnaieDef;
    }

    public String getPointVenteDef() {
        return pointVenteDef;
    }

    public void setPointVenteDef(String pointVenteDef) {
        this.pointVenteDef = pointVenteDef;
    }

    public String getCodeSecretaExclure() {
        return codeSecretaExclure;
    }

    public void setCodeSecretaExclure(String codeSecretaExclure) {
        this.codeSecretaExclure = codeSecretaExclure;
    }

    public String getCodeUoDef() {
        return codeUoDef;
    }

    public void setCodeUoDef(String codeUoDef) {
        this.codeUoDef = codeUoDef;
    }

    public String getLongueurNumTelephone() {
        return longueurNumTelephone;
    }

    public void setLongueurNumTelephone(String longueurNumTelephone) {
        this.longueurNumTelephone = longueurNumTelephone;
    }

    public List<MBrancheActivite> getMLbrancheActivite() {
        return MLbrancheActivite;
    }

    public void setMLbrancheActivite(List<MBrancheActivite> MLbrancheActivite) {
        this.MLbrancheActivite = MLbrancheActivite;
    }

    public List<MNationalite> getMLNationalite() {
        return MLNationalite;
    }

    public void setMLNationalite(List<MNationalite> MLNationalite) {
        this.MLNationalite = MLNationalite;
    }

    public String getIsAuthentificationForte() {
        return isAuthentificationForte;
    }

    public void setIsAuthentificationForte(String isAuthentificationForte) {
        this.isAuthentificationForte = isAuthentificationForte;
    }

    public String getAgeLimite() {
        return ageLimite;
    }

    public void setAgeLimite(String ageLimite) {
        this.ageLimite = ageLimite;
    }

    public List<MListeCategorieFse> getListeCategorieFse() {
        return listeCategorieFse;
    }

    public void setListeCategorieFse(List<MListeCategorieFse> listeCategorieFse) {
        this.listeCategorieFse = listeCategorieFse;
    }

    public List<MGroupeUniteOrganisation> getMgroupeUniteOrganisation() {
        return MgroupeUniteOrganisation;
    }

    public void setMgroupeUniteOrganisation(List<MGroupeUniteOrganisation> MgroupeUniteOrganisation) {
        this.MgroupeUniteOrganisation = MgroupeUniteOrganisation;
    }

    public List<MListeReseauServiceFse> getmListReseauFse() {
        return mListReseauFse;
    }

    public void setmListReseauFse(List<MListeReseauServiceFse> mListReseauFse) {
        this.mListReseauFse = mListReseauFse;
    }
    
    public String getDevise() {
        return devise;
    }

    public void setDevise(String devise) {
        this.devise = devise;
    }

    public String getIsCreateCompte() {
        return isCreateCompte;
    }

    public void setIsCreateCompte(String isCreateCompte) {
        this.isCreateCompte = isCreateCompte;
    }

    public String getFseDigital() {
        return fseDigital;
    }

    public void setFseDigital(String fseDigital) {
        this.fseDigital = fseDigital;
    }
    
    
}
