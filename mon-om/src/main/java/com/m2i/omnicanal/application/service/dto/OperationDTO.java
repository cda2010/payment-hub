/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.omnicanal.application.service.dto;

import java.io.Serializable;
import java.time.ZonedDateTime;
import jakarta.persistence.ColumnResult;

/**
 *
 * @author cda
 */
public class OperationDTO implements Serializable {

    Long id;
    ZonedDateTime dateOperation;
    String libelleTypeService;
    String fseCodeSource;
    String fseCodeDest;
    String reference;
    String textTransaction;
    String terminal;
      Integer ack;

    public OperationDTO(Long id, String reference, String fseCodeSource, String fseCodeDest, String libelleTypeService,String terminal, String textTransaction, ZonedDateTime dateOperation, Integer ack) {
        this.id = id;
        this.reference = reference;

        this.fseCodeSource = fseCodeSource;
        this.fseCodeDest = fseCodeDest;
        this.libelleTypeService = libelleTypeService;
        this.textTransaction = textTransaction;
        this.terminal = terminal;
        this.dateOperation = dateOperation;
        this.ack=ack;

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ZonedDateTime getDateOperation() {
        return dateOperation;
    }

    public void setDateOperation(ZonedDateTime dateOperation) {
        this.dateOperation = dateOperation;
    }

    public String getLibelleTypeService() {
        return libelleTypeService;
    }

    public void setLibelleTypeService(String libelleTypeService) {
        this.libelleTypeService = libelleTypeService;
    }

    public String getFseCodeSource() {
        return fseCodeSource;
    }

    public void setFseCodeSource(String fseCodeSource) {
        this.fseCodeSource = fseCodeSource;
    }

    public String getFseCodeDest() {
        return fseCodeDest;
    }

    public void setFseCodeDest(String fseCodeDest) {
        this.fseCodeDest = fseCodeDest;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getTextTransaction() {
        return textTransaction;
    }

    public void setTextTransaction(String textTransaction) {
        this.textTransaction = textTransaction;
    }

    public String getTerminal() {
        return terminal;
    }

    public void setTerminal(String terminal) {
        this.terminal = terminal;
    }

    public Integer getAck() {
        return ack;
    }

    public void setAck(Integer ack) {
        this.ack = ack;
    }

}
