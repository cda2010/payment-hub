package com.m2i.pti.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

import com.m2i.pti.domain.enumeration.Statut;

/**
 * A MachineAuditEvent.
 */
@Entity
@Table(name = "machine_audit_event")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class MachineAuditEvent extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut")
    private Statut statut;

    @NotNull
    @Column(nullable = false)
    private String principal;

    @Column(name = "event_date")
    private Instant auditEventDate;

    @ManyToOne
    private Machine machine;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Statut getStatut() {
        return statut;
    }

    public MachineAuditEvent statut(Statut statut) {
        this.statut = statut;
        return this;
    }

    public Machine getMachine() {
        return machine;
    }

    public void setMachine(Machine machine) {
        this.machine = machine;
    }

    public String getPrincipal() {
        return principal;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    public Instant getAuditEventDate() {
        return auditEventDate;
    }

    public void setAuditEventDate(Instant auditEventDate) {
        this.auditEventDate = auditEventDate;
    }

    public MachineAuditEvent machine(Machine machine) {
        this.machine = machine;
        return this;
    }

    public void setStatut(Statut statut) {
        this.statut = statut;
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
        MachineAuditEvent machineAuditEvent = (MachineAuditEvent) o;
        if (machineAuditEvent.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), machineAuditEvent.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "MachineAuditEvent{" +
                "id=" + getId() +
                ", statut='" + getStatut() + "'" +
                "}";
    }
}
