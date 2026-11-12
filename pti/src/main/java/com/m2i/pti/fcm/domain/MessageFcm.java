/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.fcm.domain;

import java.time.ZonedDateTime;

/**
 *
 * @author cda
 */
public class MessageFcm {

   
    private Long id;

   
    private String objet;

    
    private String contenu;

    
    private String expediteur;
   
    private ZonedDateTime dateMessage;
   
    private String destinataire;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getObjet() {
        return objet;
    }

    public void setObjet(String objet) {
        this.objet = objet;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public String getExpediteur() {
        return expediteur;
    }

    public void setExpediteur(String expediteur) {
        this.expediteur = expediteur;
    }

    public ZonedDateTime getDateMessage() {
        return dateMessage;
    }

    public void setDateMessage(ZonedDateTime dateMessage) {
        this.dateMessage = dateMessage;
    }

    public String getDestinataire() {
        return destinataire;
    }

    public void setDestinataire(String destinataire) {
        this.destinataire = destinataire;
    }
    
}
