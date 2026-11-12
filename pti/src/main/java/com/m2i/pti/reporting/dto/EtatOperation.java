package com.m2i.pti.reporting.dto;

import com.m2i.pti.domain.enumeration.StatutOperation;

public class EtatOperation {
    public StatutOperation type;
    public Long idChargement;
    public String format;
    public String statut;
    public String typeChargement;
    public StatutOperation getType() {
        return type;
    }

    public Long getIdChargement() {
        return idChargement;
    }

    public void setIdChargement(Long idChargement) {
        this.idChargement = idChargement;
    }

    public void setType(StatutOperation type) {
        this.type = type;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getStatut() {
        return this.statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getTypeChargement() {
        return typeChargement;
    }

    public void setTypeChargement(String typeChargement) {
        this.typeChargement = typeChargement;
    }
    

}
