package com.m2i.pti.reporting.dto;

import java.math.BigDecimal;

public class XCumulOperation {
    private String agence;
    private BigDecimal montantEnvoi;
    private BigDecimal montantReception;
    private BigDecimal montantEnvoiTotal;
    private BigDecimal montantReceptionTotal;
    private Integer nombreEnvoi;
    private Integer nombre;
    private Integer nombreReception;
    private Integer nombreEnvoiTotal;
    private Integer nombreReceptionTotal;
    private String typeTransaction;
    private String periode;
    private BigDecimal montant;

    public Integer getNombre() {
        return nombre;
    }

    public void setNombre(Integer nombre) {
        this.nombre = nombre;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }

    public String getPeriode() {
        return periode;
    }

    public void setPeriode(String periode) {
        this.periode = periode;
    }

    public String getTypeTransaction() {
        return typeTransaction;
    }

    public void setTypeTransaction(String typeTransaction) {
        this.typeTransaction = typeTransaction;
    }

    public String getAgence() {
        return agence;
    }

    public void setAgence(String agence) {
        this.agence = agence;
    }

    public BigDecimal getMontantEnvoi() {
        return montantEnvoi;
    }

    public void setMontantEnvoi(BigDecimal montantEnvoi) {
        this.montantEnvoi = montantEnvoi;
    }

    public BigDecimal getMontantReception() {
        return montantReception;
    }

    public void setMontantReception(BigDecimal montantReception) {
        this.montantReception = montantReception;
    }

    public BigDecimal getMontantEnvoiTotal() {
        return montantEnvoiTotal;
    }

    public void setMontantEnvoiTotal(BigDecimal montantEnvoiTotal) {
        this.montantEnvoiTotal = montantEnvoiTotal;
    }

    public BigDecimal getMontantReceptionTotal() {
        return montantReceptionTotal;
    }

    public void setMontantReceptionTotal(BigDecimal montantReceptionTotal) {
        this.montantReceptionTotal = montantReceptionTotal;
    }

    public Integer getNombreEnvoi() {
        return nombreEnvoi;
    }

    public void setNombreEnvoi(Integer nombreEnvoi) {
        this.nombreEnvoi = nombreEnvoi;
    }

    public Integer getNombreReception() {
        return nombreReception;
    }

    public void setNombreReception(Integer nombreReception) {
        this.nombreReception = nombreReception;
    }

    public Integer getNombreEnvoiTotal() {
        return nombreEnvoiTotal;
    }

    public void setNombreEnvoiTotal(Integer nombreEnvoiTotal) {
        this.nombreEnvoiTotal = nombreEnvoiTotal;
    }

    public Integer getNombreReceptionTotal() {
        return nombreReceptionTotal;
    }

    public void setNombreReceptionTotal(Integer nombreReceptionTotal) {
        this.nombreReceptionTotal = nombreReceptionTotal;
    }
}
