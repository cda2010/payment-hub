/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.omnicanal.presentation.domain;

import com.m2i.omnicanal.model.Operation;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

/**
 *
 * @author cda
 */
@Entity

public class Oper extends Operation implements Serializable {

   @ManyToOne
    private User user;
    @Column(columnDefinition = "TEXT")
    private String externalInfos;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getExternalInfos() {
        return externalInfos;
    }

    public void setExternalInfos(String externalInfos) {
        this.externalInfos = externalInfos;
    }
}
