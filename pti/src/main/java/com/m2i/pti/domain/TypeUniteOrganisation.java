package com.m2i.pti.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.m2i.pti.service.dto.TypeUniteOrganisationDTO;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;
@NamedNativeQueries({
@NamedNativeQuery(name = "TypeUniteOrganisation.findOne",
        query = "select  id , code_type_uo codeTypeUO, libelle_type_uo libelleTypeUO , niveau  from type_unite_organisation where id =:id ",
        resultSetMapping = "Mapping.TypeUniteOrganisationDTO",hints={@QueryHint(name="org.hibernate.cacheable",value="false")}),

    @NamedNativeQuery(name = "TypeUniteOrganisation.findCodetype",
        query = "select  id , code_type_uo codeTypeUO, libelle_type_uo libelleTypeUO , niveau  from type_unite_organisation where code_type_uo =:code ",
        resultSetMapping = "Mapping.TypeUniteOrganisationDTO",hints={@QueryHint(name="org.hibernate.cacheable",value="false")}),

    @NamedNativeQuery(name = "TypeUniteOrganisation.findLibelleTypeUO",
                query = "select  id , code_type_uo codeTypeUO, libelle_type_uo libelleTypeUO , niveau  from type_unite_organisation where libelle_type_uo =:libelle ",
                resultSetMapping = "Mapping.TypeUniteOrganisationDTO",hints={@QueryHint(name="org.hibernate.cacheable",value="false")}),


    @NamedNativeQuery(name = "TypeUniteOrganisation.findAllLibelleTypeUO",
                query = "select  id , code_type_uo codeTypeUO, libelle_type_uo libelleTypeUO , niveau  from type_unite_organisation where libelle_type_uo  in (:libelle) ",
                resultSetMapping = "Mapping.TypeUniteOrganisationDTO",hints={@QueryHint(name="org.hibernate.cacheable",value="false")}),


        @NamedNativeQuery(name = "TypeUniteOrganisation.findAllTUO",
                query = "select  id , code_type_uo codeTypeUO, libelle_type_uo libelleTypeUO , niveau  from type_unite_organisation limit :taille offset :page",
                resultSetMapping = "Mapping.TypeUniteOrganisationDTO",hints={@QueryHint(name="org.hibernate.cacheable",value="false")}),


    @NamedNativeQuery(name = "TypeUniteOrganisation.findListByNiveauOptimise",
                query = "select  id , code_type_uo codeTypeUO, libelle_type_uo libelleTypeUO , niveau  from type_unite_organisation where niveau= CAST(CAST (:niveau AS TEXT) AS integer )",
                resultSetMapping = "Mapping.TypeUniteOrganisationDTO",hints={@QueryHint(name="org.hibernate.cacheable",value="false")}),

    @NamedNativeQuery(name = "TypeUniteOrganisation.findByNiveau",
                query = "select  id , code_type_uo codeTypeUO, libelle_type_uo libelleTypeUO , niveau  from type_unite_organisation where niveau != -1 ",
                resultSetMapping = "Mapping.TypeUniteOrganisationDTO",hints={@QueryHint(name="org.hibernate.cacheable",value="false")}),






})
@SqlResultSetMappings(value = {
@SqlResultSetMapping(name = "Mapping.TypeUniteOrganisationDTO",
        classes = @ConstructorResult(targetClass = TypeUniteOrganisationDTO.class,
                columns = {
                    @ColumnResult(name = "id", type = Long.class),
                    @ColumnResult(name = "codeTypeUO", type = String.class),
                    @ColumnResult(name = "libelleTypeUO", type=String.class),
                    @ColumnResult(name = "niveau", type = Integer.class)
                   
                   
                }))
})

/**
 * A TypeUniteOrganisation.
 */
@Entity
@Table(name = "type_unite_organisation")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class TypeUniteOrganisation extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "libelle_type_uo")
    private String libelleTypeUO;

    @Column(name = "code_type_uo")
    private String codeTypeUO;

    @Column(name = "niveau")
    private Integer niveau;

   



    @ManyToMany(fetch = FetchType.EAGER)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @JoinTable(name = "type_unite_organisation_role",
        joinColumns = @JoinColumn(name="type_unite_organisation_id", referencedColumnName="id"),
        inverseJoinColumns = @JoinColumn(name="authority_name", referencedColumnName="name"))
    private Set<Authority> roles = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLibelleTypeUO() {
        return libelleTypeUO;
    }

    public TypeUniteOrganisation libelleTypeUO(String libelleTypeUO) {
        this.libelleTypeUO = libelleTypeUO;
        return this;
    }

    public void setLibelleTypeUO(String libelleTypeUO) {
        this.libelleTypeUO = libelleTypeUO;
    }

    public String getCodeTypeUO() {
        return codeTypeUO;
    }

    public TypeUniteOrganisation codeTypeUO(String codeTypeUO) {
        this.codeTypeUO = codeTypeUO;
        return this;
    }

    public void setCodeTypeUO(String codeTypeUO) {
        this.codeTypeUO = codeTypeUO;
    }

    public Integer getNiveau() {
        return niveau;
    }

    public TypeUniteOrganisation niveau(Integer niveau) {
        this.niveau = niveau;
        return this;
    }

    public void setNiveau(Integer niveau) {
        this.niveau = niveau;
    }

    

  
   

    public Set<Authority> getRoles() {
        return roles;
    }

    public void setRoles(Set<Authority> roles) {
        this.roles = roles;
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
        TypeUniteOrganisation typeUniteOrganisation = (TypeUniteOrganisation) o;
        if (typeUniteOrganisation.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), typeUniteOrganisation.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "TypeUniteOrganisation{" +
            "id=" + getId() +
            ", libelleTypeUO='" + getLibelleTypeUO() + "'" +
            ", codeTypeUO='" + getCodeTypeUO() + "'" +
            ", niveau=" + getNiveau() +
            "}";
    }
}
