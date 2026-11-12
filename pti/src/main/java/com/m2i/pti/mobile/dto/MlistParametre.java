/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.mobile.dto;

import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author cda
 */
@XmlRootElement(name = "COMMAND")
@XmlAccessorType (XmlAccessType.FIELD)
public class MlistParametre {
    @XmlElement(name = "parametres")
    private List<MParametre> parametre = null;

    public List<MParametre> getParametre() {
        return parametre;
    }

    public void setParametre(List<MParametre> parametre) {
        this.parametre = parametre;
    }
    
    
}
