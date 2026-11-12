/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.mobile.dto;

import java.util.List;

/**
 *
 * @author cda
 */
public class SettlementResponse {
         List<MParametre> mParametres;
         String codeMsg;
         String msg;
         String reference;
         String transactionId;
         Boolean sendMsg;

    public List<MParametre> getmParametres() {
        return mParametres;
    }

    public void setmParametres(List<MParametre> mParametres) {
        this.mParametres = mParametres;
    }

    public String getCodeMsg() {
        return codeMsg;
    }

    public void setCodeMsg(String codeMsg) {
        this.codeMsg = codeMsg;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public Boolean getSendMsg() {
        return sendMsg;
    }

    public void setSendMsg(Boolean sendMsg) {
        this.sendMsg = sendMsg;
    }
         
                 
}
