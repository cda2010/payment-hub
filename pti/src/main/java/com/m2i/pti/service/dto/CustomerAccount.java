/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.service.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlSeeAlso;

/**
 *
 * @author cda
 */
@XmlRootElement(name = "compte")
@XmlAccessorType(XmlAccessType.FIELD)
public class CustomerAccount {
    @XmlElement(name = "teneurCompte")
    private String teneurCompte;
    @XmlElement(name = "numeroCompte")
    private String numeroCompte;
    @XmlElement(name = "codeAgence")
    private String codeAgence;
    @XmlElement(name = "codeBanque")
    private String codeBanque;
    @XmlElement(name = "rib")
    private String rib;
    @XmlElement(name = "typeCompte")
    private String typeCompte;

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

    public String getCodeBanque() {
        return codeBanque;
    }

    public void setCodeBanque(String codeBanque) {
        this.codeBanque = codeBanque;
    }

    public String getRib() {
        return rib;
    }

    public void setRib(String rib) {
        this.rib = rib;
    }

    public String getTypeCompte() {
        return typeCompte;
    }

    public void setTypeCompte(String typeCompte) {
        this.typeCompte = typeCompte;
    }

}
