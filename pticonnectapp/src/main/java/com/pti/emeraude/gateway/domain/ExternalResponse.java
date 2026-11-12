package com.pti.emeraude.gateway.domain;

import java.io.Serializable;

public class ExternalResponse implements Serializable {
    private  String code;
    private String message;
    private String transactionId;
  private String montant;
    public ExternalResponse(String code, String message, String transactionId, String montant) {
        this.code = code;
        this.message = message;
        this.transactionId = transactionId;
         this.montant = montant;
    }

    public ExternalResponse() {
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

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getMontant() {
        return montant;
    }

    public void setMontant(String montant) {
        this.montant = montant;
    }

}
