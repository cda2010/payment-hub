/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pti.emeraude.gateway.domain;

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
    private List<MtypeDoc> mListTypeDoc;
    private List<MMotif> mListMotif;
    private List<MOrganisation> mListOrganisation;
    private List<MOrganisation> mListFse;
    private List<MNationalite> MLNationalite;
    private String longueurNumTelephone;
    private String isAuthentificationForte;
    private String ageLimite;
    private String devise;
    private List<MGroupeUniteOrganisation> MgroupeUniteOrganisation;
    private List<MListeReseauServiceFse> mListReseauFse;
    private List<MListeCategorieFse> listeCategorieFse;

    private String isCreateCompte;
    private String fseDigital;

    public MDataParameter() {
    }

    public MDataParameter(List<MTypePiece> mListypePiece, List<MtypeDoc> mListTypeDoc, List<MMotif> mListMotif, List<MOrganisation> mListOrganisation, List<MOrganisation> mListFse, String ageLimite, String devise) {
        this.mListypePiece = mListypePiece;
        this.mListTypeDoc = mListTypeDoc;
        this.mListMotif = mListMotif;
        this.mListOrganisation = mListOrganisation;
        this.mListFse = mListFse;
        this.ageLimite = ageLimite;
        this.devise = devise;
    }

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

    public List<MGroupeUniteOrganisation> getMgroupeUniteOrganisation() {
        return MgroupeUniteOrganisation;
    }

    public void setMgroupeUniteOrganisation(List<MGroupeUniteOrganisation> MgroupeUniteOrganisation) {
        this.MgroupeUniteOrganisation = MgroupeUniteOrganisation;
    }

    public List<MListeCategorieFse> getListeCategorieFse() {
        return listeCategorieFse;
    }

    public void setListeCategorieFse(List<MListeCategorieFse> listeCategorieFse) {
        this.listeCategorieFse = listeCategorieFse;
    }

    public List<MListeReseauServiceFse> getmListReseauFse() {
        return mListReseauFse;
    }

    public void setmListReseauFse(List<MListeReseauServiceFse> mListReseauFse) {
        this.mListReseauFse = mListReseauFse;
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

    public String getDevise() {
        return devise;
    }

    public void setDevise(String devise) {
        this.devise = devise;
    }

    @Override
    public String toString() {
        return "MDataParameter{" + "fournisseurMonnaieDef=" + fournisseurMonnaieDef + ", codeUoDef=" + codeUoDef + ", pointVenteDef=" + pointVenteDef + ", codeSecretaExclure=" + codeSecretaExclure + ", mListypePiece=" + mListypePiece + ", MLbrancheActivite=" + MLbrancheActivite + ", mListTypeDoc=" + mListTypeDoc + ", mListMotif=" + mListMotif + ", mListOrganisation=" + mListOrganisation + ", mListFse=" + mListFse + ", MLNationalite=" + MLNationalite + ", longueurNumTelephone=" + longueurNumTelephone + ", isAuthentificationForte=" + isAuthentificationForte + ", ageLimite=" + ageLimite + ", MgroupeUniteOrganisation=" + MgroupeUniteOrganisation + ", mListReseauFse=" + mListReseauFse + ", listeCategorieFse=" + listeCategorieFse + '}';
    }

}
