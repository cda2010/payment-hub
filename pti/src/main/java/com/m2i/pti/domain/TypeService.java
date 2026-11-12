package com.m2i.pti.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.m2i.pti.domain.enumeration.EtiquetteCompteCode;
import com.m2i.pti.service.dto.TypeServiceDTO;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

@NamedNativeQueries({
    @NamedNativeQuery(name = "TypeService.findByCodeService",
            query = "select id, code, libelle, description,etiquete_compte etiquetteCompteCode, "
            + "frais_applicable fraisApplicable, operation_client operationClient, "
            + "cmpte_client_sournce cmpteClientSournce ,cmpte_client_dest cmpteClientDest  from type_service  "
            + " where  code=:code and is_active=1",
            resultSetMapping = "Mapping.TypeServiceDTO", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})
    ,
   @NamedNativeQuery(name = "TypeService.findByCodeService2",
            query = "select id, code, libelle, description,etiquete_compte etiquetteCompteCode, "
            + "frais_applicable fraisApplicable, operation_client operationClient, "
            + "cmpte_client_sournce cmpteClientSournce ,cmpte_client_dest cmpteClientDest  from type_service  "
            + " where  code=:code",
            resultSetMapping = "Mapping.TypeServiceDTO", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})
    ,
        @NamedNativeQuery(name = "TypeService.findAllTypeService",
            query = "select id, code, libelle, description,etiquete_compte etiquetteCompteCode, "
            + "frais_applicable fraisApplicable, operation_client operationClient, "
            + "cmpte_client_sournce cmpteClientSournce ,cmpte_client_dest cmpteClientDest  from type_service",
            resultSetMapping = "Mapping.TypeServiceDTO", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})
    ,

 @NamedNativeQuery(name = "TypeService.findByCategorieService",
            query = "select ts.id, ts.code, ts.libelle, ts.description,etiquete_compte etiquetteCompteCode, "
            + "frais_applicable fraisApplicable, operation_client operationClient, "
            + "cmpte_client_sournce cmpteClientSournce ,cmpte_client_dest cmpteClientDest  from type_service ts, categorie_service cat  "
            + " where  cat.id=:idCategorieService and cat.id=ts.categorie_service_id",
            resultSetMapping = "Mapping.TypeServiceDTO", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})
    ,

        @NamedNativeQuery(name = "TypeService.findAllTypeServiceCat",
            query = "select tpservice.id, tpservice.code, tpservice.libelle, tpservice.description,tpservice.etiquete_compte as etiquetteCompteCode, "
            + "tpservice.frais_applicable as fraisApplicable, tpservice.operation_client as operationClient, "
            + "tpservice.cmpte_client_sournce as cmpteClientSournce ,tpservice.cmpte_client_dest as cmpteClientDest,cat.id as cat_id,cat.code as cat_code,cat.libelle as cat_libelle  from type_service tpservice left join categorie_service cat on cat.id=tpservice.categorie_service_id",
            resultSetMapping = "Mapping.TypeServiceCategirieDTO", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})
    ,
        @NamedNativeQuery(name = "TypeService.findOne",
            query = "select id, code, libelle, etiquete_compte etiquetteCompteCode,operation_client operationClient "
            + "from type_service where id= CAST(CAST (:id AS TEXT) AS BIGINT )",
            resultSetMapping = "Mapping.TypeServiceFindOneDTO", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})

})

@SqlResultSetMappings({
    @SqlResultSetMapping(name = "Mapping.TypeServiceDTO",
            classes = @ConstructorResult(targetClass = TypeServiceDTO.class,
                    columns = {
                        @ColumnResult(name = "id", type = Long.class)
                        ,
                                @ColumnResult(name = "code", type = String.class)
                        ,
                                @ColumnResult(name = "libelle", type = String.class)
                        ,
                                @ColumnResult(name = "description", type = String.class)
                        ,
                                @ColumnResult(name = "etiquetteCompteCode", type = String.class)
                        ,
                                @ColumnResult(name = "fraisApplicable", type = Boolean.class)
                        ,
                                @ColumnResult(name = "operationClient", type = Boolean.class)
                        ,
                                @ColumnResult(name = "cmpteClientSournce", type = Boolean.class)
                        ,
                                @ColumnResult(name = "cmpteClientDest", type = Boolean.class)

                    }))
    ,
 @SqlResultSetMapping(name = "Mapping.TypeServiceCategirieDTO",
            classes = @ConstructorResult(targetClass = TypeServiceDTO.class,
                    columns = {
                        @ColumnResult(name = "id", type = Long.class)
                        ,
                                @ColumnResult(name = "code", type = String.class)
                        ,
                                @ColumnResult(name = "libelle", type = String.class)
                        ,
                                @ColumnResult(name = "description", type = String.class)
                        ,
                                @ColumnResult(name = "etiquetteCompteCode", type = String.class)
                        ,
                                @ColumnResult(name = "fraisApplicable", type = Boolean.class)
                        ,
                                @ColumnResult(name = "operationClient", type = Boolean.class)
                        ,
                                @ColumnResult(name = "cmpteClientSournce", type = Boolean.class)
                        ,
                                @ColumnResult(name = "cmpteClientDest", type = Boolean.class)
                        ,
                                @ColumnResult(name = "cat_id", type = Long.class)
                        ,
                                @ColumnResult(name = "cat_code", type = String.class)
                        ,
                                @ColumnResult(name = "cat_libelle", type = String.class),}))
    ,
        @SqlResultSetMapping(name = "Mapping.TypeServiceFindOneDTO",
            classes = @ConstructorResult(targetClass = TypeServiceDTO.class,
                    columns = {
                        @ColumnResult(name = "id", type = Long.class)
                        ,
                                @ColumnResult(name = "code", type = String.class)
                        ,
                                @ColumnResult(name = "libelle", type = String.class)
                        ,
                                @ColumnResult(name = "etiquetteCompteCode", type = String.class)
                        ,
                                @ColumnResult(name = "operationClient", type = Boolean.class)

                    }))

})

