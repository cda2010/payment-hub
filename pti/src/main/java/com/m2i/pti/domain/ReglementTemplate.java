/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import com.m2i.pti.domain.util.*;
/**
 *
 * @author cda
 */
@Entity
@Table(name = "reglement_template")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class ReglementTemplate implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    
    private Long id;
    @Column(name = "code_service")
    String codeService;
    @Column(name = "login")
    String login;
     @Column(name = "titre")
    String titre;
    @JsonProperty("textLigneReglementTemplate")
    @Convert(converter = JpaJsonDocumentsConverter.class)
    private JsonLigneReglementTemplate textLigneReglementTemplate;
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodeService() {
        return codeService;
    }

    public void setCodeService(String codeService) {
        this.codeService = codeService;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public JsonLigneReglementTemplate getTextLigneReglementTemplate() {
        return textLigneReglementTemplate;
    }

    public void setTextLigneReglementTemplate(JsonLigneReglementTemplate textLigneReglementTemplate) {
        this.textLigneReglementTemplate = textLigneReglementTemplate;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

}
