package com.m2i.pti.reporting.dto;

import com.m2i.pti.reporting.util.ReportHeader;

import java.util.List;

public class XListPointVente extends ReportHeader {
    List<XPointVenteDTO> listPointVente;

    public List<XPointVenteDTO> getListPointVente() {
        return this.listPointVente;
    }

    public void setListPointVente(List<XPointVenteDTO> listPointVente) {
        this.listPointVente = listPointVente;
    }


    @Override
    public String toString() {
        return "{" +
                " listPointVente='" + getListPointVente() + "'" +
                "}";
    }

}
