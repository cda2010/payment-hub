/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.domain;

import com.m2i.pti.config.audit.EntityAuditEventListener;
import java.io.Serializable;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 *
 * @author cda
 */
@Entity
@Table(name = "unite_organisation_correspondance")
@EntityListeners(value = {EntityAuditEventListener.class})
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Cacheable(value = true)
public class UniteOrganisationCorrespondance extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private UniteOrganisation uniteOrganisationCorrespondance;
    @Column(name = "codeFseTeneur")
    private String codeFseTeneur;

    @ManyToOne
    private Compte compteCorrespondance;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UniteOrganisation getUniteOrganisationCorrespondance() {
        return uniteOrganisationCorrespondance;
    }

    public void setUniteOrganisationCorrespondance(UniteOrganisation uniteOrganisationCorrespondance) {
        this.uniteOrganisationCorrespondance = uniteOrganisationCorrespondance;
    }

    public String getCodeFseTeneur() {
        return codeFseTeneur;
    }

    public void setCodeFseTeneur(String codeFseTeneur) {
        this.codeFseTeneur = codeFseTeneur;
    }

    public Compte getCompteCorrespondance() {
        return compteCorrespondance;
    }

    public void setCompteCorrespondance(Compte compteCorrespondance) {
        this.compteCorrespondance = compteCorrespondance;
    }

}
