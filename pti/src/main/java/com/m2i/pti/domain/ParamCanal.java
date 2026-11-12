package com.m2i.pti.domain;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.ColumnResult;
import jakarta.persistence.ConstructorResult;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

import com.m2i.pti.domain.enumeration.Application;
import com.m2i.pti.service.dto.ParamCanalDTO;

@SqlResultSetMappings(value = {
    @SqlResultSetMapping(name = "Mapping.ParamCanalDTO",
            classes = @ConstructorResult(targetClass = ParamCanalDTO.class,
                    columns = {
                        @ColumnResult(name = "param_canal_id", type = Long.class),
                        @ColumnResult(name = "application", type = String.class),
                        @ColumnResult(name = "canal", type = String.class),                       
                        @ColumnResult(name = "sousCanal", type = String.class),
                        @ColumnResult(name = "description", type = String.class),
                        @ColumnResult(name = "uniteOrganisation", type = String.class)
                    }))
})
@NamedNativeQueries({
    @NamedNativeQuery(name = "ParamCanal.findParamCanal",
            query = "SELECT prcanal.id as param_canal_id,prcanal.application,canal , sous_canal as sousCanal ,typs.description,uo.libelle_unite_organisation as uniteOrganisation FROM public.param_canal prcanal left join type_service typs on typs.id = prcanal.type_service_id left join unite_organisation uo on uo.id = prcanal.unite_organisation_id order by poids desc",
            resultSetMapping = "Mapping.ParamCanalDTO", 
            hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})
    ,
    @NamedNativeQuery(name = "ParamCanal.findParamCanalStatut",
            query = "SELECT canal.id,canal.application,canal.type_canal FROM canal_application canal where (canal.application = CAST(:application AS TEXT) and canal.type_canal = CAST(:canal AS TEXT)) order by canal.id desc",
            resultSetMapping = "Mapping.ParamCanalDTO",
            hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})
})

@Entity
@Table(name = "param_canal")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class ParamCanal extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "application")
    private Application application;
    @Column(name = "canal")
    private String canal;
    @Column(name = "sousCanal")
    private String sousCanal;

    @ManyToOne
    private TypeService typeService;

    @ManyToOne
    private UniteOrganisation uniteOrganisation;

    @Column(name = "poids")
    private Long poids;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

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

    public Long getPoids() {
        return poids;
    }

    public void setPoids(Long poids) {
        this.poids = poids;
    }

    public String getCanal() {
        return canal;
    }

    public void setCanal(String canal) {
        this.canal = canal;
    }

    public String getSousCanal() {
        return sousCanal;
    }

    public void setSousCanal(String sousCanal) {
        this.sousCanal = sousCanal;
    }

}
