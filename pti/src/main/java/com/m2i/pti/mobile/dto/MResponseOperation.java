/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.mobile.dto;

import com.m2i.pti.domain.Operation;

/**
 *
 * @author cda
 */
public class MResponseOperation {
    private String code;
    private String message;
    private Operation operation;
    private String tansactionId;

    public MResponseOperation() {
    }

    public MResponseOperation(String code, String message, Operation operation) {
        this.code = code;
        this.message = message;
        
        
        this.operation = operation;
    }

    public MResponseOperation(String code, String message, Operation operation, String tansactionId) {
        this.code = code;
        this.message = message;
        this.operation = operation;
        this.tansactionId = tansactionId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public String getTansactionId() {
        return tansactionId;
    }

    public void setTansactionId(String tansactionId) {
        this.tansactionId = tansactionId;
    }
}
