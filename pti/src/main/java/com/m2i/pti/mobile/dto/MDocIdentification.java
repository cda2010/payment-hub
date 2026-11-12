package com.m2i.pti.mobile.dto;

import java.time.LocalDate;

public class MDocIdentification {
  
    String reference;
    MTypePiece  typePiece;
 private LocalDate dateDelivrance;
 private LocalDate dateExpiration;
   
 private String lieuDelivrance;
    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public MTypePiece getTypePiece() {
        return typePiece;
    }

    public void setTypePiece(MTypePiece typePiece) {
        this.typePiece = typePiece;
    }

    public LocalDate getDateDelivrance() {
        return dateDelivrance;
    }

    public void setDateDelivrance(LocalDate dateDelivrance) {
        this.dateDelivrance = dateDelivrance;
    }

    public LocalDate getDateExpiration() {
        return dateExpiration;
    }

    public void setDateExpiration(LocalDate dateExpiration) {
        this.dateExpiration = dateExpiration;
    }

    public String getLieuDelivrance() {
        return lieuDelivrance;
    }

    public void setLieuDelivrance(String lieuDelivrance) {
        this.lieuDelivrance = lieuDelivrance;
    }
    
}
