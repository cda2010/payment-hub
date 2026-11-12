/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.domain;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import com.m2i.pti.domain.enumeration.CategorieCompte;

/**
 *
 * @author cda
 */
@Entity
@Table(name = "unite_organisation_categorie_compte_gere")
public class UniteOrganisationCategorieCompteGere extends AbstractAuditingEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  
    @ManyToOne
    private UniteOrganisation uniteOrganisation;

    @Enumerated(EnumType.STRING)
    @Column(name = "categorie_compte")
    private CategorieCompte categorieCompte;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UniteOrganisation getUniteOrganisation() {
        return uniteOrganisation;
    }

    public void setUniteOrganisation(UniteOrganisation uniteOrganisation) {
        this.uniteOrganisation = uniteOrganisation;
    }

    public CategorieCompte getCategorieCompte() {
        return categorieCompte;
    }

    public void setCategorieCompte(CategorieCompte categorieCompte) {
        this.categorieCompte = categorieCompte;
    }


}
