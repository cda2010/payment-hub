package com.m2i.pti.reporting.dto;

import java.util.List;

public class
FicheCommissions {
    private List<XCommissionDTO> infosCommissions;
    private String titre;
    private Double total;
    private String service;
    private String periode;
    private String organisation;

    public String getOrganisation() {
        return organisation;
    }

    public void setOrganisation(String organisation) {
        this.organisation = organisation;
    }

    public List<XCommissionDTO> getInfosCommissions() {
        return infosCommissions;
    }

    public void setInfosCommissions(List<XCommissionDTO> infosCommissions) {
        this.infosCommissions = infosCommissions;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getPeriode() {
        return periode;
    }

    public void setPeriode(String periode) {
        this.periode = periode;
    }
}
