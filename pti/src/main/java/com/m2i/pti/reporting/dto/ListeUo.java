package com.m2i.pti.reporting.dto;

import autovalue.shaded.com.google$.common.collect.$PeekingIterator;

import java.time.ZonedDateTime;
import java.util.List;

public class ListeUo {
    private List<Long> agentIds;
    private Long pereId;
    private String format;
    private ZonedDateTime dateDebut;
    private ZonedDateTime dateFin;
    private String type;

    public List<Long> getAgentIds() {
        return agentIds;
    }

    public void setAgentIds(List<Long> agentIds) {
        this.agentIds = agentIds;
    }

    public Long getPereId() {
        return pereId;
    }

    public void setPereId(Long pereId) {
        this.pereId = pereId;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
