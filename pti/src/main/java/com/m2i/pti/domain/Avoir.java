/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.domain;

import java.io.Serializable;
import jakarta.persistence.Column;

import jakarta.persistence.*;
/**
 *
 * @author cda
 */
@Entity
@Table(name = "avoir")
public class Avoir  extends AbstractAuditingEntity  implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    Operation operation;
    @Column(name = "est_paye")
    private Boolean estPaye;
    @Column(name = "en_traitement")
    private Boolean enTraitement;

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public Boolean getEstPaye() {
        return estPaye;
    }

    public void setEstPaye(Boolean estPaye) {
        this.estPaye = estPaye;
    }

    public Boolean getEnTraitement() {
        return enTraitement;
    }

    public void setEnTraitement(Boolean enTraitement) {
        this.enTraitement = enTraitement;
    }
    
    
    
}
