/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.omnicanal.presentation.service;

import com.m2i.omnicanal.presentation.domain.model.Transaction;
import org.springframework.http.ResponseEntity;

/**
 *
 * @author cda
 */

public interface TransactionService {
  public    ResponseEntity<Object> getMethodeFonction (Transaction trans);
  
}
