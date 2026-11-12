package com.m2i.pti.external.domain;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlSeeAlso;

/**
 * A BwCustomer.
 */
@XmlRootElement(name = "COMMAND")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso(String.class)
public class ExtBwCustomer {

    @XmlElement(name = "numeroCompte")
    private String numeroCompte;
    @XmlElement(name = "nomCustomer")
    private String nomCustomer;
    @XmlElement(name = "prenomCustomer")
    private String prenomCustomer;

    @XmlElement(name = "codePays")
    private String codePays;
    @XmlElement(name = "msisdn")
    private String msisdn;
    @XmlElement(name = "codeFse")
    private String codeFse;
    @XmlElement(name = "alias")
    private String alias;

    String expiredDate;

    @XmlElement(name = "otp")
    private String otp;

    @XmlElement(name = "codeOperateur")
    private String codeOperateur;

    @XmlElement(name = "codeBanque")
    private String codeBanque;
    @XmlElement(name = "codeAgence")
    private String codeAgence;
    @XmlElement(name = "typePiece")
    private String typePiece;
    @XmlElement(name = "numeroPiece")
    private String numeroPiece;

    @XmlElement(name = "typeCompte")
    private String typeCompte;
    @XmlElement(name = "last4Digit")
    String last4Digit;
    @XmlElement(name = "activeImmediat")
    private Boolean activeImmediat;;




    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public String getNomCustomer() {
        return nomCustomer;
    }

    public void setNomCustomer(String nomCustomer) {
        this.nomCustomer = nomCustomer;
    }

    public String getPrenomCustomer() {
        return prenomCustomer;
    }

    public void setPrenomCustomer(String prenomCustomer) {
        this.prenomCustomer = prenomCustomer;
    }

    public String getTypePiece() {
        return typePiece;
    }

    public void setTypePiece(String typePiece) {
        this.typePiece = typePiece;
    }

    public String getNumeroPiece() {
        return numeroPiece;
    }

    public void setNumeroPiece(String numeroPiece) {
        this.numeroPiece = numeroPiece;
    }

    public String getCodePays() {
        return codePays;
    }

    public void setCodePays(String codePays) {
        this.codePays = codePays;
    }

    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public String getCodeFse() {
        return codeFse;
    }

    public void setCodeFse(String codeFse) {
        this.codeFse = codeFse;
    }

    public String getCodeBanque() {
        return codeBanque;
    }

    public void setCodeBanque(String codeBanque) {
        this.codeBanque = codeBanque;
    }

    public String getCodeAgence() {
        return codeAgence;
    }

    public void setCodeAgence(String codeAgence) {
        this.codeAgence = codeAgence;
    }

    public String getNumeroCompte() {
        return numeroCompte;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove
    public void setNumeroCompte(String numeroCompte) {
        this.numeroCompte = numeroCompte;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getTypeCompte() {
        return typeCompte;
    }

    public void setTypeCompte(String typeCompte) {
        this.typeCompte = typeCompte;
    }

    public Boolean getActiveImmediat() {
        return activeImmediat;
    }

    public void setActiveImmediat(Boolean activeImmediat) {
        this.activeImmediat = activeImmediat;
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "BwCustomer{"
                + ", nomCustomer='" + getNomCustomer() + "'"
                + ", prenomCustomer='" + getPrenomCustomer() + "'"
                + ", typePiece='" + getTypePiece() + "'"
                + ", numeroPiece='" + getNumeroPiece() + "'"
                + ", codePays='" + getCodePays() + "'"
                + ", msisdn='" + getMsisdn() + "'"
                + "}";
    }

    public String getLast4Digit() {
        return last4Digit;
    }

    public void setLast4Digit(String last4Digit) {
        this.last4Digit = last4Digit;
    }

    public String getExpiredDate() {
        return expiredDate;
    }

    public void setExpiredDate(String expiredDate) {
        this.expiredDate = expiredDate;
    }

    public String getCodeOperateur() {
        return codeOperateur;
    }

    public void setCodeOperateur(String codeOperateur) {
        this.codeOperateur = codeOperateur;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

}
