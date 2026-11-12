package com.m2i.pti.reporting.dto;


import com.m2i.pti.domain.enumeration.StatutOperation;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public class EtatParams {
    Long typeServiceId;
    Long uoConnecteId;
    Long uoSourceId;
    Long uoPaysId;
    ZonedDateTime dateDebut;
    ZonedDateTime dateFin;
    Long canalId;
    Long uoAcquereurId;
    BigDecimal montatOp;
    StatutOperation statutOperation;
    Boolean espPaye;
    Long operationId;
     String  codeOperation;
    String format;
    String typeClient;
    String statut;
    String statutCompte;
    String libelle;
    String typeAgent;
    String typeEvaluation;

    public Long getOperationId() {
        return operationId;
    }

    public void setOperationId(Long operationId) {
        this.operationId = operationId;
    }

    public Long getUoSourceId() {
        return uoSourceId;
    }

    public void setUoSourceId(Long uoSourceId) {
        this.uoSourceId = uoSourceId;
    }

    public Boolean getEspPaye() {
        return espPaye;
    }

    public void setEspPaye(Boolean espPaye) {
        this.espPaye = espPaye;
    }

    public Long getCanalId() {
        return canalId;
    }

    public void setCanalId(Long canalId) {
        this.canalId = canalId;
    }

    public Long getUoAcquereurId() {
        return uoAcquereurId;
    }

    public void setUoAcquereurId(Long uoAcquereurId) {
        this.uoAcquereurId = uoAcquereurId;
    }

    public BigDecimal getMontatOp() {
        return montatOp;
    }

    public void setMontatOp(BigDecimal montatOp) {
        this.montatOp = montatOp;
    }

    public StatutOperation getStatutOperation() {
        return statutOperation;
    }

    public void setStatutOperation(StatutOperation statutOperation) {
        this.statutOperation = statutOperation;
    }

    public Long getTypeServiceId() {
        return typeServiceId;
    }

    public void setTypeServiceId(Long typeServiceId) {
        this.typeServiceId = typeServiceId;
    }

    public Long getUoConnecteId() {
        return uoConnecteId;
    }

    public void setUoConnecteId(Long uoConnecteId) {
        this.uoConnecteId = uoConnecteId;
    }

    public Long getUoPaysId() {
        return uoPaysId;
    }

    public void setUoPaysId(Long uoPaysId) {
        this.uoPaysId = uoPaysId;
    }

    public ZonedDateTime getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(ZonedDateTime dateDebut) {
        this.dateDebut = dateDebut;
    }

    public ZonedDateTime getDateFin() {
        return dateFin;
    }

    public void setDateFin(ZonedDateTime dateFin) {
        this.dateFin = dateFin;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getTypeClient() {
        return typeClient;
    }

    public void setTypeClient(String typeClient) {
        this.typeClient = typeClient;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public String getStatutCompte() {
        return statutCompte;
    }

    public void setStatutCompte(String statutCompte) {
        this.statutCompte = statutCompte;
    }

    public String getTypeAgent() {
        return typeAgent;
    }

    public void setTypeAgent(String typeAgent) {
        this.typeAgent = typeAgent;
    }

    public String getTypeEvaluation() {
        return typeEvaluation;
    }

    public void setTypeEvaluation(String typeEvaluation) {
        this.typeEvaluation = typeEvaluation;
    }

    public String getCodeOperation() {
        return codeOperation;
    }

    public void setCodeOperation(String codeOperation) {
        this.codeOperation = codeOperation;
    }
    
    
}
