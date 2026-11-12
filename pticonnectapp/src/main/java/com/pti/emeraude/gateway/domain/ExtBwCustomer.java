package com.pti.emeraude.gateway.domain;

/**
 * A BwCustomer.
 */
public class ExtBwCustomer {
     private String codeOperateur;
    private String codeBanque;
    private String codeAgence;
    private String numeroCompte;
    private String nomCustomer;
    private String prenomCustomer;
    private String typePiece;
    private String numeroPiece;
    private String codePays;
    private String msisdn;
    private String codeFse;
    private String alias;
    private String typeCompte;
    private String last4Digit;
    private String expiredDate;
    private Boolean activeImmediat;

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

    public String getCodeFse() {
        return codeFse;
    }

    public void setCodeFse(String codeFse) {
        this.codeFse = codeFse;
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

    public Boolean getActiveImmediat() {
        return activeImmediat;
    }

    public void setActiveImmediat(Boolean activeImmediat) {
        this.activeImmediat = activeImmediat;
    }

    public String getCodeOperateur() {
        return codeOperateur;
    }

    public void setCodeOperateur(String codeOperateur) {
        this.codeOperateur = codeOperateur;
    }
}
