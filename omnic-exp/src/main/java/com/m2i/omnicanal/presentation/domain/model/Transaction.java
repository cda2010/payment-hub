/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.omnicanal.presentation.domain.model;


import com.m2i.omnicanal.model.FournisseurService;
import com.m2i.omnicanal.model.ServiceTraitement;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author cda
 */
public class Transaction implements Serializable {
    private ServiceTraitement service;
    private Set<LigneTransaction> ligneTransaction = new HashSet<>();
    private FournisseurService fse;
private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ServiceTraitement getService() {
        return service;
    }

    public void setService(ServiceTraitement service) {
        this.service = service;
    }

   

    public Set<LigneTransaction> getLigneTransaction() {
        return ligneTransaction;
    }

    public void setLigneTransaction(Set<LigneTransaction> ligneTransaction) {
        this.ligneTransaction = ligneTransaction;
    }

    public FournisseurService getFse() {
        return fse;
    }

    public void setFse(FournisseurService fse) {
        this.fse = fse;
    }

}
