/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.domain;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.ColumnResult;
import jakarta.persistence.ConstructorResult;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedNativeQueries;
import jakarta.persistence.NamedNativeQuery;
import jakarta.persistence.QueryHint;
import jakarta.persistence.SqlResultSetMapping;
import jakarta.persistence.SqlResultSetMappings;
import jakarta.persistence.Table;

import com.m2i.pti.service.dto.TypeServiceReseauOrganisationDTO;

/**
 *
 * @author cda
 */


 @SqlResultSetMappings(value = {
    @SqlResultSetMapping(name = "Mapping.TypeServiceReseauOrganisationDTO",
            classes = @ConstructorResult(targetClass = TypeServiceReseauOrganisationDTO.class,
            columns = {
                 @ColumnResult(name = "id", type = Long.class)
                ,
                    @ColumnResult(name = "code_pays", type = String.class)
                ,
                    @ColumnResult(name = "code_pv", type = String.class)
                ,
                    @ColumnResult(name = "type_service_id", type = Long.class)
                ,
                    @ColumnResult(name = "unite_organisation_id", type = Long.class)
                ,
                    @ColumnResult(name = "libelle_unite_organisation", type = String.class)
                ,
                    @ColumnResult(name = "libelle", type = String.class)
        }))
        
})
@NamedNativeQueries({
    @NamedNativeQuery(name = "TypeServiceReseauOrganisation.findAllTypeSerResOrg",
            query = "SELECT tsro.id,tsro.code_pays,tsro.code_pv,tsro.type_service_id,tsro.unite_organisation_id,uo.libelle_unite_organisation,ts.libelle FROM public.type_service_reseau_organisation tsro inner join type_service ts on ts.id = tsro.type_service_id inner join unite_organisation uo on uo.id = tsro.unite_organisation_id",
            resultSetMapping = "Mapping.TypeServiceReseauOrganisationDTO",hints={@QueryHint(name="org.hibernate.cacheable",value="false")})
    ,
})
@Entity
@Table(name = "type_service_reseau_organisation")
public class TypeServiceReseauOrganisation implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "code_pays")
    String codePays;
      
    @ManyToOne
    private TypeService typeService;

    @ManyToOne
    private UniteOrganisation uniteOrganisation;
    @Column(name = "code_pv")
  private  String codePv;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodePays() {
        return codePays;
    }

    public void setCodePays(String codePays) {
        this.codePays = codePays;
    }

    public UniteOrganisation getUniteOrganisation() {
        return uniteOrganisation;
    }

    public void setUniteOrganisation(UniteOrganisation uniteOrganisation) {
        this.uniteOrganisation = uniteOrganisation;
    }

    public TypeService getTypeService() {
        return typeService;
    }

    public void setTypeService(TypeService typeService) {
        this.typeService = typeService;
    }

    public String getCodePv() {
        return codePv;
    }

    public void setCodePv(String codePv) {
        this.codePv = codePv;
    }

}
