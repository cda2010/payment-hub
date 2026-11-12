package com.m2i.pti.reporting.dto;

import java.time.Instant;
import java.util.List;

public class XLot {

    private String dateOuverture;
    private String numero;
    private String dateFermeture;
    private String organisation;

    private List<XDetailsLot> xDetailsLots;

    public String getDateOuverture() {
        return dateOuverture;
    }
    public String getNumero() {
        return numero;
    }
    public String getDateFermeture() {
        return dateFermeture;
    }
    public String getOrganisation() {
        return organisation;
    }
    public void setDateOuverture(String dateOuverture) {
        this.dateOuverture = dateOuverture;
    }
    public void setNumero(String numero) {
        this.numero = numero;
    }
    public void setDateFermeture(String dateFermeture) {
        this.dateFermeture = dateFermeture;
    }
    public void setOrganisation(String organisation) {
        this.organisation = organisation;
    }
    public List<XDetailsLot> getxDetailsLots() {
        return xDetailsLots;
    }
    public void setxDetailsLots(List<XDetailsLot> xDetailsLots) {
        this.xDetailsLots = xDetailsLots;
    }
   

}
