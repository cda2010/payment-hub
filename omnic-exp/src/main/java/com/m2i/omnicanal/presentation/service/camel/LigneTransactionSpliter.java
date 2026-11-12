/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.omnicanal.presentation.service.camel;


import com.m2i.omnicanal.model.LigneTransaction;
import com.m2i.omnicanal.presentation.service.dto.TransactionDTO;
import java.util.Set;

/**
 *
 * @author cda
 */
public class LigneTransactionSpliter {
    public Set<LigneTransaction> splitLigneTransaction(TransactionDTO trans) {    
        return trans.getTrans().getLigneTransaction();  
    }
}
