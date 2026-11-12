package com.m2i.pti.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

/**
 * A TypePiece.
 */
@Entity
@Table(name = "type_piece")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class TypePiece extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "code_type_piece", nullable = false)
    private String codeTypePiece;

    @NotNull
    @Column(name = "libelle_type_piece", nullable = false)
    private String libelleTypePiece;

    
    
     @Column(name = "etiquette")
    private String etiquette;
     
    @OneToMany(mappedBy = "typePiece")
    @JsonIgnore
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<Piece> pieces = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodeTypePiece() {
        return codeTypePiece;
    }

    public TypePiece codeTypePiece(String codeTypePiece) {
        this.codeTypePiece = codeTypePiece;
        return this;
    }

    public void setCodeTypePiece(String codeTypePiece) {
        this.codeTypePiece = codeTypePiece;
    }

    public String getLibelleTypePiece() {
        return libelleTypePiece;
    }

    public TypePiece libelleTypePiece(String libelleTypePiece) {
        this.libelleTypePiece = libelleTypePiece;
        return this;
    }

    public void setLibelleTypePiece(String libelleTypePiece) {
        this.libelleTypePiece = libelleTypePiece;
    }

    public Set<Piece> getPieces() {
        return pieces;
    }

    public TypePiece pieces(Set<Piece> pieces) {
        this.pieces = pieces;
        return this;
    }

    public TypePiece addPiece(Piece piece) {
        this.pieces.add(piece);
        piece.setTypePiece(this);
        return this;
    }

    public TypePiece removePiece(Piece piece) {
        this.pieces.remove(piece);
        piece.setTypePiece(null);
        return this;
    }

    public void setPieces(Set<Piece> pieces) {
        this.pieces = pieces;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    public String getEtiquette() {
        return etiquette;
    }

    public void setEtiquette(String etiquette) {
        this.etiquette = etiquette;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TypePiece typePiece = (TypePiece) o;
        if (typePiece.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), typePiece.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "TypePiece{" +
            "id=" + getId() +
            ", codeTypePiece='" + getCodeTypePiece() + "'" +
            ", libelleTypePiece='" + getLibelleTypePiece() + "'" +
            "}";
    }
}
