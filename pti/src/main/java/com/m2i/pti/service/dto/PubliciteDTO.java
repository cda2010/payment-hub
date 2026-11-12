package com.m2i.pti.service.dto;


import java.io.Serializable;
import java.time.LocalDate;

import com.m2i.pti.domain.Pays;
import java.time.LocalDateTime;

/**
 * A DTO for the Pays entity.
 */
public class PubliciteDTO implements Serializable {

    private Long id;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private Integer frequence;  
        private Integer actif;  

    private Pays pays;
    private Long paysId;
    private String libellePays;
   // private MultipartFile urlMinioPublicite;
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public LocalDate getDateDebut() {
        return dateDebut;
    }
    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }
    public LocalDate getDateFin() {
        return dateFin;
    }
    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }
    public Integer getFrequence() {
        return frequence;
    }
    public void setFrequence(Integer frequence) {
        this.frequence = frequence;
    }
    public Pays getPays() {
        return pays;
    }
    public void setPays(Pays pays) {
        this.pays = pays;
    }
    public Long getPaysId() {
        return paysId;
    }
    public void setPaysId(Long paysId) {
        this.paysId = paysId;
    }
    public String getLibellePays() {
        return libellePays;
    }
    public void setLibellePays(String libellePays) {
        this.libellePays = libellePays;
    }
   // public MultipartFile getUrlMinioPublicite() {
   //     return urlMinioPublicite;
   // }
   // public void setUrlMinioPublicite(MultipartFile urlMinioPublicite) {
  //      this.urlMinioPublicite = urlMinioPublicite;
  //  }

    public Integer getActif() {
        return actif;
    }

    public void setActif(Integer actif) {
        this.actif = actif;
    }

    
}
