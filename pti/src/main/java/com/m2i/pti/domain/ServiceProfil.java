package com.m2i.pti.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.m2i.pti.domain.enumeration.TypeProfilService;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

/**
 * A ServiceProfil.
 */
@Entity
@Table(name = "service_profil")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Cacheable(value = false)
public class ServiceProfil extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(name = "type_profil_service")
    private TypeProfilService typeProfilService;

    @Column(name = "description")
    private String description;

    @ManyToMany
    @Cache(usage = CacheConcurrencyStrategy.NONE)
    @JoinTable(name = "service_profil_type_service",
               joinColumns = @JoinColumn(name="service_profils_id", referencedColumnName="id"),
               inverseJoinColumns = @JoinColumn(name="type_services_id", referencedColumnName="id"))
    private Set<TypeService> typeServices = new HashSet<>();

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

    public ServiceProfil nom(String nom) {
        this.nom = nom;
        return this;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public ServiceProfil description(String description) {
        this.description = description;
        return this;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<TypeService> getTypeServices() {
        return typeServices;
    }

    public ServiceProfil typeServices(Set<TypeService> typeServices) {
        this.typeServices = typeServices;
        return this;
    }

   

    /*public ServiceProfil addTypeService(TypeService typeService) {
        this.typeServices.add(typeService);
        typeService.getServiceProfils().add(this);
        return this;
    }

    public ServiceProfil removeTypeService(TypeService typeService) {
        this.typeServices.remove(typeService);
        typeService.getServiceProfils().remove(this);
        return this;
    }*/

    public TypeProfilService getTypeProfilService() {
        return typeProfilService;
    }

    public void setTypeProfilService(TypeProfilService typeProfilService) {
        this.typeProfilService = typeProfilService;
    }

    public void setTypeServices(Set<TypeService> typeServices) {
        this.typeServices = typeServices;
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
        ServiceProfil serviceProfil = (ServiceProfil) o;
        if (serviceProfil.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), serviceProfil.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "ServiceProfil{" +
            "id=" + getId() +
            ", nom='" + getNom() + "'" +
            ", description='" + getDescription() + "'" +
            "}";
    }
}
