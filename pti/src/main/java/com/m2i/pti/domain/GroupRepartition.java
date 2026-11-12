package com.m2i.pti.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.m2i.pti.service.dto.GroupRepartitionDTO;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A GroupRepartition.
 */
@SqlResultSetMappings(value = {
        @SqlResultSetMapping(name = "Mapping.GroupRepartitionDTO",
                classes = @ConstructorResult(targetClass = GroupRepartitionDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = Long.class),
                                @ColumnResult(name = "libelleGroup", type = String.class),
                                @ColumnResult(name = "typeServiceId", type = Long.class),
                                @ColumnResult(name = "typeServiceLibelle", type = String.class)
                        })),
        @SqlResultSetMapping(name = "Mapping.findOneDTO",
                classes = @ConstructorResult(targetClass = GroupRepartitionDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = Long.class),
                                @ColumnResult(name = "libelleGroup", type = String.class),
                                @ColumnResult(name = "typeServiceId", type = Long.class)
                        }))
})


@NamedNativeQueries({
        @NamedNativeQuery(name = "GroupRepartition.findByAll",
                query = "SELECT gp.id, gp.libelle_group as libelleGroup, ts.id as typeServiceId, ts.code as typeServiceLibelle from group_repartition gp, type_service ts where gp.type_service_id = ts.id limit :taille offset :page",
                resultSetMapping = "Mapping.GroupRepartitionDTO",hints={@QueryHint(name="org.hibernate.cacheable",value="false")}),

        @NamedNativeQuery(name = "GroupRepartition.findOne",
                query = "SELECT gp.id, gp.libelle_group as libelleGroup, gp.type_service_id as typeServiceId from group_repartition gp where gp.id = CAST (CAST (:id AS TEXT) AS BIGINT)",
                resultSetMapping = "Mapping.findOneDTO",hints={@QueryHint(name="org.hibernate.cacheable",value="false")})
})


@Entity
@Table(name = "group_repartition")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class GroupRepartition extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "libelle_group")
    private String libelleGroup;

    @ManyToOne
    TypeService typeService;

    @OneToMany(mappedBy = "group")
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<TableauRepartitionCommission> tableauRepartitionCommissions = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLibelleGroup() {
        return libelleGroup;
    }

    public GroupRepartition libelleGroup(String libelleGroup) {
        this.libelleGroup = libelleGroup;
        return this;
    }

    public TypeService getTypeService() {
        return typeService;
    }

    public void setTypeService(TypeService typeService) {
        this.typeService = typeService;
    }

    public void setLibelleGroup(String libelleGroup) {
        this.libelleGroup = libelleGroup;
    }

    public Set<TableauRepartitionCommission> getTableauRepartitionCommissions() {
        return tableauRepartitionCommissions;
    }

    public void setTableauRepartitionCommissions(Set<TableauRepartitionCommission> tableauRepartitionCommissions) {
        this.tableauRepartitionCommissions = tableauRepartitionCommissions;
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
        GroupRepartition groupRepartition = (GroupRepartition) o;
        if (groupRepartition.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), groupRepartition.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "GroupRepartition{" +
            "id=" + getId() +
            ", libelleGroup='" + getLibelleGroup() + "'" +
            "}";
    }
}
