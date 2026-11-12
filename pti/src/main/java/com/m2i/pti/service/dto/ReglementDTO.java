package com.m2i.pti.service.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Objects;

public class ReglementDTO {
    private Long id;
    private ZonedDateTime dateReglement;
    private Boolean statut;
    private String createdBy;
    private Instant createdDate;
    private String validerPar;
    private Boolean estAnnuler;
  private Double montantTotal ;
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ZonedDateTime getDateReglement() {
        return dateReglement;
    }

    public void setDateReglement(ZonedDateTime dateReglement) {
        this.dateReglement = dateReglement;
    }

    public Boolean getStatut() {
        return statut;
    }

    public void setStatut(Boolean statut) {
        this.statut = statut;
    }

    public String getValiderPar() {
        return validerPar;
    }

    public void setValiderPar(String validerPar) {
        this.validerPar = validerPar;
    }

    public Boolean getEstAnnuler() {
        return estAnnuler;
    }

    public void setEstAnnuler(Boolean estAnnuler) {
        this.estAnnuler = estAnnuler;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }
    public Double getMontantTotal() {
        return montantTotal;
    }

    public void setMontantTotal(Double montantTotal) {
        this.montantTotal = montantTotal;
    }


    @Override
    public String toString() {
        return "ReglementDTO{" +
                "id=" + id +
                ", dateReglement=" + dateReglement +
                ", statut=" + statut +
                ", createdBy='" + createdBy + '\'' +
                '}';
    }
}
