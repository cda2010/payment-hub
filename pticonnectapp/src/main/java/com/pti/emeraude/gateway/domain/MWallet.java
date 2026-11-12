package com.pti.emeraude.gateway.domain;

import java.util.List;

public class MWallet {

    private String code;
    private String message;
    List<MCompteWallet> walletList;

    public MWallet(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public MWallet() {
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<MCompteWallet> getWalletList() {
        return walletList;
    }

    public void setWalletList(List<MCompteWallet> walletList) {
        this.walletList = walletList;
    }
}
