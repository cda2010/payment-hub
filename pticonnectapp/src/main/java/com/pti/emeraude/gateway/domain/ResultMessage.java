package com.pti.emeraude.gateway.domain;

public class ResultMessage {

    int errCode;
    String msg;
    String numCouche;
    Object responseEntity;
    String responseType;

    public ResultMessage() {
    }

    public ResultMessage(int errCode, String msg, String numCouche, Object responseEntity) {
        this.errCode = errCode;
        this.msg = msg;
        this.numCouche = numCouche;
        this.responseEntity = responseEntity;
    }

    public int getErrCode() {
        return this.errCode;
    }

    public void setErrCode(int errCode) {
        this.errCode = errCode;
    }

    public String getMsg() {
        return this.msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getResponseEntity() {
        return this.responseEntity;
    }

    public void setResponseEntity(Object responseEntity) {
        this.responseEntity = responseEntity;
    }

    public String getNumCouche() {
        return this.numCouche;
    }

    public void setNumCouche(String numCouche) {
        this.numCouche = numCouche;
    }

    public String getResponseType() {
        return this.responseType;
    }

    public void setResponseType(String responseType) {
        this.responseType = responseType;
    }
}
