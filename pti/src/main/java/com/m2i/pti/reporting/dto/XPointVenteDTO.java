package com.m2i.pti.reporting.dto;
import java.math.BigDecimal;
import java.time.ZonedDateTime;



public class XPointVenteDTO {

    public String codePv;
    public String libellePv;
    public String uo;
    public String derniere_op;
    public String montant_op;
    public String duree;
    public String longitude;
    public String latitude;


    public String getCodePv() {
        return this.codePv;
    }

    public void setCodePv(String codePv) {
        this.codePv = codePv;
    }

    public String getLibellePv() {
        return this.libellePv;
    }

    public void setLibellePv(String libellePv) {
        this.libellePv = libellePv;
    }

    public String getUo() {
        return this.uo;
    }

    public void setUo(String uo) {
        this.uo = uo;
    }


    public String getDerniere_op() {
        return this.derniere_op;
    }

    public void setDerniere_op(String derniere_op) {
        this.derniere_op = derniere_op;
    }

    public String getMontant_op() {
        return this.montant_op;
    }

    public void setMontant_op(String montant_op) {
        this.montant_op = montant_op;
    }


    public String getDuree() {
        return this.duree;
    }

    public void setDuree(String duree) {
        this.duree = duree;
    }


    public String getLongitude() {
        return this.longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return this.latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    @Override
    public String toString() {
        return "{" +
                " codePv='" + getCodePv() + "'" +
                ", libellePv='" + getLibellePv() + "'" +
                ", uo='" + getUo() + "'" +
                ", derniere_op='" + getDerniere_op() + "'" +
                ", montant_op='" + getMontant_op() + "'" +
                ", duree='" + getDuree() + "'" +
                "}";
    }



}
