/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.service.dto;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author cda
 */
public class OperationReglementDTO {
    OperationDTO operationDto;
    List<Long> uoId;
    ZonedDateTime dateReglement;

    public OperationDTO getOperationDto() {
        return operationDto;
    }

    public void setOperationDto(OperationDTO operationDto) {
        this.operationDto = operationDto;
    }

    public List<Long> getUoId() {
        return uoId;
    }

    public void setUoId(List<Long> uoId) {
        this.uoId = uoId;
    }

    public ZonedDateTime getDateReglement() {
        return dateReglement;
    }

    public void setDateReglement(ZonedDateTime dateReglement) {
        this.dateReglement = dateReglement;
    }
}
