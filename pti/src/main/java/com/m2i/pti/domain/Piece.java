package com.m2i.pti.domain;

import com.m2i.pti.service.dto.PieceDTO;
import com.m2i.pti.service.dto.TypeServiceDTO;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A Piece.
 */
@NamedNativeQueries({

        @NamedNativeQuery(name = "Piece.findTopByTitulaireId",
                query = "select p.id, reference_piece referencePiece,date_delivrance dateDelivrance,date_expiration dateExpiration,copie_content_type copieContentType, "
                        + "lieu_delivrance lieuDelivrance,titulaire_id  titulaireId, "
                        + " t.id typePieceId ,t.libelle_type_piece typePieceLibelleTypePiece  from piece p inner join type_piece t on p.type_piece_id=t.id "
                        + " where  titulaire_id=:titulaireId",
                resultSetMapping = "Mapping.PieceDTO",hints={@QueryHint(name="org.hibernate.cacheable",value="false")}),


})

@SqlResultSetMappings({
        @SqlResultSetMapping(name = "Mapping.PieceDTO",
                classes = @ConstructorResult(targetClass = PieceDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = Long.class),
                                @ColumnResult(name = "referencePiece", type = String.class),
                                @ColumnResult(name = "dateDelivrance", type = LocalDate.class),
                                @ColumnResult(name = "dateExpiration", type = LocalDate.class),
                                @ColumnResult(name = "copieContentType", type = String.class),
                                @ColumnResult(name = "lieuDelivrance", type = String.class),
                                @ColumnResult(name = "titulaireId", type = Long.class),
                                @ColumnResult(name = "typePieceId", type = Long.class),
                                @ColumnResult(name = "typePieceLibelleTypePiece", type = String.class)

                        }))

})


@Entity
@Table(name = "piece")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Piece extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "reference_piece", nullable = false)
    private String referencePiece;

    @Column(name = "date_delivrance")
    private LocalDate dateDelivrance;

    @Column(name = "date_expiration")
    private LocalDate dateExpiration;

    @Lob
    @Column(name = "copie")
    private byte[] copie;

    @Column(name = "copie_content_type")
    private String copieContentType;

    @Column(name = "lieu_delivrance")
    private String lieuDelivrance;

    @ManyToOne
    private Personne titulaire;

    @ManyToOne
    private TypePiece typePiece;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReferencePiece() {
        return referencePiece;
    }

    public Piece referencePiece(String referencePiece) {
        this.referencePiece = referencePiece;
        return this;
    }

    public void setReferencePiece(String referencePiece) {
        this.referencePiece = referencePiece;
    }

    public LocalDate getDateDelivrance() {
        return dateDelivrance;
    }

    public Piece dateDelivrance(LocalDate dateDelivrance) {
        this.dateDelivrance = dateDelivrance;
        return this;
    }

    public void setDateDelivrance(LocalDate dateDelivrance) {
        this.dateDelivrance = dateDelivrance;
    }

    public LocalDate getDateExpiration() {
        return dateExpiration;
    }

    public Piece dateExpiration(LocalDate dateExpiration) {
        this.dateExpiration = dateExpiration;
        return this;
    }

    public void setDateExpiration(LocalDate dateExpiration) {
        this.dateExpiration = dateExpiration;
    }

    public byte[] getCopie() {
        return copie;
    }

    public Piece copie(byte[] copie) {
        this.copie = copie;
        return this;
    }

    public void setCopie(byte[] copie) {
        this.copie = copie;
    }

    public String getCopieContentType() {
        return copieContentType;
    }

    public Piece copieContentType(String copieContentType) {
        this.copieContentType = copieContentType;
        return this;
    }

    public void setCopieContentType(String copieContentType) {
        this.copieContentType = copieContentType;
    }

    public String getLieuDelivrance() {
        return lieuDelivrance;
    }

    public Piece lieuDelivrance(String lieuDelivrance) {
        this.lieuDelivrance = lieuDelivrance;
        return this;
    }

    public void setLieuDelivrance(String lieuDelivrance) {
        this.lieuDelivrance = lieuDelivrance;
    }

    public Personne getTitulaire() {
        return titulaire;
    }

    public Piece titulaire(Personne personne) {
        this.titulaire = personne;
        return this;
    }

    public void setTitulaire(Personne personne) {
        this.titulaire = personne;
    }

    public TypePiece getTypePiece() {
        return typePiece;
    }

    public Piece typePiece(TypePiece typePiece) {
        this.typePiece = typePiece;
        return this;
    }

    public void setTypePiece(TypePiece typePiece) {
        this.typePiece = typePiece;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Piece piece = (Piece) o;
        if (piece.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), piece.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "Piece{" +
            "id=" + getId() +
            ", referencePiece='" + getReferencePiece() + "'" +
            ", dateDelivrance='" + getDateDelivrance() + "'" +
            ", dateExpiration='" + getDateExpiration() + "'" +
            ", copie='" + getCopie() + "'" +
            ", copieContentType='" + getCopieContentType() + "'" +
            ", lieuDelivrance='" + getLieuDelivrance() + "'" +
                ", type piece='"+ getTypePiece()+ "'"+
            "}";
    }
}
