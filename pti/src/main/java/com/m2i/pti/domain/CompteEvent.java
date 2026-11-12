/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.domain;

import com.m2i.pti.domain.enumeration.InstructionEcriture;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 *
 * @author cda
 */
@Entity
@Table(name = "compte_event")
public class CompteEvent {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "compte_id")
    private Long compteId;
    @Column(name = "montant")
    private BigDecimal montant;
    @Enumerated(EnumType.STRING)
    @Column(name = "instruction")
    private InstructionEcriture instruction;
    @Column(name = "date_operation", nullable = false)
    private ZonedDateTime dateOperation;
 @Column(name = "reference_op", nullable = false)
    private String referenceOp;
  @Column(name = "solde_compte")
    private BigDecimal soldeCompte;
  
  
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCompteId() {
        return compteId;
    }

    public void setCompteId(Long compteId) {
        this.compteId = compteId;
    }

   

    public BigDecimal getMontant() {
        return montant;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }

    public InstructionEcriture getInstruction() {
        return instruction;
    }

    public void setInstruction(InstructionEcriture instruction) {
        this.instruction = instruction;
    }

    public ZonedDateTime getDateOperation() {
        return dateOperation;
    }

    public void setDateOperation(ZonedDateTime dateOperation) {
        this.dateOperation = dateOperation;
    }

    public String getReferenceOp() {
        return referenceOp;
    }

    public void setReferenceOp(String referenceOp) {
        this.referenceOp = referenceOp;
    }

    public BigDecimal getSoldeCompte() {
        return soldeCompte;
    }

    public void setSoldeCompte(BigDecimal soldeCompte) {
        this.soldeCompte = soldeCompte;
    }

}
