/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.domain;

import com.m2i.pti.domain.enumeration.TypeControl;
import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 *
 * @author cda
 */
public class DataControl  extends AbstractAuditingEntity implements Serializable
{
     private static final long serialVersionUID = 1L;

     @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "action")
    private TypeControl action;
        @Column(name = "type_acteur")
    private String typeActeur;
        
         @Column(name = "supprime")
    private Boolean supprime=false;
         
          @Column(name = "donnee")
    private String donnee;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TypeControl getAction() {
        return action;
    }

    public void setAction(TypeControl action) {
        this.action = action;
    }

    public String getTypeActeur() {
        return typeActeur;
    }

    public void setTypeActeur(String typeActeur) {
        this.typeActeur = typeActeur;
    }

    public Boolean getSupprime() {
        return supprime;
    }

    public void setSupprime(Boolean supprime) {
        this.supprime = supprime;
    }

    public String getDonnee() {
        return donnee;
    }

    public void setDonnee(String donnee) {
        this.donnee = donnee;
    }
          
          
}
