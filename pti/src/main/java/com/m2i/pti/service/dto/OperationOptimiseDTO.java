package com.m2i.pti.service.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

public class OperationOptimiseDTO implements Serializable{

    private Long id;

    private BigDecimal montantOp;

    private String codeOperation;

    private ZonedDateTime dateOperation;

    private String statut;

    private String codeService;
    private String codeExterne;
    private String devisePaiementLibelle;
    Long typeService;

    public OperationOptimiseDTO() {
    }

    public OperationOptimiseDTO(Long id, BigDecimal montantOp, String codeOperation, ZonedDateTime dateOperation, String statut, String codeService, String devisePaiementLibelle) {
        this.id = id;
        this.montantOp = montantOp;
        this.codeOperation = codeOperation;
        this.dateOperation = dateOperation;
        this.statut = statut;
        this.codeService = codeService;
        this.devisePaiementLibelle = devisePaiementLibelle;
    }

    public OperationOptimiseDTO(Long id, BigDecimal montantOp, String codeOperation, ZonedDateTime dateOperation, String statut, String codeService) {
        this.id = id;
        this.montantOp = montantOp;
        this.codeOperation = codeOperation;
        this.dateOperation = dateOperation;
        this.statut = statut;
        this.codeService = codeService;
    }

    public OperationOptimiseDTO(Long id, BigDecimal montantOp, String codeOperation, ZonedDateTime dateOperation, String statut, String codeExterne, Long typeService) {
        this.id = id;
        this.montantOp = montantOp;
        this.codeOperation = codeOperation;
        this.dateOperation = dateOperation;
        this.statut = statut;
        this.codeExterne = codeExterne;
    
        this.typeService = typeService;
    }

    public String getDevisePaiementLibelle() {
        return devisePaiementLibelle;
    }

    public void setDevisePaiementLibelle(String devisePaiementLibelle) {
        this.devisePaiementLibelle = devisePaiementLibelle;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getMontantOp() {
        return montantOp;
    }

    public void setMontantOp(BigDecimal montantOp) {
        this.montantOp = montantOp;
    }

    public String getCodeOperation() {
        return codeOperation;
    }

    public void setCodeOperation(String codeOperation) {
        this.codeOperation = codeOperation;
    }

    public ZonedDateTime getDateOperation() {
        return dateOperation;
    }

    public void setDateOperation(ZonedDateTime dateOperation) {
        this.dateOperation = dateOperation;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getCodeService() {
        return codeService;
    }

    public void setCodeService(String codeService) {
        this.codeService = codeService;
    }

    public String getCodeExterne() {
        return codeExterne;
    }

    public void setCodeExterne(String codeExterne) {
        this.codeExterne = codeExterne;
    }

    public Long getTypeService() {
        return typeService;
    }

    public void setTypeService(Long typeService) {
        this.typeService = typeService;
    }

}
