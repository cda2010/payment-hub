package com.m2i.pti.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.m2i.pti.service.dto.TableauRepartitionCommissionDTO;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

/**
 * A TableauRepartitionCommission.
 */

@SqlResultSetMappings(value = {
        @SqlResultSetMapping(name = "Mapping.TableauRepartitionCommissionDTO",
                classes = @ConstructorResult(targetClass = TableauRepartitionCommissionDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = Long.class),
                                @ColumnResult(name = "niveaoOperation", type = Integer.class),
                                @ColumnResult(name = "dateDebut", type = LocalDate.class),
                                @ColumnResult(name = "dateFin", type = LocalDate.class),
                                @ColumnResult(name = "typeServiceId", type = Long.class),
                                @ColumnResult(name = "typeServiceLibelle", type = String.class),
                                @ColumnResult(name = "organisationId", type = Long.class),
                                @ColumnResult(name = "organisationLibelle", type = String.class),
                                @ColumnResult(name = "activated", type = Boolean.class),
                                @ColumnResult(name = "groupId", type = Long.class),
                                @ColumnResult(name = "groupLibelle", type = String.class)
                        }))
})


@NamedNativeQueries({
        @NamedNativeQuery(name = "TableauRepartitionCommission.findByGroupeId",
                query = "SELECT tr.id, tr.niveao_operation as niveaoOperation, tr.date_debut as dateDebut, tr.date_fin as dateFin," +
                        "tr.type_service_id as typeServiceId,ts.code as typeServiceLibelle, tr.organisation_id as organisationId," +
                        "uo.libelle_unite_organisation as organisationLibelle, tr.activated as activated, tr.group_id as groupId, g.libelle_group as groupLibelle" +
                        " FROM tableau_repartition_commission tr " +
                        "LEFT JOIN type_service ts on tr.type_service_id=ts.id" +
                        " LEFT JOIN unite_organisation uo on tr.organisation_id=uo.id" +
                        " LEFT JOIN group_repartition g on tr.group_id=g.id where tr.group_id= CAST(CAST (:groupId AS TEXT) AS BIGINT ) limit :taille offset :page",
                resultSetMapping = "Mapping.TableauRepartitionCommissionDTO",hints={@QueryHint(name="org.hibernate.cacheable",value="false")})
})



@Entity
@Table(name = "tableau_repartition_commission")
@Cacheable(value = true)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class TableauRepartitionCommission extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "niveao_operation")
    private Integer niveaoOperation;

    @Column(name = "date_debut")
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    @Column
    private boolean activated = false;


    @OneToMany(mappedBy = "tableauRepartitionCommission")
    @JsonIgnore
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<LigneRepartitionCommission> ligneRepartitionCommissions = new HashSet<>();

    @ManyToOne
    private TypeService typeService;

    @ManyToOne
    private UniteOrganisation organisation;

    @ManyToOne
    private  GroupRepartition group;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public Integer getNiveaoOperation() {
        return niveaoOperation;
    }

    public TableauRepartitionCommission niveaoOperation(Integer niveaoOperation) {
        this.niveaoOperation = niveaoOperation;
        return this;
    }

    public void setNiveaoOperation(Integer niveaoOperation) {
        this.niveaoOperation = niveaoOperation;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public TableauRepartitionCommission dateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
        return this;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public TableauRepartitionCommission dateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
        return this;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }

    public Set<LigneRepartitionCommission> getLigneRepartitionCommissions() {
        return ligneRepartitionCommissions;
    }

    public TableauRepartitionCommission ligneRepartitionCommissions(Set<LigneRepartitionCommission> ligneRepartitionCommissions) {
        this.ligneRepartitionCommissions = ligneRepartitionCommissions;
        return this;
    }

    public TableauRepartitionCommission addLigneRepartitionCommission(LigneRepartitionCommission ligneRepartitionCommission) {
        this.ligneRepartitionCommissions.add(ligneRepartitionCommission);
        ligneRepartitionCommission.setTableauRepartitionCommission(this);
        return this;
    }

    public TableauRepartitionCommission removeLigneRepartitionCommission(LigneRepartitionCommission ligneRepartitionCommission) {
        this.ligneRepartitionCommissions.remove(ligneRepartitionCommission);
        ligneRepartitionCommission.setTableauRepartitionCommission(null);
        return this;
    }

    public void setLigneRepartitionCommissions(Set<LigneRepartitionCommission> ligneRepartitionCommissions) {
        this.ligneRepartitionCommissions = ligneRepartitionCommissions;
    }

    public TypeService getTypeService() {
        return typeService;
    }

    public TableauRepartitionCommission typeService(TypeService typeService) {
        this.typeService = typeService;
        return this;
    }

    public void setTypeService(TypeService typeService) {
        this.typeService = typeService;
    }

    public UniteOrganisation getOrganisation() {
        return organisation;
    }

    public void setOrganisation(UniteOrganisation organisation) {
        this.organisation = organisation;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    public GroupRepartition getGroup() {
        return group;
    }

    public void setGroup(GroupRepartition group) {
        this.group = group;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TableauRepartitionCommission tableauRepartitionCommission = (TableauRepartitionCommission) o;
        if (tableauRepartitionCommission.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), tableauRepartitionCommission.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "TableauRepartitionCommission{" +
            "id=" + getId() +
            ", niveaoOperation=" + getNiveaoOperation() +
            ", dateDebut='" + getDateDebut() + "'" +
            ", dateFin='" + getDateFin() + "'" +
            "}";
    }
}
