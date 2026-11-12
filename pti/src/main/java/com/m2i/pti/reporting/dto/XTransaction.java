package com.m2i.pti.reporting.dto;

import java.util.List;

public class XTransaction {
    public String titre;
    public String logo;
    public List<ListTransDTO> listTransaction;

    public List<ListTransDTO> getListTransaction() {
        return listTransaction;
    }

    public void setListTransaction(List<ListTransDTO> listTransaction) {
        this.listTransaction = listTransaction;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }
}
