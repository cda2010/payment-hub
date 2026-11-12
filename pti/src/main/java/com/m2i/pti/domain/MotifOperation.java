package com.m2i.pti.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import com.m2i.pti.service.dto.MotifOperationDTO;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
@NamedNativeQuery(name = "MotifOperation.findByCode",
        query = "SELECT id, code , libelle  FROM motif_operation p WHERE code = :code",
        resultSetMapping = "Mapping.MotifOperationDTO",hints={@QueryHint(name="org.hibernate.cacheable",value="false")})

@SqlResultSetMapping(name = "Mapping.MotifOperationDTO",
        classes = @ConstructorResult(targetClass = MotifOperationDTO.class,
                columns = {
                    @ColumnResult(name = "id",type = Long.class),
                    @ColumnResult(name = "code",type = String.class),
                    @ColumnResult(name = "libelle",type = String.class)
                   
                }))
/**
 * A MotifOperation.
 */
@Entity
@Table(name = "motif_operation")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class MotifOperation extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "libelle", nullable = false)
    private String libelle;

    @Column(name = "code")
    private String code;

   

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLibelle() {
        return libelle;
    }

    public MotifOperation libelle(String libelle) {
        this.libelle = libelle;
        return this;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public MotifOperation code(String code) {
        this.code = code;
        return this;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
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
        MotifOperation motifOperation = (MotifOperation) o;
        if (motifOperation.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), motifOperation.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "MotifOperation{" +
                "id=" + getId() +
                ", libelle='" + getLibelle() + "'" +
                "}";
    }
}
