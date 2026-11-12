package com.pti.emeraude.gateway.domain;

import java.io.Serializable;

public class Message implements Serializable {

    String msg;
    String codeErr;
    String text;
    String errCode;
    String transactionId;

    public String getErrCode() {
        return errCode;
    }

    public void setErrCode(String errCode) {
        this.errCode = errCode;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getCodeErr() {
        return codeErr;
    }

    public void setCodeErr(String codeErr) {
        this.codeErr = codeErr;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

}
