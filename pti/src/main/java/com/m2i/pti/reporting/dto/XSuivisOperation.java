package com.m2i.pti.reporting.dto;

import com.m2i.pti.service.dto.OperationSuivisDTO;

import java.util.List;

public class XSuivisOperation {
    private List<XOperationSuivisDTO> listeOperationSuivis;
    private String titre;

    public List<XOperationSuivisDTO> getListeOperationSuivis() {
        return listeOperationSuivis;
    }

    public void setListeOperationSuivis(List<XOperationSuivisDTO> listeOperationSuivis) {
        this.listeOperationSuivis = listeOperationSuivis;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }
}
