package com.m2i.pti.service.dto;

import java.util.List;

public class ProduitDTO {
    private Long id;
    private String nomProduit;
    private Integer numeroOrdre;
    private boolean actif;
        private boolean transactionnel;
    private boolean aUnMontant;
    private double montant;
    private Long facturierId;
    private String nomFacturier;
    private Long serviceAssocieId;
    private List<Long> canalIds;

    public ProduitDTO(Long id, String nomProduit, Integer numeroOrdre, boolean actif, boolean transactionnel, boolean aUnMontant, double montant) {
        this.id = id;
        this.nomProduit = nomProduit;
        this.numeroOrdre = numeroOrdre;
        this.actif = actif;
        this.transactionnel = transactionnel;
        this.aUnMontant = aUnMontant;
        this.montant = montant;
    }

    public ProduitDTO(Long id, String nomProduit, Integer numeroOrdre, boolean actif, boolean transactionnel, boolean aUnMontant, double montant, Long facturierId, String nomFacturier) {
        this.id = id;
        this.nomProduit = nomProduit;
        this.numeroOrdre = numeroOrdre;
        this.actif = actif;
        this.transactionnel = transactionnel;
        this.aUnMontant = aUnMontant;
        this.montant = montant;
        this.facturierId = facturierId;
        this.nomFacturier = nomFacturier;
    }

    public ProduitDTO(Long id, String nomProduit, Integer numeroOrdre, boolean actif, boolean transactionnel, boolean aUnMontant, double montant, Long facturierId, Long serviceAssocieId) {
        this.id = id;
        this.nomProduit = nomProduit;
        this.numeroOrdre = numeroOrdre;
        this.actif = actif;
        this.transactionnel = transactionnel;
        this.aUnMontant = aUnMontant;
        this.montant = montant;
        this.facturierId = facturierId;
        this.serviceAssocieId = serviceAssocieId;
    }

    
    
    public ProduitDTO() {
    }

    public Long getServiceAssocieId() {
        return serviceAssocieId;
    }

    public void setServiceAssocieId(Long serviceAssocieId) {
        this.serviceAssocieId = serviceAssocieId;
    }

    
    
    
    public String getNomProduit() {
        return nomProduit;
    }

    public void setNomProduit(String nomProduit) {
        this.nomProduit = nomProduit;
    }

    public Integer getNumeroOrdre() {
        return numeroOrdre;
    }

    public void setNumeroOrdre(Integer numeroOrdre) {
        this.numeroOrdre = numeroOrdre;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    public boolean isaUnMontant() {
        return aUnMontant;
    }

    public void setaUnMontant(boolean aUnMontant) {
        this.aUnMontant = aUnMontant;
    }

    public double getMontant() {
        return montant;
    }

    public void setMontant(double montant) {
        this.montant = montant;
    }

    public Long getFacturierId() {
        return facturierId;
    }

    public void setFacturierId(Long facturierId) {
        this.facturierId = facturierId;
    }

    public String getNomFacturier() {
        return nomFacturier;
    }

    public void setNomFacturier(String nomFacturier) {
        this.nomFacturier = nomFacturier;
    }

    public List<Long> getCanalIds() {
        return canalIds;
    }

    public void setCanalIds(List<Long> canalIds) {
        this.canalIds = canalIds;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isTransactionnel() {
        return transactionnel;
    }

    public void setTransactionnel(boolean transactionnel) {
        this.transactionnel = transactionnel;
    }

    @Override
    public String toString() {
        return "ProduitDTO{" +
                "id=" + id +
                ", nomProduit='" + nomProduit + '\'' +
                ", numeroOrdre=" + numeroOrdre +
                ", actif=" + actif +
                ", aUnMontant=" + aUnMontant +
                ", montant=" + montant +
                ", facturierId=" + facturierId +
                ", canalIds=" + canalIds +
                '}';
    }
}
