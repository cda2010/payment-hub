/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.omnicanal.presentation.service.impl;

import com.m2i.omnicanal.outils.restclient.model.ResultMessage;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 *
 * @author cda
 */
@Component
public class MsgBean {
    List <ResultMessage> msg;

    public List<ResultMessage> getMsg() {
        return msg;
    }

    public void setMsg(List<ResultMessage> msg) {
        this.msg = msg;
    }
    
}
