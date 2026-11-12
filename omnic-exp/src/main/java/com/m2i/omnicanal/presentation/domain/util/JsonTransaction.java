/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.omnicanal.presentation.domain.util;


import com.fasterxml.jackson.annotation.JsonProperty;

import com.m2i.omnicanal.presentation.domain.model.Transaction;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


/**
 *
 * @author HP PAVILLON
 */
public class JsonTransaction  implements  Serializable {
 @JsonProperty("transaction")
 private Transaction transation;

    public Transaction getTransation() {
        return transation;
    }

    public void setTransation(Transaction transation) {
        this.transation = transation;
    }

   

 
   
   

    public JsonTransaction() {
    }

    

    
    
    
    
}
