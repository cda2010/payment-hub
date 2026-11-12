/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.external.domain;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author cda
 */
import com.m2i.omnicanal.outils.restclient.RestClient;
import com.m2i.pti.service.util.Util;

/**
 *
 * 
 * @author USER_M2I
 */
@XmlRootElement(name = "COMMAND")
@XmlAccessorType(XmlAccessType.FIELD)
public class GrData implements Serializable{
 private static final long serialVersionUID = 1L;
 
    private RestClient restClient;
    
    
    @XmlElement(name = "Facturier")
    List<ExFacturier> exfacturier;

    public GrData() {
        this.restClient = new RestClient();
    }

    public List<ExFacturier> getExfacturier() {
        return exfacturier;
    }
    
    public void setExfacturier(List<ExFacturier> exfacturier) {
        this.exfacturier = exfacturier;
    }
    public String toXml() throws JAXBException{
        String marchal= Util.marshall(this,String.class);
        return marchal ;
    }
 
    public String toJson() throws IOException{
        return restClient.convertToJson(this);
    }
}