package com.m2i.pti.reporting.dto;

public class XFactureDTO {
    String refFacture;
    String refPaiement;
    Long produit;
    String datePaiement;
    String format;
    public Long facturierId;
    public String getRefFacture() {
        return refFacture;
    }

    public void setRefFacture(String refFacture) {
        this.refFacture = refFacture;
    }

    public String getRefPaiement() {
        return refPaiement;
    }

    public void setRefPaiement(String refPaiement) {
        this.refPaiement = refPaiement;
    }

    public Long getProduit() {
        return produit;
    }

    public void setProduit(Long produit) {
        this.produit = produit;
    }

    public String getDatePaiement() {
        return datePaiement;
    }

    public void setDatePaiement(String datePaiement) {
        this.datePaiement = datePaiement;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public Long getFacturierId() {
        return facturierId;
    }

    public void setFacturierId(Long facturierId) {
        this.facturierId = facturierId;
    }
}
