/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.domain;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 *
 * @author cda
 */
@Entity
@Table(name = "instruction_comptable_registre")
public class InstructionComptableRegistre implements Serializable {
      private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    Operation operation;
    
    @ManyToOne
    UniteOrganisation uoDestAgenceSiege;
    
    @ManyToOne
    UniteOrganisation uoSrcAgenceSiege;
    
    @ManyToOne
    RegleCommission regleCommission;

    @Column(name = "is_cousumed")
    private Boolean isConsumed;
    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public UniteOrganisation getUoDestAgenceSiege() {
        return uoDestAgenceSiege;
    }

    public void setUoDestAgenceSiege(UniteOrganisation uoDestAgenceSiege) {
        this.uoDestAgenceSiege = uoDestAgenceSiege;
    }

    public UniteOrganisation getUoSrcAgenceSiege() {
        return uoSrcAgenceSiege;
    }

    public void setUoSrcAgenceSiege(UniteOrganisation uoSrcAgenceSiege) {
        this.uoSrcAgenceSiege = uoSrcAgenceSiege;
    }

    public RegleCommission getRegleCommission() {
        return regleCommission;
    }

    public void setRegleCommission(RegleCommission regleCommission) {
        this.regleCommission = regleCommission;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getIsConsumed() {
        return isConsumed;
    }

    public void setIsConsumed(Boolean isConsumed) {
        this.isConsumed = isConsumed;
    }
    
    
    
}
