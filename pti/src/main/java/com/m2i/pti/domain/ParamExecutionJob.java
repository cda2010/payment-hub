package com.m2i.pti.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A ParamExecutionJob.
 */
@Entity
@Table(name = "param_execution_job")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class ParamExecutionJob implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "cle")
    private String cle;
    @Column(name = "valeur")
    private String valeur;

    @ManyToOne
    private PtiJobDetails ptiJobDetail;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCle() {
        return cle;
    }

    public ParamExecutionJob cle(String cle) {
        this.cle = cle;
        return this;
    }

    public void setCle(String cle) {
        this.cle = cle;
    }

    public String getValeur() {
        return valeur;
    }

    public ParamExecutionJob valeur(String valeur) {
        this.valeur = valeur;
        return this;
    }

    public void setValeur(String valeur) {
        this.valeur = valeur;
    }

    public PtiJobDetails getPtiJobDetail() {
        return ptiJobDetail;
    }

    public ParamExecutionJob ptiJobDetail(PtiJobDetails ptiJobDetail) {
        this.ptiJobDetail = ptiJobDetail;
        return this;
    }

    public void setPtiJobDetail(PtiJobDetails ptiJobDetail) {
        this.ptiJobDetail = ptiJobDetail;
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
        ParamExecutionJob paramExecutionJob = (ParamExecutionJob) o;
        if (paramExecutionJob.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), paramExecutionJob.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "ParamExecutionJob{" +
            "id=" + getId() +
            ", cle='" + getCle() + "'" +
            ", valeur='" + getValeur() + "'" +
            "}";
    }
}
