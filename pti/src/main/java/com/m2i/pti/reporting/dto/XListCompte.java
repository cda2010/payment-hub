package com.m2i.pti.reporting.dto;

import com.m2i.pti.reporting.util.ReportHeader;

import java.util.List;

public class XListCompte extends ReportHeader {
    List<XCompteDTO> listCompte;
    List<XTitulaireCompteDTO> listTitulaireCompte;
    List<XUserControl> listeAction;

    public List<XCompteDTO> getListCompte() {
        return listCompte;
    }

    public void setListCompte(List<XCompteDTO> listCompte) {
        this.listCompte = listCompte;
    }

    public List<XTitulaireCompteDTO> getListTitulaireCompte() {
        return listTitulaireCompte;
    }

    public void setListTitulaireCompte(List<XTitulaireCompteDTO> listTitulaireCompte) {
        this.listTitulaireCompte = listTitulaireCompte;
    }

    public List<XUserControl> getListeAction() {
        return listeAction;
    }

    public void setListeAction(List<XUserControl> listeAction) {
        this.listeAction = listeAction;
    }
}
