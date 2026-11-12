package com.m2i.pti.service.dto;


import com.m2i.pti.domain.ParamExecutionJob;
import java.time.ZonedDateTime;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

/**
 * A DTO for the PtiJobDetails entity.
 */
public class PtiJobDetailsDTO implements Serializable {

    private Long id;

    private String jobName;

    private String displayName;

    private String expressionCron;

    private Integer priorite;

    private Integer schedulerGroupe;

    private String nomGroupe;

    private ZonedDateTime dateDerniereExecution;

    private ZonedDateTime dateProchaineExecution;

    private String jobKey;

    private String erreurExecution;

    private Boolean estActif;

    private Boolean estEnExecution;

   private Long fournisseurServiceId;
   private String libelleFse;
 private Set<ParamExecutionJob>  paramExecutionJob;
   private String jobClass;  
    
       private Boolean cronJob;  
          
           private Long repeatTime;  
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getJobName() {
        return jobName;
    }

    public Set<ParamExecutionJob> getParamExecutionJob() {
        return paramExecutionJob;
    }

    public void setParamExecutionJob(Set<ParamExecutionJob> paramExecutionJob) {
        this.paramExecutionJob = paramExecutionJob;
    }

    

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getExpressionCron() {
        return expressionCron;
    }

    public void setExpressionCron(String expressionCron) {
        this.expressionCron = expressionCron;
    }

    public Integer getPriorite() {
        return priorite;
    }

    public void setPriorite(Integer priorite) {
        this.priorite = priorite;
    }

    public Integer getSchedulerGroupe() {
        return schedulerGroupe;
    }

    public void setSchedulerGroupe(Integer schedulerGroupe) {
        this.schedulerGroupe = schedulerGroupe;
    }

    public String getNomGroupe() {
        return nomGroupe;
    }

    public void setNomGroupe(String nomGroupe) {
        this.nomGroupe = nomGroupe;
    }

    public ZonedDateTime getDateDerniereExecution() {
        return dateDerniereExecution;
    }

    public void setDateDerniereExecution(ZonedDateTime dateDerniereExecution) {
        this.dateDerniereExecution = dateDerniereExecution;
    }

    public ZonedDateTime getDateProchaineExecution() {
        return dateProchaineExecution;
    }

    public void setDateProchaineExecution(ZonedDateTime dateProchaineExecution) {
        this.dateProchaineExecution = dateProchaineExecution;
    }

    public String getJobKey() {
        return jobKey;
    }

    public void setJobKey(String jobKey) {
        this.jobKey = jobKey;
    }

    public String getErreurExecution() {
        return erreurExecution;
    }

    public void setErreurExecution(String erreurExecution) {
        this.erreurExecution = erreurExecution;
    }

    public Boolean isEstActif() {
        return estActif;
    }

    public void setEstActif(Boolean estActif) {
        this.estActif = estActif;
    }

    public Boolean isEstEnExecution() {
        return estEnExecution;
    }

    public void setEstEnExecution(Boolean estEnExecution) {
        this.estEnExecution = estEnExecution;
    }

    public Boolean getEstActif() {
        return estActif;
    }

    public Boolean getEstEnExecution() {
        return estEnExecution;
    }

    public Long getFournisseurServiceId() {
        return fournisseurServiceId;
    }

    public void setFournisseurServiceId(Long fournisseurServiceId) {
        this.fournisseurServiceId = fournisseurServiceId;
    }

    public String getLibelleFse() {
        return libelleFse;
    }

    public void setLibelleFse(String libelleFse) {
        this.libelleFse = libelleFse;
    }

    public String getJobClass() {
        return jobClass;
    }

    public void setJobClass(String jobClass) {
        this.jobClass = jobClass;
    }

    public Boolean getCronJob() {
        return cronJob;
    }

    public void setCronJob(Boolean cronJob) {
        this.cronJob = cronJob;
    }

    public Long getRepeatTime() {
        return repeatTime;
    }

    public void setRepeatTime(Long repeatTime) {
        this.repeatTime = repeatTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PtiJobDetailsDTO ptiJobDetailsDTO = (PtiJobDetailsDTO) o;
        if(ptiJobDetailsDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), ptiJobDetailsDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "PtiJobDetailsDTO{" +
            "id=" + getId() +
            ", jobName='" + getJobName() + "'" +
            ", displayName='" + getDisplayName() + "'" +
            ", expressionCron='" + getExpressionCron() + "'" +
            ", priorite='" + getPriorite() + "'" +
            ", schedulerGroupe='" + getSchedulerGroupe() + "'" +
            ", nomGroupe='" + getNomGroupe() + "'" +
            ", dateDerniereExecution='" + getDateDerniereExecution() + "'" +
            ", dateProchaineExecution='" + getDateProchaineExecution() + "'" +
            ", jobKey='" + getJobKey() + "'" +
            ", erreurExecution='" + getErreurExecution() + "'" +
            ", estActif='" + isEstActif() + "'" +
            ", estEnExecution='" + isEstEnExecution() + "'" +
           // ", fournisseurServiceId='" + getfournisseurServiceId() + "'" +
            "}";
    }
}
