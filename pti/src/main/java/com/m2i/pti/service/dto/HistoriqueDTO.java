package com.m2i.pti.service.dto;

import com.m2i.pti.domain.enumeration.HistoryType;

import java.time.Instant;
import java.time.ZonedDateTime;

public class HistoriqueDTO {
    public Long id;
    private String fournisseurService;
    private ZonedDateTime dateHistorique;
    public HistoryType etiquette;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFournisseurService() {
        return fournisseurService;
    }

    public void setFournisseurService(String fournisseurService) {
        this.fournisseurService = fournisseurService;
    }

    public ZonedDateTime getDateHistorique() {
        return dateHistorique;
    }

    public void setDateHistorique(ZonedDateTime dateHistorique) {
        this.dateHistorique = dateHistorique;
    }

    public HistoryType getEtiquette() {
        return etiquette;
    }

    public void setEtiquette(HistoryType etiquette) {
        this.etiquette = etiquette;
    }
}
