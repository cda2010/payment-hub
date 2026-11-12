/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pti.emeraude.gateway.domain;

import java.time.LocalDateTime;
import java.util.List;

/**
 *
 * @author cda
 */
public class MNotification {
       
    private Long id;
    
    private LocalDateTime dateHeureEnvoie;

   
    private String contenu;

    
    private String destinataire;

    
    private String codeIndicatif;
    
    
   
    private Boolean isDelivred;

    
    private String typeNotification;
  private Long  operationId;
     private    List<MParametre> extraData ;
    public MNotification(Long id, LocalDateTime dateHeureEnvoie, String contenu, String destinataire, String codeIndicatif, String typeNotification) {
        this.id = id;
        this.dateHeureEnvoie = dateHeureEnvoie;
        this.contenu = contenu;
        this.destinataire = destinataire;
        this.codeIndicatif = codeIndicatif;
       
        this.typeNotification = typeNotification;
    }
 public MNotification(Long id, LocalDateTime dateHeureEnvoie, String contenu, String destinataire, String codeIndicatif, String typeNotification,Long  operationId, List<MParametre> extraData) {
        this.id = id;
        this.dateHeureEnvoie = dateHeureEnvoie;
        this.contenu = contenu;
        this.destinataire = destinataire;
        this.codeIndicatif = codeIndicatif;
        this.typeNotification = typeNotification;
        this.operationId=operationId;
        this.extraData=extraData;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getDateHeureEnvoie() {
        return dateHeureEnvoie;
    }

    public void setDateHeureEnvoie(LocalDateTime dateHeureEnvoie) {
        this.dateHeureEnvoie = dateHeureEnvoie;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public String getDestinataire() {
        return destinataire;
    }

    public void setDestinataire(String destinataire) {
        this.destinataire = destinataire;
    }

    public String getCodeIndicatif() {
        return codeIndicatif;
    }

    public void setCodeIndicatif(String codeIndicatif) {
        this.codeIndicatif = codeIndicatif;
    }

    public Boolean getIsDelivred() {
        return isDelivred;
    }

    public void setIsDelivred(Boolean isDelivred) {
        this.isDelivred = isDelivred;
    }

    public String getTypeNotification() {
        return typeNotification;
    }

    public void setTypeNotification(String typeNotification) {
        this.typeNotification = typeNotification;
    }

    public Long getOperationId() {
        return operationId;
    }

    public void setOperationId(Long operationId) {
        this.operationId = operationId;
    }

    public List<MParametre> getExtraData() {
        return extraData;
    }

    public void setExtraData(List<MParametre> extraData) {
        this.extraData = extraData;
    }
    
}