/**
 * A TypeService.
 */
@Entity
@Table(name = "type_service")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class TypeService extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "description")
    private String description;
    @NotNull
    @Column(name = "libelle", nullable = false)
    private String libelle;

    @Column(name = "cmpte_client_sournce")
    private Boolean cmpteClientSournce;

    @Column(name = "cmpte_client_dest")
    private Boolean cmpteClientDest;

    @Column(name = "operation_client")
    private Boolean operationClient;

    @Column(name = "frais_applicable")
    private Boolean fraisApplicable;

    @Column(name = "code")
    private String code;
    @Column(name = "is_active")
    private int actif;

    @Enumerated(EnumType.STRING)
    @Column(name = "etiquete_compte")
    private EtiquetteCompteCode etiquetteCompteCode;

    @ManyToOne
    // @JsonIgnore
    private CategorieService categorieService;

    /*@ManyToMany(mappedBy = "typeServices")
    @JsonIgnore
    @Cache(usage = CacheConcurrencyStrategy.NONE)
    private Set<ServiceProfil> serviceProfils = new HashSet<>();*/
    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLibelle() {
        return libelle;
    }

    public TypeService libelle(String libelle) {
        this.libelle = libelle;
        return this;
    }

    public EtiquetteCompteCode getEtiquetteCompteCode() {
        return etiquetteCompteCode;
    }

    public void setEtiquetteCompteCode(EtiquetteCompteCode etiquetteCompteCode) {
        this.etiquetteCompteCode = etiquetteCompteCode;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public String getCode() {
        return code;
    }

    public TypeService code(String code) {
        this.code = code;
        return this;
    }

    public TypeService etiquetteCompteCode(EtiquetteCompteCode etiquetteCompteCode) {
        this.etiquetteCompteCode = etiquetteCompteCode;
        return this;
    }

    /*public Set<ServiceProfil> getServiceProfils() {
        return serviceProfils;
    }

    public void setServiceProfils(Set<ServiceProfil> serviceProfils) {
        this.serviceProfils = serviceProfils;
    }*/
    public void setCode(String code) {
        this.code = code;
    }

    public Boolean isCmpteClientSournce() {
        return cmpteClientSournce;
    }

    public TypeService cmpteClientSournce(Boolean cmpteClientSournce) {
        this.cmpteClientSournce = cmpteClientSournce;
        return this;
    }

    public void setCmpteClientSournce(Boolean cmpteClientSournce) {
        this.cmpteClientSournce = cmpteClientSournce;
    }

    public Boolean isCmpteClientDest() {
        return cmpteClientDest;
    }

    public TypeService cmpteClientDest(Boolean cmpteClientDest) {
        this.cmpteClientDest = cmpteClientDest;
        return this;
    }

    public void setCmpteClientDest(Boolean cmpteClientDest) {
        this.cmpteClientDest = cmpteClientDest;
    }

    public Boolean isOperationClient() {
        return operationClient;
    }

    public TypeService operationClient(Boolean operationClient) {
        this.operationClient = operationClient;
        return this;
    }

    public void setOperationClient(Boolean operationClient) {
        this.operationClient = operationClient;
    }

    public Boolean isFraisApplicable() {
        return fraisApplicable;
    }

    public TypeService fraisApplicable(Boolean fraisApplicable) {
        this.fraisApplicable = fraisApplicable;
        return this;
    }

    public void setFraisApplicable(Boolean fraisApplicable) {
        this.fraisApplicable = fraisApplicable;
    }

    public Boolean getCmpteClientSournce() {
        return cmpteClientSournce;
    }

    public Boolean getCmpteClientDest() {
        return cmpteClientDest;
    }

    public Boolean getOperationClient() {
        return operationClient;
    }

    public Boolean getFraisApplicable() {
        return fraisApplicable;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getActif() {
        return actif;
    }

    public void setActif(int actif) {
        this.actif = actif;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TypeService typeService = (TypeService) o;
        if (typeService.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), typeService.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "TypeService{"
                + "id=" + getId()
                + ", libelle='" + getLibelle() + "'"
                + ", cmpteClientSournce='" + isCmpteClientSournce() + "'"
                + ", cmpteClientDest='" + isCmpteClientDest() + "'"
                + ", operationClient='" + isOperationClient() + "'"
                + ", fraisApplicable='" + isFraisApplicable() + "'"
                + "}";
    }

    public TypeService(Long id, String code) {
        this.id = id;
        this.code = code;
    }

    public TypeService(Long id, String description, String libelle, Boolean cmpteClientSournce, Boolean cmpteClientDest,
            Boolean operationClient, Boolean fraisApplicable, String code, EtiquetteCompteCode etiquetteCompteCode) {
        this.id = id;
        this.description = description;
        this.libelle = libelle;
        this.cmpteClientSournce = cmpteClientSournce;
        this.cmpteClientDest = cmpteClientDest;
        this.operationClient = operationClient;
        this.fraisApplicable = fraisApplicable;
        this.code = code;
        this.etiquetteCompteCode = etiquetteCompteCode;
    }

    public TypeService() {
    }

}
