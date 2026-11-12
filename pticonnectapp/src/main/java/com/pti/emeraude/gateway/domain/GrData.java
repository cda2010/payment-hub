/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pti.emeraude.gateway.domain;

/**
 *
 * @author cda
 */
import java.io.Serializable;
import java.util.List;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 *
 * @author USER_M2I
 */
@XmlRootElement(name = "COMMAND")
@XmlAccessorType(XmlAccessType.FIELD)
public class GrData implements Serializable {

    private static final long serialVersionUID = 1L;

    @XmlElement(name = "Facturier")
    List<ExFacturier> exfacturier;

    public List<ExFacturier> getExfacturier() {
        return exfacturier;
    }

    public void setExfacturier(List<ExFacturier> exfacturier) {
        this.exfacturier = exfacturier;
    }

    @Override
    public String toString() {
        return "GrData{" + "exfacturier=" + exfacturier + '}';
    }

}
