/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pti.emeraude.gateway.domain;

/**
 *
 * @author cda
 */
public class MDataTransaction {

    MFrais mfrais;
    MClient beneficiaire;
    String nomUo;

    public MFrais getMfrais() {
        return mfrais;
    }

    public void setMfrais(MFrais mfrais) {
        this.mfrais = mfrais;
    }

    public MClient getBeneficiaire() {
        return beneficiaire;
    }

    public void setBeneficiaire(MClient beneficiaire) {
        this.beneficiaire = beneficiaire;
    }

    public String getNomUo() {
        return nomUo;
    }

    public void setNomUo(String nomUo) {
        this.nomUo = nomUo;
    }

}
