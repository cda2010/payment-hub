/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pti.emeraude.gateway.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

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
    
    @XmlElement(name = "last4Digit")
    private String last4Digit;
    @XmlElement(name = "cardType")
    private String cardType;

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

    public String getLast4Digit() {
        return last4Digit;
    }

    public void setLast4Digit(String last4Digit) {
        this.last4Digit = last4Digit;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    @Override
    public String toString() {
        return "CustomerAccount{" + "teneurCompte=" + teneurCompte + ", numeroCompte=" + numeroCompte + ", codeAgence=" + codeAgence + ", codeBanque=" + codeBanque + ", rib=" + rib + ", typeCompte=" + typeCompte + '}';
    }
    


}
