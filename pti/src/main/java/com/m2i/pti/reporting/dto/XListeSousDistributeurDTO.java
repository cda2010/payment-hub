package com.m2i.pti.reporting.dto;

import com.m2i.pti.reporting.util.ReportHeader;

import java.util.List;

public class XListeSousDistributeurDTO extends ReportHeader {

    List<XSousDistributeurDTO> listDistributeur;

    public List<XSousDistributeurDTO> getListDistributeur() {
        return this.listDistributeur;
    }

    public void setListDistributeur(List<XSousDistributeurDTO> listDistributeur) {
        this.listDistributeur = listDistributeur;
    }

}
