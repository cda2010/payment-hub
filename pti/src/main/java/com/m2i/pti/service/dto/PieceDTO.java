package com.m2i.pti.service.dto;


import java.time.LocalDate;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;
import jakarta.persistence.Lob;

/**
 * A DTO for the Piece entity.
 */
public class PieceDTO implements Serializable {

    private Long id;

    private String referencePiece;

    private LocalDate dateDelivrance;

    private LocalDate dateExpiration;

    @Lob
    private byte[] copie;
    private String copieContentType;

    private String lieuDelivrance;

    private Long titulaireId;

    private Long typePieceId;

    private String typePieceLibelleTypePiece;

    public PieceDTO() {
    }

    public PieceDTO(Long id, String referencePiece, LocalDate dateDelivrance, LocalDate dateExpiration, String copieContentType, String lieuDelivrance, Long titulaireId, Long typePieceId, String typePieceLibelleTypePiece) {
        this.id = id;
        this.referencePiece = referencePiece;
        this.dateDelivrance = dateDelivrance;
        this.dateExpiration = dateExpiration;
        this.copieContentType = copieContentType;
        this.lieuDelivrance = lieuDelivrance;
        this.titulaireId = titulaireId;
        this.typePieceId = typePieceId;
        this.typePieceLibelleTypePiece = typePieceLibelleTypePiece;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReferencePiece() {
        return referencePiece;
    }

    public void setReferencePiece(String referencePiece) {
        this.referencePiece = referencePiece;
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

    public byte[] getCopie() {
        return copie;
    }

    public void setCopie(byte[] copie) {
        this.copie = copie;
    }

    public String getCopieContentType() {
        return copieContentType;
    }

    public void setCopieContentType(String copieContentType) {
        this.copieContentType = copieContentType;
    }

    public String getLieuDelivrance() {
        return lieuDelivrance;
    }

    public void setLieuDelivrance(String lieuDelivrance) {
        this.lieuDelivrance = lieuDelivrance;
    }

    public Long getTitulaireId() {
        return titulaireId;
    }

    public void setTitulaireId(Long personneId) {
        this.titulaireId = personneId;
    }

    public Long getTypePieceId() {
        return typePieceId;
    }

    public void setTypePieceId(Long typePieceId) {
        this.typePieceId = typePieceId;
    }

    public String getTypePieceLibelleTypePiece() {
        return typePieceLibelleTypePiece;
    }

    public void setTypePieceLibelleTypePiece(String typePieceLibelleTypePiece) {
        this.typePieceLibelleTypePiece = typePieceLibelleTypePiece;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PieceDTO pieceDTO = (PieceDTO) o;
        if(pieceDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), pieceDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "PieceDTO{" +
            "id=" + getId() +
            ", referencePiece='" + getReferencePiece() + "'" +
            ", dateDelivrance='" + getDateDelivrance() + "'" +
            ", dateExpiration='" + getDateExpiration() + "'" +
            ", copie='" + getCopie() + "'" +
            ", lieuDelivrance='" + getLieuDelivrance() + "'" +
            "}";
    }
}
