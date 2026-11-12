/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.omnicanal.presentation.service.impl;

import com.m2i.omnicanal.presentation.domain.model.Transaction;

import org.springframework.stereotype.Component;

/**
 *
 * @author cda
 */

@Component(value="objectTransactBean")

public class ObjectTransact  {
public Transaction presentation;

    public Transaction getTransaction() {
        return presentation;
    }

    public void setTransaction(Transaction presentation) {
        this.presentation = presentation;
    }
   
   
}
