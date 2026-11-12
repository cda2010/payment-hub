package com.m2i.pti.service.dto;


import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * A DTO for the OperationSuivis entity.
 */
public class OperationSuivisDTO implements Serializable {

    private Long id;

    private Long clientId;

    private Long operationId;

    private String nomClient;

    private BigDecimal montantOp;

    public OperationSuivisDTO() {
    }

    public OperationSuivisDTO(Long id, Long clientId, Long operationId) {
        this.id = id;
        this.clientId = clientId;
        this.operationId = operationId;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    private String statut;

    public BigDecimal getMontantOp() {
        return montantOp;
    }

    public void setMontantOp(BigDecimal montantOp) {
        this.montantOp = montantOp;
    }

    public String getNomClient() {
        return nomClient;
    }

    public void setNomClient(String nomClient) {
        this.nomClient = nomClient;
    }

    public String getPrenomClient() {
        return prenomClient;
    }

    public void setPrenomClient(String prenomClient) {
        this.prenomClient = prenomClient;
    }

    private String prenomClient;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
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

        OperationSuivisDTO operationSuivisDTO = (OperationSuivisDTO) o;
        if(operationSuivisDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), operationSuivisDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "OperationSuivisDTO{" +
            "id=" + getId() +
            "}";
    }
}
