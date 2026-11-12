/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.service.dto;


import jakarta.xml.bind.annotation.*;
import java.util.List;

/**
 *
 * @author cda
 */
@XmlRootElement(name = "Command")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso(String.class)
public class Statement {
      @XmlElement(name = "Transaction")
       List<Mouvement> listeMouvement;

    public List<Mouvement> getListeMouvement() {
        return listeMouvement;
    }

    public void setListeMouvement(List<Mouvement> listeMouvement) {
        this.listeMouvement = listeMouvement;
    }
       
}
