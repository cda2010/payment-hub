package com.m2i.pti.service.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * A DTO for the BilanTaxe entity.
 */
public class BilanTaxeDTO implements Serializable {

    private Long id;

    private BigDecimal montant;

    private Long operationId;

    private Long taxeOperationId;

    private Boolean estPaye;

    private Long organisationId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getEstPaye() {
        return estPaye;
    }

    public void setEstPaye(Boolean estPaye) {
        this.estPaye = estPaye;
    }

    public Long getOrganisationId() {
        return organisationId;
    }

    public void setOrganisationId(Long organisationId) {
        this.organisationId = organisationId;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }

    public Long getOperationId() {
        return operationId;
    }

    public void setOperationId(Long operationId) {
        this.operationId = operationId;
    }

    public Long getTaxeOperationId() {
        return taxeOperationId;
    }

    public void setTaxeOperationId(Long taxeOperationId) {
        this.taxeOperationId = taxeOperationId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BilanTaxeDTO bilanTaxeDTO = (BilanTaxeDTO) o;
        if (bilanTaxeDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), bilanTaxeDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "BilanTaxeDTO{" +
            "id=" + getId() +
            ", montant=" + getMontant() +
            ", operation=" + getOperationId() +
            ", taxeOperation=" + getTaxeOperationId() +
            "}";
    }
}
