/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.m2i.pti.external.domain;

import jakarta.persistence.Column;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

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
