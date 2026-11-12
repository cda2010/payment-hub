/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.domain;

import com.m2i.pti.config.audit.EntityAuditEventListener;
import java.io.Serializable;
import java.math.BigDecimal;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 *
 * @author cda
 */
@Entity
@Table(name = "evenement_operation")
@EntityListeners(value = {EntityAuditEventListener.class})
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class EvenementOperation  extends AbstractAuditingEntity  implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private Operation operation;
    @ManyToOne
    private Compte compte;
    @Column(name = "montant", precision = 10, scale = 2)
    private BigDecimal montant;
    @NotNull
    @Column(name = "est_levee", nullable = false)
    private Integer estLevee;
    @Column(name = "en_traitement")
    private Boolean enTraitement;

    @Column(name="motif")
    private String motif;
    @Column(name="login")
    private String login;
   
     @Column(name="type_evenement")
    private String typeEvenement;
    
    
    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getEstLevee() {
        return estLevee;
    }

    public void setEstLevee(Integer estLevee) {
        this.estLevee = estLevee;
    }

    public Compte getCompte() {
        return compte;
    }

    public void setCompte(Compte compte) {
        this.compte = compte;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }

    public Boolean getEnTraitement() {
        return enTraitement;
    }

    public void setEnTraitement(Boolean enTraitement) {
        this.enTraitement = enTraitement;
    }

    public String getMotif() {
        return motif;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getTypeEvenement() {
        return typeEvenement;
    }

    public void setTypeEvenement(String typeEvenement) {
        this.typeEvenement = typeEvenement;
    }
    
    
    
}
