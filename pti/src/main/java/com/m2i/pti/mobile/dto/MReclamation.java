/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.mobile.dto;

import com.m2i.pti.domain.enumeration.EtatReclamation;
import com.m2i.pti.domain.enumeration.QualiteReclamation;
import java.time.ZonedDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

/**
 *
 * @author cda
 */
public class MReclamation {
  
    private String contenu;
    private String telephone;
    private String codeAuteur;
  
    private String codeActeur;
    private QualiteReclamation qualiteAuteur;
    private Long typeReclamation;
    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getCodeAuteur() {
        return codeAuteur;
    }

    public void setCodeAuteur(String codeAuteur) {
        this.codeAuteur = codeAuteur;
    }

    public String getCodeActeur() {
        return codeActeur;
    }

    public void setCodeActeur(String codeActeur) {
        this.codeActeur = codeActeur;
    }

    public QualiteReclamation getQualiteAuteur() {
        return qualiteAuteur;
    }

    public void setQualiteAuteur(QualiteReclamation qualiteAuteur) {
        this.qualiteAuteur = qualiteAuteur;
    }

    public Long getTypeReclamation() {
        return typeReclamation;
    }

    public void setTypeReclamation(Long typeReclamation) {
        this.typeReclamation = typeReclamation;
    }
    
}
