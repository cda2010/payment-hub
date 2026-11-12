/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.mobile.dto;

/**
 *
 * @author cda
 */
public class MPointVente {
    String codePv;
    MUser user;
String libellePv;
    public String getCodePv() {
        return codePv;
    }

    public void setCodePv(String codePv) {
        this.codePv = codePv;
    }

    public MUser getUser() {
        return user;
    }

    public void setUser(MUser user) {
        this.user = user;
    }

    public String getLibellePv() {
        return libellePv;
    }

    public void setLibellePv(String libellePv) {
        this.libellePv = libellePv;
    }
    
}
