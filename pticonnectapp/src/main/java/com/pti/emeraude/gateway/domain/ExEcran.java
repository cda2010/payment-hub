/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.pti.emeraude.gateway.domain;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 *
 * @author USER_M2I
 */

public class ExEcran {

    @XmlElement(name = "idecran")
    private Long id;

    @XmlAttribute(name = "numeroOrdre", required = true)
    private Integer numeroOrdre;

}
