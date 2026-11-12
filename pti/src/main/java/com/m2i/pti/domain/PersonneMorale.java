package com.m2i.pti.domain;

import com.m2i.pti.config.audit.EntityAuditEventListener;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A PersonneMorale.
 */
@Entity
@Table(name = "personne_morale")
@EntityListeners(value = {EntityAuditEventListener.class})
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class PersonneMorale extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "raison_sociale", nullable = false)
    private String raisonSociale;

    @Column(name = "registre_commerce")
    private String registreCommerce;

    @Column(name = "numreo_ifu")
    private String numreoIfu;

    @Column(name = "siege_social")
    private String siegeSocial;

    @Column(name = "date_rccm")
    private LocalDate dateRCCM;

    @Column(name = "dirigeant")
    private String dirigeant;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRaisonSociale() {
        return raisonSociale;
    }

    public PersonneMorale raisonSociale(String raisonSociale) {
        this.raisonSociale = raisonSociale;
        return this;
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

    public void setRaisonSociale(String raisonSociale) {
        this.raisonSociale = raisonSociale;
    }

    public String getRegistreCommerce() {
        return registreCommerce;
    }

    public PersonneMorale registreCommerce(String registreCommerce) {
        this.registreCommerce = registreCommerce;
        return this;
    }

    public void setRegistreCommerce(String registreCommerce) {
        this.registreCommerce = registreCommerce;
    }

    public String getNumreoIfu() {
        return numreoIfu;
    }

    public PersonneMorale numreoIfu(String numreoIfu) {
        this.numreoIfu = numreoIfu;
        return this;
    }

    public String getDirigeant() {
        return dirigeant;
    }

    public void setDirigeant(String dirigeant) {
        this.dirigeant = dirigeant;
    }

    public void setNumreoIfu(String numreoIfu) {
        this.numreoIfu = numreoIfu;
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
        PersonneMorale personneMorale = (PersonneMorale) o;
        if (personneMorale.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), personneMorale.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "PersonneMorale{" +
            "id=" + getId() +
            ", raisonSociale='" + getRaisonSociale() + "'" +
            ", registreCommerce='" + getRegistreCommerce() + "'" +
            ", numreoIfu='" + getNumreoIfu() + "'" +
            "}";
    }
}
