package com.m2i.pti.mobile.dto;

import java.math.BigDecimal;

public class MParamFrais {
    String codePv;
    String typeService;
    String canal;
    String  codeUoDest;
    String  codeUoSrc;
    java.math.BigDecimal montant;
private MInfos emetteur;
    public MParamFrais(String codePv, String typeService, String canal,  String codeUoSrc, String codeUoDest, BigDecimal montant,MInfos emetteur) {
        this.codePv = codePv;
        this.typeService = typeService;
        this.canal = canal;
        this.codeUoDest = codeUoDest;
        this.codeUoSrc = codeUoSrc;
        this.montant = montant;
        this.emetteur=emetteur;
    }

    public String getCodeUoDest() {
        return codeUoDest;
    }

    public void setCodeUoDest(String codeUoDest) {
        this.codeUoDest = codeUoDest;
    }

    public String getCodeUoSrc() {
        return codeUoSrc;
    }

    public void setCodeUoSrc(String codeUoSrc) {
        this.codeUoSrc = codeUoSrc;
    }

    public String getCodePv() {
        return codePv;
    }

    public void setCodePv(String codePv) {
        this.codePv = codePv;
    }

    public String getTypeService() {
        return typeService;
    }

    public void setTypeService(String typeService) {
        this.typeService = typeService;
    }

    public String getCanal() {
        return canal;
    }

    public void setCanal(String canal) {
        this.canal = canal;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }

    public MInfos getEmetteur() {
        return emetteur;
    }

    public void setEmetteur(MInfos emetteur) {
        this.emetteur = emetteur;
    }
    
}
