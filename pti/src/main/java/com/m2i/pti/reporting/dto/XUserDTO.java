package com.m2i.pti.reporting.dto;

public class XUserDTO {

    public String login;
    public String telephone;
    public Long profilId;
    public Long organisationId;
    public String reportFormat;
    public String estRapport;

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public Long getProfilId() {
        return profilId;
    }

    public void setProfilId(Long profilId) {
        this.profilId = profilId;
    }

    public Long getOrganisationId() {
        return organisationId;
    }

    public void setOrganisationId(Long organisationId) {
        this.organisationId = organisationId;
    }

    public String getReportFormat() {
        return reportFormat;
    }

    public void setReportFormat(String reportFormat) {
        this.reportFormat = reportFormat;
    }

    public String getEstRapport() {
        return estRapport;
    }

    public void setEstRapport(String estRapport) {
        this.estRapport = estRapport;
    }
}
