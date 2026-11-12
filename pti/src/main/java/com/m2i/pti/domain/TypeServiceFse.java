/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.domain;

import java.io.Serializable;

import jakarta.persistence.Cacheable;
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

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.m2i.pti.mobile.dto.MtypeService;
import com.m2i.pti.service.dto.TypeServiceFseDTO;

/**
 *
 * @author cda
 */
@SqlResultSetMappings(value = {
    @SqlResultSetMapping(name = "Mapping.TypeServiceFse",
            classes = @ConstructorResult(targetClass = MtypeService.class,
                    columns = {
                        @ColumnResult(name = "codeUo", type = String.class),
                        @ColumnResult(name = "code", type = String.class)
                        ,
                        @ColumnResult(name = "libelle", type = String.class)
                        ,
                        @ColumnResult(name = "ctxCodeFse", type = String.class)
                        ,
                          @ColumnResult(name = "ctxCodePv", type = String.class)

                    }))
    ,
    @SqlResultSetMapping(name = "Mapping.TypeServiceFseDTO",
            classes = @ConstructorResult(targetClass = TypeServiceFseDTO.class,
                    columns = {
                        @ColumnResult(name = "id", type = Long.class)
                        ,
                @ColumnResult(name = "type_service_id", type = Long.class)
                        ,
                @ColumnResult(name = "unite_organisation_id", type = Long.class)
                ,
                @ColumnResult(name = "code_uo", type = String.class)
                        ,
                @ColumnResult(name = "libelle_unite_organisation", type = String.class)
                        ,
                @ColumnResult(name = "code", type = String.class)
                        ,
                @ColumnResult(name = "description", type = String.class)
            })),
    @SqlResultSetMapping(name = "Mapping.TypeServiceFseDTOForUo",
    classes = @ConstructorResult(targetClass = TypeServiceFseDTO.class,
            columns = {
                @ColumnResult(name = "id", type = Long.class)
                ,
                @ColumnResult(name = "type_service_id", type = Long.class)
                ,
                @ColumnResult(name = "unite_organisation_id", type = Long.class)
                ,
                @ColumnResult(name = "code_fse", type = String.class)
                ,
                @ColumnResult(name = "code_uo", type = String.class)
                ,
                @ColumnResult(name = "libelle_unite_organisation", type = String.class)
                ,
                @ColumnResult(name = "code", type = String.class)
                ,
                @ColumnResult(name = "description", type = String.class)
                    }))
})
@NamedNativeQueries({
    @NamedNativeQuery(name = "typeServiceGroupe.findAllTypeService",
            query = "select  uo.code_uo as  codeUo, ts.code, ts.description as libelle, context_code_fse as ctxCodeFse,   context_code_pv as ctxCodePv    from type_service_fse tsf, categorie_service c, unite_organisation uo, type_service ts "
            + " where tsf.type_service_id= ts.id and uo.id= tsf.unite_organisation_id and c.id= ts.categorie_service_id and uo.code_uo=:codeUo and c.id=:categorieServiceId",
            resultSetMapping = "Mapping.TypeServiceFse",
            hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")}),
        
        @NamedNativeQuery(name = "typeServiceGroupe.findAllByTypeService",
            query = "select  uo.code_uo as  codeUo ,ts.code, ts.description as libelle,context_code_fse as ctxCodeFse,   context_code_pv as ctxCodePv    from type_service_fse tsf, unite_organisation uo, type_service ts "
            + " where tsf.type_service_id= ts.id and uo.id= tsf.unite_organisation_id and ts.code=:codeService",
            resultSetMapping = "Mapping.TypeServiceFse",
            hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})
    ,
     @NamedNativeQuery(name = "typeServiceGroupe.findCategorieTypeService",
            query = "select uo.code_uo as  codeUo ,ts.code, ts.description as libelle, context_code_fse as ctxCodeFse,   context_code_pv as ctxCodePv    from type_service_fse tsf, categorie_service c, unite_organisation uo, type_service ts "
            + " where tsf.type_service_id= ts.id and uo.id= tsf.unite_organisation_id and c.id= ts.categorie_service_id  and c.id=:categorieServiceId",
            resultSetMapping = "Mapping.TypeServiceFse",
            hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")}),
    @NamedNativeQuery(name = "typeServiceFse.findAllTypeServiceFse",
            query = "select tsf.id, tsf.type_service_id, tsf.unite_organisation_id, uo.code_fse, uo.code_uo, uo.libelle_unite_organisation, ts.code, ts.description from type_service_fse tsf inner join unite_organisation uo on uo.id=tsf.unite_organisation_id inner join type_service ts on ts.id= tsf.type_service_id",
            //resultSetMapping = "Mapping.TypeServiceFseDTO",
            hints = {
                @QueryHint(name = "org.hibernate.cacheable", value= "false")}),
    @NamedNativeQuery(name="typeServiceFse.findOneTypeService",
            query = "select tsf.id, tsf.type_service_id, tsf.unite_organisation_id, uo.code_fse, uo.code_uo, uo.libelle_unite_organisation, ts.code, ts.description from type_service_fse tsf inner join unite_organisation uo on uo.id=tsf.unite_organisation_id inner join type_service ts on ts.id= tsf.type_service_id where tsf.unite_organisation_id =:uniteOrganisationId",
            resultSetMapping = "Mapping.TypeServiceFseDTOForUo",
            hints = {
                @QueryHint(name = "org.hibernate.cacheable", value= "false")}),
        })

@Entity
@Table(name = "type_service_fse")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Cacheable(value = true)
public class TypeServiceFse extends AbstractAuditingEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String contextCodePv;
    private String contextCodeFse;
    @ManyToOne
    private TypeService typeService;

    @ManyToOne
    private UniteOrganisation uniteOrganisation;

    public TypeService getTypeService() {
        return typeService;
    }

    public void setTypeService(TypeService typeService) {
        this.typeService = typeService;
    }

    public UniteOrganisation getUniteOrganisation() {
        return uniteOrganisation;
    }

    public void setUniteOrganisation(UniteOrganisation uniteOrganisation) {
        this.uniteOrganisation = uniteOrganisation;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContextCodeFse() {
        return contextCodeFse;
    }

    public void setContextCodeFse(String contextCodeFse) {
        this.contextCodeFse = contextCodeFse;
    }

    public String getContextCodePv() {
        return contextCodePv;
    }

    public void setContextCodePv(String contextCodePv) {
        this.contextCodePv = contextCodePv;
    }

}
