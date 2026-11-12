/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.omnicanal.presentation.service.dto;

import com.m2i.omnicanal.model.Transaction;
import com.m2i.omnicanal.outils.restclient.model.ResultMessage;

import java.util.List;

/**
 *
 * @author cda
 */
public class TransactionDTO {
    Transaction trans;
    List<ResultMessage> msg;

    public TransactionDTO() {
    }

    public TransactionDTO(Transaction trans, List<ResultMessage> msg) {
        this.trans = trans;
        this.msg = msg;
    }

    public Transaction getTrans() {
        return trans;
    }

    public void setTrans(Transaction trans) {
        this.trans = trans;
    }

    public List<ResultMessage> getMsg() {
        return msg;
    }

    public void setMsg(List<ResultMessage> msg) {
        this.msg = msg;
    }
    
}
