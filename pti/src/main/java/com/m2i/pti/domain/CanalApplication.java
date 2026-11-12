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
import jakarta.persistence.NamedNativeQueries;
import jakarta.persistence.NamedNativeQuery;
import jakarta.persistence.QueryHint;
import jakarta.persistence.SqlResultSetMapping;
import jakarta.persistence.SqlResultSetMappings;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.m2i.pti.domain.enumeration.TypeCanal;
import com.m2i.pti.service.dto.CanalApplicationDTO;



@SqlResultSetMappings(value = {
    @SqlResultSetMapping(name = "Mapping.CanalApplicationDTO",
            classes = @ConstructorResult(targetClass = CanalApplicationDTO.class,
                    columns = {
                        @ColumnResult(name = "id", type = Long.class),
                        @ColumnResult(name = "libelle_canal", type = String.class),
                        @ColumnResult(name = "type_canal", type = String.class),                       
                    }))
}) 
@NamedNativeQueries({
    @NamedNativeQuery(name = "CanalApplication.findCanalApplication",
            query = "SELECT canal.id,canal.libelle_canal,canal.type_canal FROM canal_application canal order by canal.id desc",
            resultSetMapping = "Mapping.CanalApplicationDTO", 
            hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})
                ,
    @NamedNativeQuery(name = "CanalApplication.findCanalApplicationStatut",
    query = "SELECT canal.id,canal.libelle_canal,canal.type_canal FROM canal_application canal where canal.type_canal = CAST(:canal AS TEXT) order by canal.id desc",
    resultSetMapping = "Mapping.CanalApplicationDTO", 
    hints = {
        @QueryHint(name = "org.hibernate.cacheable", value = "false")}) 
            })
@Entity
@Table(name = "canal_application")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class CanalApplication extends AbstractAuditingEntity  implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "libelle_canal", nullable = false)  
    private String libelleCanal; 

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type_canal", nullable = false)
    private TypeCanal typeCanal;

    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    
    public String getLibelleCanal() {
        return libelleCanal;
    }

    public void setLibelleCanal(String libelleCanal) {
        this.libelleCanal = libelleCanal;
    }

    public TypeCanal getTypeCanal() {
        return typeCanal;
    }

    public void setTypeCanal(TypeCanal typeCanal) {
        this.typeCanal = typeCanal;
    }

    public CanalApplication(Long id, String libelleCanal, TypeCanal typeCanal) {
        this.id = id;
        this.libelleCanal = libelleCanal;
        this.typeCanal = typeCanal;
    }

    public CanalApplication() {
    }

    

}
