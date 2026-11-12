/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.domain;

import com.m2i.pti.config.audit.EntityAuditEventListener;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
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
@Table(name = "table_of_data")
@Cacheable(value = true)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class TableOfData {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "code_data")
    private String codeData;

    @Column(name = "libelle_data")
    private String libelleData;
    
    
    
    @ManyToOne
    private TypeTableOfData typeTableOfData;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodeData() {
        return codeData;
    }

    public void setCodeData(String codeData) {
        this.codeData = codeData;
    }

    public String getLibelleData() {
        return libelleData;
    }

    public void setLibelleData(String libelleData) {
        this.libelleData = libelleData;
    }

    public TypeTableOfData getTypeTableOfData() {
        return typeTableOfData;
    }

    public void setTypeTableOfData(TypeTableOfData typeTableOfData) {
        this.typeTableOfData = typeTableOfData;
    }
    
    
    

}
