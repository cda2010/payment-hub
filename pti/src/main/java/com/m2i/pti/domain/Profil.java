package com.m2i.pti.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.m2i.pti.config.audit.EntityAuditEventListener;
import com.m2i.pti.service.dto.ProfilDTO;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A Profil.
 */
@SqlResultSetMappings(value = {
        @SqlResultSetMapping(name = "Mapping.ProfilDTO",
                classes = @ConstructorResult(targetClass = ProfilDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = Long.class),
                                @ColumnResult(name = "description", type = String.class),
                                @ColumnResult(name = "nom", type = String.class),
                                @ColumnResult(name = "typeUOId", type = Long.class),
                                @ColumnResult(name = "typeUONom", type = String.class),

                        }))
})


@NamedNativeQueries({
        @NamedNativeQuery(name = "Profil.findAllProfil",
                query = "select p.id, p.description, p.nom, p.typeuO_id as typeUOId,tuo.code_type_uo as typeUONom "
                        + "from profil p ,type_unite_organisation tuo  "
                        + " where p.typeuO_id=tuo.id limit :taille offset :page",
                resultSetMapping = "Mapping.ProfilDTO",hints={@QueryHint(name="org.hibernate.cacheable",value="false")}),

        @NamedNativeQuery(name = "Profil.findByTuoId",
                query = "select p.id, p.description, p.nom, p.typeuO_id as typeUOId,tuo.code_type_uo as typeUONom "
                        + "from profil p ,type_unite_organisation tuo  "
                        + " where p.typeuO_id=tuo.id and p.typeuo_id = CAST(CAST (:typeUoId AS TEXT) AS BIGINT )",
                resultSetMapping = "Mapping.ProfilDTO",hints={@QueryHint(name="org.hibernate.cacheable",value="false")})
})


@Entity
@Table(name = "profil")
@EntityListeners(value = {EntityAuditEventListener.class})
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Profil extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(name = "description")
    private String description;

    @OneToMany(mappedBy = "profil")
    @JsonIgnore
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<User> users = new HashSet<>();

    @ManyToOne
    private UniteOrganisation organisation;

    @ManyToOne
    private TypeUniteOrganisation typeUO;


    @JsonIgnore
    @ManyToMany
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @JoinTable(name = "profil_role",
        joinColumns = @JoinColumn(name="profils_id", referencedColumnName="id"),
        inverseJoinColumns = @JoinColumn(name="authority_name", referencedColumnName="name"))
    private Set<Authority> roles = new HashSet<>();


    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public Profil nom(String nom) {
        this.nom = nom;
        return this;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public Profil description(String description) {
        this.description = description;
        return this;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }


    public UniteOrganisation getOrganisation() {
        return organisation;
    }

    public void setOrganisation(UniteOrganisation organisation) {
        this.organisation = organisation;
    }

    public Set<Authority> getRoles() {
        return roles;
    }

    public void setRoles(Set<Authority> roles) {
        this.roles = roles;
    }

    public TypeUniteOrganisation getTypeUO() {
        return typeUO;
    }

    public void setTypeUO(TypeUniteOrganisation typeUO) {
        this.typeUO = typeUO;
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
        Profil profil = (Profil) o;
        if (profil.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), profil.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "Profil{" +
            "id=" + getId() +
            ", nom='" + getNom() + "'" +
            ", description='" + getDescription() + "'" +
            "}";
    }
}
