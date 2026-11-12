package com.pti.emeraude.gateway.domain;

import java.util.List;

public class MCard {

    private MPersonne titulaire;
    private String code;
    private String message;
    List<MCompteCard> cardList;

    public MCard(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public MCard() {
    }

    public MPersonne getTitulaire() {
        return titulaire;
    }

    public void setTitulaire(MPersonne titulaire) {
        this.titulaire = titulaire;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<MCompteCard> getCardList() {
        return cardList;
    }

    public void setCardList(List<MCompteCard> cardList) {
        this.cardList = cardList;
    }
}
