package com.m2i.pti.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A RecepteurAlarme.
 */
@Entity
@Table(name = "recepteur_alarme")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class RecepteurAlarme extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "etiquette")
    private String etiquette;

    @Column(name = "emails")
    private String emails;

    @Column(name = "sms")
    private String sms;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEtiquette() {
        return etiquette;
    }

    public RecepteurAlarme etiquette(String etiquette) {
        this.etiquette = etiquette;
        return this;
    }

    public void setEtiquette(String etiquette) {
        this.etiquette = etiquette;
    }

    public String getEmails() {
        return emails;
    }

    public RecepteurAlarme emails(String emails) {
        this.emails = emails;
        return this;
    }

    public void setEmails(String emails) {
        this.emails = emails;
    }

    public String getSms() {
        return sms;
    }

    public RecepteurAlarme sms(String sms) {
        this.sms = sms;
        return this;
    }

    public void setSms(String sms) {
        this.sms = sms;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RecepteurAlarme recepteurAlarme = (RecepteurAlarme) o;
        if (recepteurAlarme.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), recepteurAlarme.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "RecepteurAlarme{" +
            "id=" + getId() +
            ", etiquette='" + getEtiquette() + "'" +
            ", emails='" + getEmails() + "'" +
            ", sms='" + getSms() + "'" +
            "}";
    }
}
