/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.mobile.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.LockModeType;

/**
 *
 * @author cda
 */
public class BankTransactionService {


    EntityManagerFactory emf ;
    
   EntityManager em = emf.createEntityManager();
     // em.lock(a, LockModeType.PESSIMISTIC_WRITE);
 // Account a = em.;
    
}
