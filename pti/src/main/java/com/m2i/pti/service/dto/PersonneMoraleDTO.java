package com.m2i.pti.service.dto;


import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A DTO for the PersonneMorale entity.
 */
public class PersonneMoraleDTO implements Serializable {

    private Long id;

    private String raisonSociale;

    private String registreCommerce;

    private String numreoIfu;

    private String siegeSocial;

    private LocalDate dateRCCM;
    private String dirigeant;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRaisonSociale() {
        return raisonSociale;
    }

    public void setRaisonSociale(String raisonSociale) {
        this.raisonSociale = raisonSociale;
    }

    public String getRegistreCommerce() {
        return registreCommerce;
    }

    public void setRegistreCommerce(String registreCommerce) {
        this.registreCommerce = registreCommerce;
    }

    public String getNumreoIfu() {
        return numreoIfu;
    }

    public void setNumreoIfu(String numreoIfu) {
        this.numreoIfu = numreoIfu;
    }

    public String getSiegeSocial() {
        return siegeSocial;
    }

    public void setSiegeSocial(String siegeSocial) {
        this.siegeSocial = siegeSocial;
    }

    public LocalDate getDateRCCM() {
        return dateRCCM;
    }

    public void setDateRCCM(LocalDate dateRCCM) {
        this.dateRCCM = dateRCCM;
    }

    public String getDirigeant() {
        return dirigeant;
    }

    public void setDirigeant(String dirigeant) {
        this.dirigeant = dirigeant;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PersonneMoraleDTO personneMoraleDTO = (PersonneMoraleDTO) o;
        if(personneMoraleDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), personneMoraleDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "PersonneMoraleDTO{" +
            "id=" + getId() +
            ", raisonSociale='" + getRaisonSociale() + "'" +
            ", registreCommerce='" + getRegistreCommerce() + "'" +
            ", numreoIfu='" + getNumreoIfu() + "'" +
            "}";
    }
}
