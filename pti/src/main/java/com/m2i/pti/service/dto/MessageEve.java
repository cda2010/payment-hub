package com.m2i.pti.service.dto;

import com.m2i.omnicanal.outils.restclient.model.Message;

import java.util.List;

public class MessageEve extends Message {
    List<Evenement> listeEvenement;

    private String resultat;

    public List<Evenement> getListeEvenement() {
        return listeEvenement;
    }

    public void setListeEvenement(List<Evenement> listeEvenement) {
        this.listeEvenement = listeEvenement;
    }

    public String getResultat() {
        return resultat;
    }

    public void setResultat(String resultat) {
        this.resultat = resultat;
    }
}
