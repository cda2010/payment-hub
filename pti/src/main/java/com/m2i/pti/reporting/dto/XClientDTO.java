package com.m2i.pti.reporting.dto;

import com.m2i.pti.reporting.util.ReportHeader;

import java.util.List;

public class XClientDTO extends ReportHeader {
    public String fmt;
    public String titre;
    public List<ListeClientDTO> listeClient;
    List<XCompteMonnaieElectro> listeCompteMonnaieElectros;
    public List<ListeClientDTO> getListeClient() {
        return listeClient;
    }

    public void setListeClient(List<ListeClientDTO> listeClient) {
        this.listeClient = listeClient;
    }

    public String getFmt() {
        return fmt;
    }

    public void setFmt(String fmt) {
        this.fmt = fmt;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public List<XCompteMonnaieElectro> getListeCompteMonnaieElectros() {
        return listeCompteMonnaieElectros;
    }

    public void setListeCompteMonnaieElectros(List<XCompteMonnaieElectro> listeCompteMonnaieElectros) {
        this.listeCompteMonnaieElectros = listeCompteMonnaieElectros;
    }
}
