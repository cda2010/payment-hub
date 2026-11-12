package com.m2i.pti.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.m2i.pti.service.dto.CanalDTO;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;
@NamedNativeQuery(name = "Canal.findByCodeCanal",
        query = "SELECT id, code_canal codeCanal, libelle_canal libelleCanal  FROM canal c WHERE code_canal =CAST(:code AS text)",
        resultSetMapping = "Mapping.CanalDTO",hints={@QueryHint(name="org.hibernate.cacheable",value="false")})

@SqlResultSetMapping(name = "Mapping.CanalDTO",
        classes = @ConstructorResult(targetClass = CanalDTO.class,
                columns = {
                    @ColumnResult(name = "id",type = Long.class),
                    @ColumnResult(name = "codeCanal",type = String.class),
                    @ColumnResult(name = "libelleCanal",type = String.class),
                 
                }))
/**
 * A Canal.
 */
@Entity
@Table(name = "canal")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Canal extends AbstractAuditingEntity  implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "code_canal", nullable = false)
    private String codeCanal;

    @NotNull
    @Column(name = "libelle_canal", nullable = false)
    private String libelleCanal;

   

    

    @ManyToMany
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @JoinTable(name = "canal_type_service",
               joinColumns = @JoinColumn(name="canals_id", referencedColumnName="id"),
               inverseJoinColumns = @JoinColumn(name="type_services_id", referencedColumnName="id"))
    private Set<TypeService> typeServices = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodeCanal() {
        return codeCanal;
    }

    public Canal codeCanal(String codeCanal) {
        this.codeCanal = codeCanal;
        return this;
    }

    public void setCodeCanal(String codeCanal) {
        this.codeCanal = codeCanal;
    }

    public String getLibelleCanal() {
        return libelleCanal;
    }

    public Canal libelleCanal(String libelleCanal) {
        this.libelleCanal = libelleCanal;
        return this;
    }

    public void setLibelleCanal(String libelleCanal) {
        this.libelleCanal = libelleCanal;
    }

   

   

    public Set<TypeService> getTypeServices() {
        return typeServices;
    }

    public Canal typeServices(Set<TypeService> typeServices) {
        this.typeServices = typeServices;
        return this;
    }

   

    public void setTypeServices(Set<TypeService> typeServices) {
        this.typeServices = typeServices;
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
        Canal canal = (Canal) o;
        if (canal.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), canal.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "Canal{" +
            "id=" + getId() +
            ", codeCanal='" + getCodeCanal() + "'" +
            ", libelleCanal='" + getLibelleCanal() + "'" +
            "}";
    }
}
