/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.service.dto;

import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author cda
 */
@XmlRootElement(name = "listCompte")
@XmlAccessorType(XmlAccessType.FIELD)
public class ListAccount {
    @XmlElement(name = "compte")
    List<CustomerAccount> listCompte;
    public List<CustomerAccount> getListCompte() {
        return listCompte;
    }
    public void setListCompte(List<CustomerAccount> listCompte) {
        this.listCompte = listCompte;
    }

}
