package com.m2i.pti.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * A HistExecutionJob.
 */
@Entity
@Table(name = "hist_execution_job")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class HistExecutionJob extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type_trigger")
    private String typeTrigger;

    @Column(name = "debut_execution")
    private ZonedDateTime debutExecution;

    @Column(name = "fin_execution")
    private ZonedDateTime finExecution;

    @Column(name = "statut")
    private String statut;

    @Column(name = "message_erreur")
    private String messageErreur;

    @Column(name = "erreur_log")
    private String erreurLog;

    @ManyToOne
    private PtiJobDetails ptiJobDetail;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTypeTrigger() {
        return typeTrigger;
    }

    public HistExecutionJob typeTrigger(String typeTrigger) {
        this.typeTrigger = typeTrigger;
        return this;
    }

    public void setTypeTrigger(String typeTrigger) {
        this.typeTrigger = typeTrigger;
    }

    public ZonedDateTime getDebutExecution() {
        return debutExecution;
    }

    public HistExecutionJob debutExecution(ZonedDateTime debutExecution) {
        this.debutExecution = debutExecution;
        return this;
    }

    public void setDebutExecution(ZonedDateTime debutExecution) {
        this.debutExecution = debutExecution;
    }

    public ZonedDateTime getFinExecution() {
        return finExecution;
    }

    public HistExecutionJob finExecution(ZonedDateTime finExecution) {
        this.finExecution = finExecution;
        return this;
    }

    public void setFinExecution(ZonedDateTime finExecution) {
        this.finExecution = finExecution;
    }

    public String getStatut() {
        return statut;
    }

    public HistExecutionJob statut(String statut) {
        this.statut = statut;
        return this;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getMessageErreur() {
        return messageErreur;
    }

    public HistExecutionJob messageErreur(String messageErreur) {
        this.messageErreur = messageErreur;
        return this;
    }

    public void setMessageErreur(String messageErreur) {
        this.messageErreur = messageErreur;
    }

    public String getErreurLog() {
        return erreurLog;
    }

    public HistExecutionJob erreurLog(String erreurLog) {
        this.erreurLog = erreurLog;
        return this;
    }

    public void setErreurLog(String erreurLog) {
        this.erreurLog = erreurLog;
    }

    public PtiJobDetails getPtiJobDetail() {
        return ptiJobDetail;
    }

    public HistExecutionJob ptiJobDetail(PtiJobDetails ptiJobDetails) {
        this.ptiJobDetail = ptiJobDetails;
        return this;
    }

    public void setPtiJobDetail(PtiJobDetails ptiJobDetails) {
        this.ptiJobDetail = ptiJobDetails;
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
        HistExecutionJob histExecutionJob = (HistExecutionJob) o;
        if (histExecutionJob.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), histExecutionJob.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "HistExecutionJob{" +
            "id=" + getId() +
            ", typeTrigger='" + getTypeTrigger() + "'" +
            ", debutExecution='" + getDebutExecution() + "'" +
            ", finExecution='" + getFinExecution() + "'" +
            ", statut='" + getStatut() + "'" +
            ", messageErreur='" + getMessageErreur() + "'" +
            ", erreurLog='" + getErreurLog() + "'" +
            "}";
    }
}
