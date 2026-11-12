package com.m2i.pti.service.dto;


import java.time.ZonedDateTime;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

/**
 * A DTO for the HistExecutionJob entity.
 */
public class HistExecutionJobDTO implements Serializable {

    private Long id;

    private String typeTrigger;

    private ZonedDateTime debutExecution;

    private ZonedDateTime finExecution;

    private String statut;

    private String messageErreur;

    private String erreurLog;

    private Long ptiJobDetailId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTypeTrigger() {
        return typeTrigger;
    }

    public void setTypeTrigger(String typeTrigger) {
        this.typeTrigger = typeTrigger;
    }

    public ZonedDateTime getDebutExecution() {
        return debutExecution;
    }

    public void setDebutExecution(ZonedDateTime debutExecution) {
        this.debutExecution = debutExecution;
    }

    public ZonedDateTime getFinExecution() {
        return finExecution;
    }

    public void setFinExecution(ZonedDateTime finExecution) {
        this.finExecution = finExecution;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getMessageErreur() {
        return messageErreur;
    }

    public void setMessageErreur(String messageErreur) {
        this.messageErreur = messageErreur;
    }

    public String getErreurLog() {
        return erreurLog;
    }

    public void setErreurLog(String erreurLog) {
        this.erreurLog = erreurLog;
    }

    public Long getPtiJobDetailId() {
        return ptiJobDetailId;
    }

    public void setPtiJobDetailId(Long ptiJobDetailsId) {
        this.ptiJobDetailId = ptiJobDetailsId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        HistExecutionJobDTO histExecutionJobDTO = (HistExecutionJobDTO) o;
        if(histExecutionJobDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), histExecutionJobDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "HistExecutionJobDTO{" +
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
