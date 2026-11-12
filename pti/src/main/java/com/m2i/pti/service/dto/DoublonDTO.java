package com.m2i.pti.service.dto;

import java.util.List;

public class DoublonDTO {
    public String nbr_doublon;
    public String nomClient;
    public String telephone;
    private List<PersonneDTO> listePersonne;
    private List<Long> listePersonneId;

    public String getNbr_doublon() {
        return nbr_doublon;
    }

    public void setNbr_doublon(String nbr_doublon) {
        this.nbr_doublon = nbr_doublon;
    }

    public String getNomClient() {
        return nomClient;
    }

    public void setNomClient(String nomClient) {
        this.nomClient = nomClient;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public List<PersonneDTO> getListePersonne() {
        return listePersonne;
    }

    public void setListePersonne(List<PersonneDTO> listePersonne) {
        this.listePersonne = listePersonne;
    }

    public List<Long> getListePersonneId() {
        return listePersonneId;
    }

    public void setListePersonneId(List<Long> listePersonneId) {
        this.listePersonneId = listePersonneId;
    }

    @Override
    public String toString() {
        return "DoublonDTO{" +
                "nbr_doublon='" + nbr_doublon + '\'' +
                ", nomClient='" + nomClient + '\'' +
                ", telephone='" + telephone + '\'' +
                ", lisePersonne=" + listePersonne +
                ",listePersonneId="+ listePersonneId+
                '}';
    }
}
