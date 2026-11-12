/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.omnicanal.presentation.domain.util;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 *
 * @author cda
 */
@XmlRootElement(name = "RETOURMESSAGE")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso(String.class)
public class RetourMessage {

    String rrn;
    String status;
    String msg;
    String cpteSrc;
    String cpteDest;
    String code;
    public String getRrn() {
        return rrn;
    }

    public void setRrn(String rrn) {
        this.rrn = rrn;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getCpteSrc() {
        return cpteSrc;
    }

    public void setCpteSrc(String cpteSrc) {
        this.cpteSrc = cpteSrc;
    }

    public String getCpteDest() {
        return cpteDest;
    }

    public void setCpteDest(String cpteDest) {
        this.cpteDest = cpteDest;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

}
