package com.m2i.pti.reporting.dto;

import java.time.ZonedDateTime;

public class XDataUnique {
    Long compteAutreId;
    String format;
    String type;
    ZonedDateTime dateDebut;
    ZonedDateTime dateFin;

    public Long getCompteAutreId() {
        return compteAutreId;
    }

    public void setCompteAutreId(Long compteAutreId) {
        this.compteAutreId = compteAutreId;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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
}
