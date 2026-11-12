package com.m2i.pti.service.dto;


import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the RecepteurAlarme entity.
 */
public class RecepteurAlarmeDTO implements Serializable {

    private Long id;

    private String etiquette;

    private String emails;

    private String sms;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEtiquette() {
        return etiquette;
    }

    public void setEtiquette(String etiquette) {
        this.etiquette = etiquette;
    }

    public String getEmails() {
        return emails;
    }

    public void setEmails(String emails) {
        this.emails = emails;
    }

    public String getSms() {
        return sms;
    }

    public void setSms(String sms) {
        this.sms = sms;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RecepteurAlarmeDTO recepteurAlarmeDTO = (RecepteurAlarmeDTO) o;
        if(recepteurAlarmeDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), recepteurAlarmeDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "RecepteurAlarmeDTO{" +
            "id=" + getId() +
            ", etiquette='" + getEtiquette() + "'" +
            ", emails='" + getEmails() + "'" +
            ", sms='" + getSms() + "'" +
            "}";
    }
}
