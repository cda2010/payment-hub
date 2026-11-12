package com.m2i.pti.reporting.dto;

import com.m2i.pti.reporting.util.ReportHeader;

import java.math.BigDecimal;
import java.util.List;

public class XListBeneficiaire extends ReportHeader {
    private List<XBeneficiaire> listBeneficiaire;
    private String nomClient;
    private String numeroCompte;
    private String numeroClient;
    private BigDecimal montantTotal;

    public List<XBeneficiaire> getListBeneficiaire() {
        return listBeneficiaire;
    }

    public void setListBeneficiaire(List<XBeneficiaire> listBeneficiaire) {
        this.listBeneficiaire = listBeneficiaire;
    }

    public String getNomClient() {
        return nomClient;
    }

    public void setNomClient(String nomClient) {
        this.nomClient = nomClient;
    }

    public String getNumeroCompte() {
        return numeroCompte;
    }

    public void setNumeroCompte(String numeroCompte) {
        this.numeroCompte = numeroCompte;
    }

    public String getNumeroClient() {
        return numeroClient;
    }

    public void setNumeroClient(String numeroClient) {
        this.numeroClient = numeroClient;
    }

    public BigDecimal getMontantTotal() {
        return montantTotal;
    }

    public void setMontantTotal(BigDecimal montantTotal) {
        this.montantTotal = montantTotal;
    }
}
