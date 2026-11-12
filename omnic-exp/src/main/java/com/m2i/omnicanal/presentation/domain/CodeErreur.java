/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.omnicanal.presentation.domain;

import java.io.Serializable;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 *
 * @author cda
 */
@Entity
public class CodeErreur implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private static final long serialVersionUID = 1L;
    String code;
    String libelle;
    String cptSrc;
    String cptDst;

    public CodeErreur(String code, String libelle, String cptSrc, String cptDst) {
        this.code = code;
        this.libelle = libelle;
        this.cptSrc=cptSrc;
        this.cptDst=cptDst;
    }

    public CodeErreur(String code, String libelle) {
        this.code = code;
        this.libelle = libelle;
    }

    public CodeErreur() {
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getCptSrc() {
        return cptSrc;
    }

    public void setCptSrc(String cptSrc) {
        this.cptSrc = cptSrc;
    }

    public String getCptDst() {
        return cptDst;
    }

    public void setCptDst(String cptDst) {
        this.cptDst = cptDst;
    }

    @Override
    public String toString() {
        return "CodeErreur{" + "id=" + id + ", code=" + code + ", libelle=" + libelle + ", cptSrc=" + cptSrc + ", cptDst=" + cptDst + '}';
    }
    
    
    
    

}
