package com.m2i.pti.service.dto;

import com.m2i.pti.domain.enumeration.Statut;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the MachineAuditEvent entity.
 */
public class MachineAuditEventDTO implements Serializable {

    private Long id;

    private Statut statut;

    private Long machineId;

    private String principal;

    private Instant auditEventDate;

    private String machineHostName;

    public String getMachineHostName() {
        return machineHostName;
    }

    public void setMachineHostName(String machineHostName) {
        this.machineHostName = machineHostName;
    }

    public Instant getAuditEventDate() {
        return auditEventDate;
    }

    public void setAuditEventDate(Instant auditEventDate) {
        this.auditEventDate = auditEventDate;
    }

    public String getPrincipal() {
        return principal;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Statut getStatut() {
        return statut;
    }

    public void setStatut(Statut statut) {
        this.statut = statut;
    }

    public Long getMachineId() {
        return machineId;
    }

    public void setMachineId(Long machineId) {
        this.machineId = machineId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MachineAuditEventDTO machineAuditEventDTO = (MachineAuditEventDTO) o;
        if (machineAuditEventDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), machineAuditEventDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "MachineAuditEventDTO{" +
                "id=" + getId() +
                ", statut='" + getStatut() + "'" +
                "}";
    }
}
