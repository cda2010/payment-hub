package com.m2i.pti.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A Liste.
 */
@Entity
@Table(name = "liste")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Liste extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "options")
    private String options;

    @Column(name = "multiple")
    private Boolean multiple;
    
    @Column(name = "est_static")
    private Boolean estStatic = true;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOptions() {
        return options;
    }

    public Liste options(String options) {
        this.options = options;
        return this;
    }

    public void setOptions(String options) {
        this.options = options;
    }

    public Boolean isMultiple() {
        return multiple;
    }

    public Boolean getEstStatic() {
        return estStatic;
    }

    public void setEstStatic(Boolean estStatic) {
        this.estStatic = estStatic;
    }

    public Liste multiple(Boolean multiple) {
        this.multiple = multiple;
        return this;
    }

    public void setMultiple(Boolean multiple) {
        this.multiple = multiple;
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
        Liste liste = (Liste) o;
        if (liste.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), liste.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "Liste{"
                + "id=" + getId()
                + ", options='" + getOptions() + "'"
                + ", multiple='" + isMultiple() + "'"
                +", estStatic='"+ getEstStatic() +"'"
                + "}";
    }
}
