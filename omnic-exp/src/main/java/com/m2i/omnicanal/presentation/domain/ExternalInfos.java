/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.omnicanal.presentation.domain;

import com.m2i.omnicanal.presentation.domain.util.RetourMessage;
import com.m2i.omnicanal.presentation.service.util.AdaptorCDATA;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 *
 * @author cda
 */
@XmlRootElement(name = "COMMAND")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso(String.class)
public class ExternalInfos {

    @XmlElement(name = "externalInfos")
    private List<RetourMessage> listExternalInfos;

    public List<RetourMessage> getListExternalInfos() {
        return listExternalInfos;
    }

    public void setListExternalInfos(List<RetourMessage> listExternalInfos) {
        this.listExternalInfos = listExternalInfos;
    }

}
