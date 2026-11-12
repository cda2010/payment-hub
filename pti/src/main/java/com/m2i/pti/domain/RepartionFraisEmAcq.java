package com.m2i.pti.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Objects;

/**
 * A RepartionFraisEmAcq.
 */
@Entity
@Table(name = "repartion_frais_em_acq")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class RepartionFraisEmAcq extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "part_em")
    private Integer partEm;

    @Column(name = "part_acq")
    private Integer partAcq;

    @ManyToOne
    private TypeService typeService;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getPartEm() {
        return partEm;
    }

    public RepartionFraisEmAcq partEm(Integer partEm) {
        this.partEm = partEm;
        return this;
    }

    public void setPartEm(Integer partEm) {
        this.partEm = partEm;
    }

    public Integer getPartAcq() {
        return partAcq;
    }

    public RepartionFraisEmAcq partAcq(Integer partAcq) {
        this.partAcq = partAcq;
        return this;
    }

    public void setPartAcq(Integer partAcq) {
        this.partAcq = partAcq;
    }

    public TypeService getTypeService() {
        return typeService;
    }

    public RepartionFraisEmAcq typeService(TypeService typeService) {
        this.typeService = typeService;
        return this;
    }

    public void setTypeService(TypeService typeService) {
        this.typeService = typeService;
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
        RepartionFraisEmAcq repartionFraisEmAcq = (RepartionFraisEmAcq) o;
        if (repartionFraisEmAcq.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), repartionFraisEmAcq.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "RepartionFraisEmAcq{" +
            "id=" + getId() +
            ", partEm=" + getPartEm() +
            ", partAcq=" + getPartAcq() +
            "}";
    }
}
