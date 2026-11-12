package com.pti.emeraude.gateway.domain;

import java.util.List;

public class MWalletList {

    private String codeIndicatif;
    private String msisdn;
    private String accountNumber;
    private String typeCompte;
    List<MCompte> walletList;

    public String getCodeIndicatif() {
        return codeIndicatif;
    }

    public void setCodeIndicatif(String codeIndicatif) {
        this.codeIndicatif = codeIndicatif;
    }

    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getTypeCompte() {
        return typeCompte;
    }

    public void setTypeCompte(String typeCompte) {
        this.typeCompte = typeCompte;
    }

    public List<MCompte> getWalletList() {
        return walletList;
    }

    public void setWalletList(List<MCompte> walletList) {
        this.walletList = walletList;
    }
}
