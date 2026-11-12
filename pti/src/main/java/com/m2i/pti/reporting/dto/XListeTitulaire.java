package com.m2i.pti.reporting.dto;

import com.m2i.pti.reporting.util.ReportHeader;

import java.util.List;

public class XListeTitulaire extends ReportHeader {
    List<XTitulaireCompteDTO> listTitulaire;


    public List<XTitulaireCompteDTO> getListTitulaire() {
        return this.listTitulaire;
    }

    public void setListTitulaire(List<XTitulaireCompteDTO> listTitulaire) {
        this.listTitulaire = listTitulaire;
    }

}
