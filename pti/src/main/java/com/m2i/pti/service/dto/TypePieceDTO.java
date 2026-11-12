package com.m2i.pti.service.dto;


import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

/**
 * A DTO for the TypePiece entity.
 */
public class TypePieceDTO implements Serializable {

    private Long id;

    @NotNull
    private String codeTypePiece;

    @NotNull
    private String libelleTypePiece;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodeTypePiece() {
        return codeTypePiece;
    }

    public void setCodeTypePiece(String codeTypePiece) {
        this.codeTypePiece = codeTypePiece;
    }

    public String getLibelleTypePiece() {
        return libelleTypePiece;
    }

    public void setLibelleTypePiece(String libelleTypePiece) {
        this.libelleTypePiece = libelleTypePiece;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TypePieceDTO typePieceDTO = (TypePieceDTO) o;
        if(typePieceDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), typePieceDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "TypePieceDTO{" +
            "id=" + getId() +
            ", codeTypePiece='" + getCodeTypePiece() + "'" +
            ", libelleTypePiece='" + getLibelleTypePiece() + "'" +
            "}";
    }
}
