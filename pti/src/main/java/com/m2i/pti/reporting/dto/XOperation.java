package com.m2i.pti.reporting.dto;

import com.m2i.pti.domain.enumeration.StatutOperation;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public class XOperation {
    Long typeServiceId;
    Long canalId;
    Long uoAcquereurId;
    BigDecimal montantOp;
    StatutOperation statutOperation;
    ZonedDateTime dateOperation;
    ZonedDateTime dateFin;
    String codeOperation;
    Long organisationId;
    String format;
    String description;
    public Long getTypeServiceId() {
        return typeServiceId;
    }

    public void setTypeServiceId(Long typeServiceId) {
        this.typeServiceId = typeServiceId;
    }

    public Long getCanalId() {
        return canalId;
    }

    public void setCanalId(Long canalId) {
        this.canalId = canalId;
    }

    public Long getUoAcquereurId() {
        return uoAcquereurId;
    }

    public void setUoAcquereurId(Long uoAcquereurId) {
        this.uoAcquereurId = uoAcquereurId;
    }

    public BigDecimal getMontantOp() {
        return montantOp;
    }

    public void setMontantOp(BigDecimal montantOp) {
        this.montantOp = montantOp;
    }

    public StatutOperation getStatutOperation() {
        return statutOperation;
    }

    public void setStatutOperation(StatutOperation statutOperation) {
        this.statutOperation = statutOperation;
    }

    public ZonedDateTime getDateOperation() {
        return dateOperation;
    }

    public void setDateOperation(ZonedDateTime dateOperation) {
        this.dateOperation = dateOperation;
    }

    public ZonedDateTime getDateFin() {
        return dateFin;
    }

    public void setDateFin(ZonedDateTime dateFin) {
        this.dateFin = dateFin;
    }

    public String getCodeOperation() {
        return codeOperation;
    }

    public void setCodeOperation(String codeOperation) {
        this.codeOperation = codeOperation;
    }

    public Long getOrganisationId() {
        return organisationId;
    }

    public void setOrganisationId(Long organisationId) {
        this.organisationId = organisationId;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
