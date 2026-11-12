/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.domain;

import java.io.Serializable;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 *
 * @author cda
 */
@Entity
@Table(name = "external_account")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Cacheable(value = true)
public class ExternalAccount extends AbstractAuditingEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private Compte compte;

    @Column(name = "alias")
    private String alias;
    @Column(name = "code_operateur")
    private String codeOperateur;

    
  @Column(name = "etat_compte_externe")    
       private boolean etatCompteExterne;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Compte getCompte() {
        return compte;
    }

    public void setCompte(Compte compte) {
        this.compte = compte;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getCodeOperateur() {
        return codeOperateur;
    }

    public void setCodeOperateur(String codeOperateur) {
        this.codeOperateur = codeOperateur;
    }

    public boolean isEtatCompteExterne() {
        return etatCompteExterne;
    }

    public void setEtatCompteExterne(boolean etatCompteExterne) {
        this.etatCompteExterne = etatCompteExterne;
    }

}
