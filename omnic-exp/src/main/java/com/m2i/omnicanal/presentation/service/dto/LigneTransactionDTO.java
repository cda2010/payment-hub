/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.omnicanal.presentation.service.dto;

import com.m2i.omnicanal.model.LigneTransaction;
import com.m2i.omnicanal.outils.restclient.model.ResultMessage;
import java.util.List;

/**
 *
 * @author cda
 */
public class LigneTransactionDTO {
      List<ResultMessage> msg;
      LigneTransaction ligneTransaction;
Long opId;
String terminal;

    public LigneTransactionDTO() {
    }

    public LigneTransactionDTO(List<ResultMessage> msg, LigneTransaction ligneTransaction, Long opId, String terminal) {
        this.msg = msg;
        this.ligneTransaction = ligneTransaction;
        this.opId = opId;
        this.terminal = terminal;
    }


    public LigneTransactionDTO(List<ResultMessage> msg, LigneTransaction ligneTransaction,Long opId) {
        this.msg = msg;
        this.ligneTransaction = ligneTransaction;
        this.opId=opId;
    }
    
    
    public List<ResultMessage> getMsg() {
        return msg;
    }

    public void setMsg(List<ResultMessage> msg) {
        this.msg = msg;
    }

    public LigneTransaction getLigneTransaction() {
        return ligneTransaction;
    }

    public void setLigneTransaction(LigneTransaction ligneTransaction) {
        this.ligneTransaction = ligneTransaction;
    }

    public Long getOpId() {
        return opId;
    }

    public void setOpId(Long opId) {
        this.opId = opId;
    }

    public String getTerminal() {
        return terminal;
    }

    public void setTerminal(String terminal) {
        this.terminal = terminal;
    }
      
}
