package com.m2i.pti.service.dto;


import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;
import com.m2i.pti.domain.enumeration.Acteur;

/**
 * A DTO for the RepartitionOp entity.
 */
public class RepartitionOpDTO implements Serializable {

    private Long id;

    @NotNull
    private BigDecimal montant;

    @NotNull
    private Acteur acteur;

    private Long operationId;

    private ZonedDateTime date;

    public ZonedDateTime getDate() {
        return date;
    }

    public void setDate(ZonedDateTime date) {
        this.date = date;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }

    public Acteur getActeur() {
        return acteur;
    }

    public void setActeur(Acteur acteur) {
        this.acteur = acteur;
    }

    public Long getOperationId() {
        return operationId;
    }

    public void setOperationId(Long operationId) {
        this.operationId = operationId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RepartitionOpDTO repartitionOpDTO = (RepartitionOpDTO) o;
        if(repartitionOpDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), repartitionOpDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "RepartitionOpDTO{" +
            "id=" + getId() +
            ", montant='" + getMontant() + "'" +
            ", acteur='" + getActeur() + "'" +
            "}";
    }
}
