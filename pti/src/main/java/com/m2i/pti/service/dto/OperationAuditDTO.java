package com.m2i.pti.service.dto;


import com.m2i.pti.domain.enumeration.StatutOperation;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * A DTO for the OperationAudit entity.
 */
public class OperationAuditDTO implements Serializable {

    private Long id;

    private ZonedDateTime date;

    private StatutOperation statutPrecedent;

    private StatutOperation statutApres;

    private Long operationId;

    private Long userId;

    private String login;

    private String operationCodeOperation;

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ZonedDateTime getDate() {
        return date;
    }

    public void setDate(ZonedDateTime date) {
        this.date = date;
    }

    public StatutOperation getStatutPrecedent() {
        return statutPrecedent;
    }

    public void setStatutPrecedent(StatutOperation statutPrecedent) {
        this.statutPrecedent = statutPrecedent;
    }

    public StatutOperation getStatutApres() {
        return statutApres;
    }

    public void setStatutApres(StatutOperation statutApres) {
        this.statutApres = statutApres;
    }

    public Long getOperationId() {
        return operationId;
    }

    public void setOperationId(Long operationId) {
        this.operationId = operationId;
    }

    public String getOperationCodeOperation() {
        return operationCodeOperation;
    }

    public void setOperationCodeOperation(String operationCodeOperation) {
        this.operationCodeOperation = operationCodeOperation;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }


    public OperationAuditDTO(Long id,ZonedDateTime date, String statutPrecedent, String statutApres, String operationCodeOperation) {
        this.id = id;
        this.date = date;
        if(statutPrecedent != null){
            if (statutPrecedent.equals(StatutOperation.valueOf(statutPrecedent).name())){
                this.statutPrecedent = StatutOperation.valueOf(statutPrecedent);
            }
        }
        if(statutApres != null){
            if (statutApres.equals(StatutOperation.valueOf(statutApres).name())){
                this.statutApres = StatutOperation.valueOf(statutApres);
            }
        }
        this.operationCodeOperation = operationCodeOperation;
    }

    public OperationAuditDTO() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        OperationAuditDTO operationAuditDTO = (OperationAuditDTO) o;
        if(operationAuditDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), operationAuditDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "OperationAuditDTO{" +
            "id=" + getId() +
            ", date='" + getDate() + "'" +
            ", statutPrecedent='" + getStatutPrecedent() + "'" +
            ", statutApres='" + getStatutApres() + "'" +
            "}";
    }
}
