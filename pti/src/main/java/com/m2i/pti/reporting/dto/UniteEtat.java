package com.m2i.pti.reporting.dto;

import java.time.ZonedDateTime;

public class UniteEtat {
    public String adresse;
    public Long pereUoId;
    public Long groupeId;
    public Long villeId;
    public String typeUOLibelleTypeUO;
    public Integer numeroOrdre;
    public String codeUO;
    public String pereUOId;
    public String libelleUniteOrganisation;
    public String typeUO;
    public String format;
    public Long reglementId;
    private String type;
    ZonedDateTime dateDebut;
    ZonedDateTime dateFin;


    public String getTypeUO() {
        return typeUO;
    }

    public void setTypeUO(String typeUO) {
        this.typeUO = typeUO;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public Long getPereUoId() {
        return pereUoId;
    }

    public void setPereUoId(Long pereUoId) {
        this.pereUoId = pereUoId;
    }

    public Long getGroupeId() {
        return groupeId;
    }

    public void setGroupeId(Long groupeId) {
        this.groupeId = groupeId;
    }

    public Long getVilleId() {
        return villeId;
    }

    public void setVilleId(Long villeId) {
        this.villeId = villeId;
    }

    public String getTypeUOLibelleTypeUO() {
        return typeUOLibelleTypeUO;
    }

    public void setTypeUOLibelleTypeUO(String typeUOLibelleTypeUO) {
        this.typeUOLibelleTypeUO = typeUOLibelleTypeUO;
    }

    public Integer getNumeroOrdre() {
        return numeroOrdre;
    }

    public void setNumeroOrdre(Integer numeroOrdre) {
        this.numeroOrdre = numeroOrdre;
    }

    public String getCodeUO() {
        return codeUO;
    }

    public void setCodeUO(String codeUO) {
        this.codeUO = codeUO;
    }

    public String getPereUOId() {
        return pereUOId;
    }

    public void setPereUOId(String pereUOId) {
        this.pereUOId = pereUOId;
    }

    public String getLibelleUniteOrganisation() {
        return libelleUniteOrganisation;
    }

    public void setLibelleUniteOrganisation(String libelleUniteOrganisation) {
        this.libelleUniteOrganisation = libelleUniteOrganisation;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public Long getReglementId() {
        return reglementId;
    }

    public void setReglementId(Long reglementId) {
        this.reglementId = reglementId;
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
