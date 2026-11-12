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
public class MtypeService {
       String codeUo;
    String code;
    String libelle;
    String ctxCodeFse;
  String ctxCodePv;
  

    public MtypeService(String code, String libelle, String ctxCodeFse, String ctxCodePv) {
        this.code = code;
        this.libelle = libelle;
        this.ctxCodeFse = ctxCodeFse;
        this.ctxCodePv = ctxCodePv;
    }

    public MtypeService(String codeUo, String code, String libelle, String ctxCodeFse, String ctxCodePv) {
        this.codeUo = codeUo;
        this.code = code;
        this.libelle = libelle;
        this.ctxCodeFse = ctxCodeFse;
        this.ctxCodePv = ctxCodePv;
    }

    public MtypeService() {
    }

    public MtypeService(String codeUo, String ctxCodeFse, String ctxCodePv) {
        this.codeUo = codeUo;
        this.ctxCodeFse = ctxCodeFse;
        this.ctxCodePv = ctxCodePv;
    }

    
    
    
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    @Override
    public String toString() {
        return "MtypeService{" + "code=" + code + ", libelle=" + libelle + '}';
    }

    public String getCtxCodeFse() {
        return ctxCodeFse;
    }

    public void setCtxCodeFse(String ctxCodeFse) {
        this.ctxCodeFse = ctxCodeFse;
    }

    public String getCtxCodePv() {
        return ctxCodePv;
    }

    public void setCtxCodePv(String ctxCodePv) {
        this.ctxCodePv = ctxCodePv;
    }

    public String getCodeUo() {
        return codeUo;
    }

    public void setCodeUo(String codeUo) {
        this.codeUo = codeUo;
    }

   
    
    
}
