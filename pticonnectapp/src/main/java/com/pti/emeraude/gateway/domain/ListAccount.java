/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pti.emeraude.gateway.domain;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

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

    @Override
    public String toString() {
        return "ListAccount{" + "listCompte=" + listCompte + '}';
    }

}
