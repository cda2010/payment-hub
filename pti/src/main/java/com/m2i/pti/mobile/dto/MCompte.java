package com.m2i.pti.mobile.dto;

import com.m2i.pti.mobile.enumeration.MTypeCompte;
import java.math.BigDecimal;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlSeeAlso;
@XmlRootElement(name = "COMPTE")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso(String.class)
public class MCompte {
    
    
    
      @XmlElement(name = "las4Digit")
    private String las4Digit;
    @XmlElement(name = "teneurCompte")
    private String teneurCompte;
    @XmlElement(name = "codeOperateur")
    private String codeOperateur;
    @XmlElement(name = "numeroCompte")
    private String numeroCompte;
    @XmlElement(name = "codeAgence")
    private String codeAgence;
    @XmlElement(name = "codeBanque")
    private String codeBanque;
    @XmlElement(name = "rib")
    private String rib;
    @XmlElement(name = "typeCompte")
    private MTypeCompte typeCompte;
    @XmlElement(name = "alias")
    private String alias;



    public String getCodeBanque() {
        return codeBanque;
    }

    public void setCodeBanque(String codeBanque) {
        this.codeBanque = codeBanque;
    }

    public MTypeCompte getTypeCompte() {
        return typeCompte;
    }

    public void setTypeCompte(MTypeCompte typeCompte) {
        this.typeCompte = typeCompte;
    }

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

    public String getRib() {
        return rib;
    }

    public void setRib(String rib) {
        this.rib = rib;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getLas4Digit() {
        return las4Digit;
    }

    public void setLas4Digit(String las4Digit) {
        this.las4Digit = las4Digit;
    }

    public String getCodeOperateur() {
        return codeOperateur;
    }

    public void setCodeOperateur(String codeOperateur) {
        this.codeOperateur = codeOperateur;
    }

}
