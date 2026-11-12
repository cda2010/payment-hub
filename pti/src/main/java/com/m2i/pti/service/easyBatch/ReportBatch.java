/*
 * To ch ange this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.service.easyBatch;

/**
 *
 * @author cda
 */
public class ReportBatch {
    Integer nombreLigneCharge;
         Integer nombreLigneErreur;

    public ReportBatch() {
    }

    public ReportBatch(Integer nombreLigneCharge, Integer nombreLigneErreur) {
        this.nombreLigneCharge = nombreLigneCharge;
        this.nombreLigneErreur = nombreLigneErreur;
    }

    public Integer getNombreLigneCharge() {
        return nombreLigneCharge;
    }

    public void setNombreLigneCharge(Integer nombreLigneCharge) {
        this.nombreLigneCharge = nombreLigneCharge;
    }

    public Integer getNombreLigneErreur() {
        return nombreLigneErreur;
    }

    public void setNombreLigneErreur(Integer nombreLigneErreur) {
        this.nombreLigneErreur = nombreLigneErreur;
    }
         
}
