/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.omnicanal.application.domain.util;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.m2i.omnicanal.model.Transaction;



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
 private Transact transation;

    public Transact getTransation() {
        return transation;
    }

    public void setTransation(Transact transation) {
        this.transation = transation;
    }

   

 
   
   

    public JsonTransaction() {
    }

    

    
    
    
    
}
