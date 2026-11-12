package com.m2i.pti.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

/**
 * A PtiJobDetails.
 */
@Entity
@Table(name = "pti_job_details")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class PtiJobDetails extends AbstractAuditingEntity implements Serializable, Job {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_name",unique = true)
    private String jobName;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "expression_cron")
    private String expressionCron;

    @Column(name = "priorite")
    private Integer priorite;

    @Column(name = "scheduler_groupe")
    private Integer schedulerGroupe;

    @Column(name = "nom_groupe")
    private String nomGroupe;

    @Column(name = "date_derniere_execution")
    private ZonedDateTime dateDerniereExecution;

    @Column(name = "date_prochaine_execution")
    private ZonedDateTime dateProchaineExecution;

    @Column(name = "argument")
    private String argument;
    
    
    @Column(name = "job_key")
    private String jobKey;

    @Column(name = "erreur_execution")
    private String erreurExecution;

    @Column(name = "est_actif")
    private Boolean estActif;

    @Column(name = "est_en_execution")
    private Boolean estEnExecution;
      @Column(name = "job_class")
   private String jobClass;  
        @Column(name = "cron_job")
       private Boolean cronJob;  
            @Column(name = "repeat_time")
           private Long repeatTime;  
    @ManyToOne
    FournisseurService fournisseurService;

    @OneToMany(mappedBy = "ptiJobDetail")
    @JsonIgnore
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<HistExecutionJob> histExecutionJobs = new HashSet<>();
    
    
    @OneToMany(mappedBy = "ptiJobDetail")
    @JsonIgnore
   // @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<ParamExecutionJob> paramExecutionJob = new HashSet<>();

    public Set<ParamExecutionJob> getParamExecutionJob() {
        return paramExecutionJob;
    }

    public void setParamExecutionJob(Set<ParamExecutionJob> paramExecutionJob) {
        this.paramExecutionJob = paramExecutionJob;
    }
    
  
    public Long getRepeatTime() {
        return repeatTime;
    }

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public void setRepeatTime(Long repeatTime) {  
        this.repeatTime = repeatTime;
    }

    public Long getId() {
        return id;
    }

    public Boolean getCronJob() {
        return cronJob;
    }

    public void setCronJob(Boolean cronJob) {
        this.cronJob = cronJob;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getJobName() {
        return jobName;
    }

    public String getJobClass() {
        return jobClass;
    }

      public PtiJobDetails jobClass(String jobClass) {
        this.jobClass = jobClass;
        return this;
    }
    public void setJobClass(String jobClass) {
        this.jobClass = jobClass;
    }

    public PtiJobDetails jobName(String jobName) {
        this.jobName = jobName;
        return this;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public PtiJobDetails displayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getExpressionCron() {
        return expressionCron;
    }

    public PtiJobDetails expressionCron(String expressionCron) {
        this.expressionCron = expressionCron;
        return this;
    }

    public void setExpressionCron(String expressionCron) {
        this.expressionCron = expressionCron;
    }

    public Integer getPriorite() {
        return priorite;
    }

    public PtiJobDetails priorite(Integer priorite) {
        this.priorite = priorite;
        return this;
    }

    public void setPriorite(Integer priorite) {
        this.priorite = priorite;
    }

    public Integer getSchedulerGroupe() {
        return schedulerGroupe;
    }

    public PtiJobDetails schedulerGroupe(Integer schedulerGroupe) {
        this.schedulerGroupe = schedulerGroupe;
        return this;
    }

    public void setSchedulerGroupe(Integer schedulerGroupe) {
        this.schedulerGroupe = schedulerGroupe;
    }

    public String getNomGroupe() {
        return nomGroupe;
    }

    public PtiJobDetails nomGroupe(String nomGroupe) {
        this.nomGroupe = nomGroupe;
        return this;
    }

    public void setNomGroupe(String nomGroupe) {
        this.nomGroupe = nomGroupe;
    }

    public ZonedDateTime getDateDerniereExecution() {
        return dateDerniereExecution;
    }

    public PtiJobDetails dateDerniereExecution(ZonedDateTime dateDerniereExecution) {
        this.dateDerniereExecution = dateDerniereExecution;
        return this;
    }

    public void setDateDerniereExecution(ZonedDateTime dateDerniereExecution) {
        this.dateDerniereExecution = dateDerniereExecution;
    }

    public ZonedDateTime getDateProchaineExecution() {
        return dateProchaineExecution;
    }

    public PtiJobDetails dateProchaineExecution(ZonedDateTime dateProchaineExecution) {
        this.dateProchaineExecution = dateProchaineExecution;
        return this;
    }

    public void setDateProchaineExecution(ZonedDateTime dateProchaineExecution) {
        this.dateProchaineExecution = dateProchaineExecution;
    }

    public String getJobKey() {
        return jobKey;
    }

    public PtiJobDetails jobKey(String jobKey) {
        this.jobKey = jobKey;
        return this;
    }

    public void setJobKey(String jobKey) {
        this.jobKey = jobKey;
    }

    public String getErreurExecution() {
        return erreurExecution;
    }

    public PtiJobDetails erreurExecution(String erreurExecution) {
        this.erreurExecution = erreurExecution;
        return this;
    }

    public void setErreurExecution(String erreurExecution) {
        this.erreurExecution = erreurExecution;
    }

    public Boolean isEstActif() {
        return estActif;
    }

    public PtiJobDetails estActif(Boolean estActif) {
        this.estActif = estActif;
        return this;
    }

    public void setEstActif(Boolean estActif) {
        this.estActif = estActif;
    }

    public Boolean isEstEnExecution() {
        return estEnExecution;
    }

    public PtiJobDetails estEnExecution(Boolean estEnExecution) {
        this.estEnExecution = estEnExecution;
        return this;
    }

    public void setEstEnExecution(Boolean estEnExecution) {
        this.estEnExecution = estEnExecution;
    }

    public FournisseurService getFournisseurService() {
        return fournisseurService;
    }

    public PtiJobDetails fournisseurService(FournisseurService fournisseurService) {
        this.fournisseurService = fournisseurService;
        return this;
    }

    public void setFournisseurService(FournisseurService fournisseurService) {
        this.fournisseurService = fournisseurService;
    }

    public Set<HistExecutionJob> getHistExecutionJobs() {
        return histExecutionJobs;
    }

    public PtiJobDetails histExecutionJobs(Set<HistExecutionJob> histExecutionJobs) {
        this.histExecutionJobs = histExecutionJobs;
        return this;
    }

    public PtiJobDetails addHistExecutionJob(HistExecutionJob histExecutionJob) {
        this.histExecutionJobs.add(histExecutionJob);
        histExecutionJob.setPtiJobDetail(this);
        return this;
    }

    public PtiJobDetails removeHistExecutionJob(HistExecutionJob histExecutionJob) {
        this.histExecutionJobs.remove(histExecutionJob);
        histExecutionJob.setPtiJobDetail(null);
        return this;
    }

    public void setHistExecutionJobs(Set<HistExecutionJob> histExecutionJobs) {
        this.histExecutionJobs = histExecutionJobs;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    public String getArgument() {
        return argument;
    }

    public void setArgument(String argument) {
        this.argument = argument;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PtiJobDetails ptiJobDetails = (PtiJobDetails) o;
        if (ptiJobDetails.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), ptiJobDetails.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "PtiJobDetails{" +
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
           // ", fournisseurService='" + getFournisseurService() + "'" +
            "}";
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        System.out.println("ça marche à merveille !!!!!!");
    }
}
