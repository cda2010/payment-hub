/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.domain;

import com.m2i.pti.mobile.dto.MGroupeUniteOrganisation;
import com.m2i.pti.service.dto.EtapeServiceDTO;
import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.ColumnResult;
import jakarta.persistence.ConstructorResult;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedNativeQueries;
import jakarta.persistence.NamedNativeQuery;
import jakarta.persistence.QueryHint;
import jakarta.persistence.SqlResultSetMapping;
import jakarta.persistence.SqlResultSetMappings;
import jakarta.persistence.Table;

/**
 *
 * @author cda
 */
@SqlResultSetMappings(value = {
    @SqlResultSetMapping(name = "Mapping.GroupeFseDTO",
            classes = @ConstructorResult(targetClass = MGroupeUniteOrganisation.class,
                    columns = {
                  
                        @ColumnResult(name = "code", type = String.class),
                        @ColumnResult(name = "libelle", type = String.class)
                    
                       
                    }))
}) 
@NamedNativeQueries({
    @NamedNativeQuery(name = "groupeFse.findAllGroupe",
            query = "select code, libelle  from  groupe_unite_organisation",
           resultSetMapping = "Mapping.GroupeFseDTO", 
            hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")}),  
            })
@Entity
@Table(name = "groupe_unite_organisation")
public class GroupeFse extends AbstractAuditingEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
   @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "libelle", nullable = false)
    private String libelle;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
    
    
}
