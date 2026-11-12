/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.omnicanal.application.domain.util;

import com.m2i.omnicanal.model.FournisseurService;
import com.m2i.omnicanal.model.ServiceTraitement;
import com.m2i.omnicanal.model.enumeration.TypeTransaction;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

/**
 *
 * @author cda
 */
public class Transact implements Serializable {
    private ServiceTraitement service;
    private Set<LigneTrans> ligneTransaction = new HashSet<>();
    private FournisseurService fse;
       @Enumerated(EnumType.STRING)
    private TypeTransaction typeTransaction;
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

   

    public Set<LigneTrans> getLigneTransaction() {
        return ligneTransaction;
    }

    public void setLigneTransaction(Set<LigneTrans> ligneTransaction) {
        this.ligneTransaction = ligneTransaction;
    }

    public FournisseurService getFse() {
        return fse;
    }

    public void setFse(FournisseurService fse) {
        this.fse = fse;
    }

    public TypeTransaction getTypeTransaction() {
        return typeTransaction;
    }

    public void setTypeTransaction(TypeTransaction typeTransaction) {
        this.typeTransaction = typeTransaction;
    }

}
