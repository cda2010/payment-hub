package com.m2i.pti.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.m2i.pti.config.audit.EntityAuditEventListener;
import com.m2i.pti.service.dto.PointVenteDTO;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

/**
 * A PointVente.
 */
@NamedNativeQueries({
    @NamedNativeQuery(name = "PointVente.findByCode",
            query = "SELECT p.id, p.code_point_vente as codePointVente, p.libelle_point_vente as libellePointVente, "
            + " p.unite_organisation_id uniteOrganisationId, u.typeuo_id typeUoId , libelle_unite_organisation libelleUniteOrganisation, code_uo codeUo, tu.code_type_uo typeUOCode, tu.libelle_type_uo typeUOLibelleTypeUO, tu.niveau niveauTypeUo "
            + " FROM point_vente p, unite_organisation u, type_unite_organisation tu WHERE  u.typeuo_id=tu.id  and u.id=p.unite_organisation_id  and code_point_vente = CAST (:codePv AS TEXT)",
            resultSetMapping = "Mapping.PointVenteDTO", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})
    ,
          @NamedNativeQuery(name = "PointVente.findByUoId",
            query = "SELECT p.id, p.code_point_vente as codePointVente, p.libelle_point_vente as libellePointVente, "
            + " p.unite_organisation_id uniteOrganisationId,p.latitude as latitude, p.longitude as longitude,p.est_actif estActif,p.est_sur_card estSurCard "
            + " FROM point_vente p WHERE unite_organisation_id = :uoId",
            resultSetMapping = "Mapping.PointVenteCordOptimiseDTO", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})
    ,
        @NamedNativeQuery(name = "PointVente.findById",
            query = "SELECT p.id, p.code_point_vente as codePointVente, p.libelle_point_vente as libellePointVente, "
            + " p.unite_organisation_id uniteOrganisationId "
            + " FROM point_vente p WHERE id = :id",
            resultSetMapping = "Mapping.PointVenteOptimiseDTO", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})
    ,

        @NamedNativeQuery(name = "PointVente.findPointVenteDTO",
            query = "SELECT pv.id, pv.code_point_vente as codePointVente, pv.libelle_point_vente as libellePointVente, pv.unite_organisation_id as uniteOrganisationId from point_vente pv where pv.id = :id",
            resultSetMapping = "Mapping.PointVenteById")
    ,
        @NamedNativeQuery(name = "PointVente.findNonActivePointVenteDTO",
            query = "SELECT pv.id, pv.code_point_vente as codePointVente, pv.libelle_point_vente as libellePointVente, pv.unite_organisation_id as uniteOrganisationId, pv.latitude as latitude, pv.longitude as longitude from point_vente pv where pv.id not in (select point_vente_id from operation where point_vente_id is not null)",
            resultSetMapping = "Mapping.PointVenteNonActive")
    ,

        @NamedNativeQuery(name = "PointVente.findByUoIdAndCord",
            query = "SELECT p.id, p.code_point_vente as codePointVente, p.libelle_point_vente as libellePointVente, "
            + " p.unite_organisation_id uniteOrganisationId,p.latitude as latitude, p.longitude as longitude,p.est_actif estActif,p.est_sur_card estSurCard "
            + " FROM point_vente p WHERE p.est_actif=true and p.est_sur_card=true and p.latitude ~ '^-?\\d+(\\.|,)\\d+$' and p.longitude ~ '^-?\\d+(\\.|,)\\d+$' and  (cast(replace(p.latitude, ',','.') as decimal) >= cast(:latitudeMin as decimal)) and (cast(replace(p.latitude, ',','.') as decimal) <= cast(:latitudeMax as decimal)) and (cast(replace(p.longitude, ',','.') as decimal) >= cast(:longitudeMin as decimal)) and (cast(replace(p.longitude, ',','.') as decimal) <= cast(:longitudeMax as decimal)) limit 100",
            resultSetMapping = "Mapping.PointVenteCordOptimiseDTO", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")}),})

@SqlResultSetMappings({
    @SqlResultSetMapping(name = "Mapping.PointVenteDTO",
            classes = @ConstructorResult(targetClass = PointVenteDTO.class,
                    columns = {
                        @ColumnResult(name = "id", type = Long.class)
                        ,
                                @ColumnResult(name = "codePointVente", type = String.class)
                        ,
                                @ColumnResult(name = "libellePointVente", type = String.class)
                        ,
                                @ColumnResult(name = "uniteOrganisationId", type = Long.class)
                        ,
                                @ColumnResult(name = "typeUoId", type = Long.class)
                        ,
                                @ColumnResult(name = "libelleUniteOrganisation", type = String.class)
                        ,
                                @ColumnResult(name = "codeUo", type = String.class)
                        ,
                                @ColumnResult(name = "typeUOCode", type = String.class)
                        ,
                                @ColumnResult(name = "niveauTypeUo", type = String.class)
                    }))
    ,
        @SqlResultSetMapping(name = "Mapping.PointVenteOptimiseDTO",
            classes = @ConstructorResult(targetClass = PointVenteDTO.class,
                    columns = {
                        @ColumnResult(name = "id", type = Long.class)
                        ,
                                @ColumnResult(name = "codePointVente", type = String.class)
                        ,
                                @ColumnResult(name = "libellePointVente", type = String.class)
                        ,
                                @ColumnResult(name = "uniteOrganisationId", type = Long.class)
                        ,
                                @ColumnResult(name = "estActif", type = Boolean.class)
                        ,
                                @ColumnResult(name = "estSurCard", type = Boolean.class)
                    }))
    ,

        @SqlResultSetMapping(name = "Mapping.PointVenteById",
            classes = @ConstructorResult(targetClass = PointVenteDTO.class,
                    columns = {
                        @ColumnResult(name = "id", type = Long.class)
                        ,
                                @ColumnResult(name = "codePointVente", type = String.class)
                        ,
                                @ColumnResult(name = "libellePointVente", type = String.class)
                        ,
                                @ColumnResult(name = "uniteOrganisationId", type = Long.class)
                    }
            )
    )
    ,

        @SqlResultSetMapping(name = "Mapping.PointVenteNonActive",
            classes = @ConstructorResult(targetClass = PointVenteDTO.class,
                    columns = {
                        @ColumnResult(name = "id", type = Long.class)
                        ,
                                @ColumnResult(name = "codePointVente", type = String.class)
                        ,
                                @ColumnResult(name = "libellePointVente", type = String.class)
                        ,
                                @ColumnResult(name = "uniteOrganisationId", type = Long.class)
                        ,
                                @ColumnResult(name = "latitude", type = String.class)
                        ,
                                @ColumnResult(name = "longitude", type = String.class)
                    }
            )
    )
    ,

        @SqlResultSetMapping(name = "Mapping.PointVenteCordOptimiseDTO",
            classes = @ConstructorResult(targetClass = PointVenteDTO.class,
                    columns = {
                        @ColumnResult(name = "id", type = Long.class)
                        ,
                                @ColumnResult(name = "codePointVente", type = String.class)
                        ,
                                @ColumnResult(name = "libellePointVente", type = String.class)
                        ,
                                @ColumnResult(name = "uniteOrganisationId", type = Long.class)
                        ,
                                @ColumnResult(name = "longitude", type = String.class)
                        ,
                                @ColumnResult(name = "latitude", type = String.class)
                        ,
                                @ColumnResult(name = "estActif", type = Boolean.class)
                        ,
                                @ColumnResult(name = "estSurCard", type = Boolean.class)

                    })),})

@Entity
@Table(name = "point_vente")
@EntityListeners(value = {EntityAuditEventListener.class})
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Cacheable(value = true)
public class PointVente extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "code_point_vente", nullable = false)
    private String codePointVente;

    @NotNull
    @Column(name = "libelle_point_vente", nullable = false)
    private String libellePointVente;

    @Column(name = "latitude")
    private String latitude;

    @Column(name = "longitude")
    private String longitude;

    @Column(name = "est_actif")
    private Boolean estActif = true;

    @Column(name = "est_sur_card")
    private Boolean estSurCard = false;
    @Column(name = "alias")
    private String alias ;
 
    @ManyToOne
    private UniteOrganisation uniteOrganisation;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodePointVente() {
        return codePointVente;
    }

    public PointVente codePointVente(String codePointVente) {
        this.codePointVente = codePointVente;
        return this;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public void setCodePointVente(String codePointVente) {
        this.codePointVente = codePointVente;
    }

    public String getLibellePointVente() {
        return libellePointVente;
    }

    public PointVente libellePointVente(String libellePointVente) {
        this.libellePointVente = libellePointVente;
        return this;
    }

    public Boolean getEstActif() {
        return estActif;
    }

    public void setEstActif(Boolean estActif) {
        this.estActif = estActif;
    }

    public void setLibellePointVente(String libellePointVente) {
        this.libellePointVente = libellePointVente;
    }

    public UniteOrganisation getUniteOrganisation() {
        return uniteOrganisation;
    }

    public PointVente uniteOrganisation(UniteOrganisation uniteOrganisation) {
        this.uniteOrganisation = uniteOrganisation;
        return this;
    }

    public Boolean getEstSurCard() {
        return estSurCard;
    }

    public void setEstSurCard(Boolean estSurCard) {
        this.estSurCard = estSurCard;
    }

    public void setUniteOrganisation(UniteOrganisation uniteOrganisation) {
        this.uniteOrganisation = uniteOrganisation;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PointVente pointVente = (PointVente) o;
        if (pointVente.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), pointVente.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "PointVente{"
                + "id=" + getId()
                + ", codePointVente='" + getCodePointVente() + "'"
                + ", libellePointVente='" + getLibellePointVente() + "'"
                + "}";
    }
}
