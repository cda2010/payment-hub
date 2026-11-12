package com.m2i.pti.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

/**
 * A Pays.
 */
@Entity
@Table(name = "pays")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Pays extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "code_pays", nullable = false)
    private String codePays;

    @Column(name = "libelle_pays")
    private String libellePays;

   

    @OneToMany(mappedBy = "pays")
    @JsonIgnore
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<FournisseurService> fournisseurServices = new HashSet<>();

    @ManyToOne
    private Devise devise;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodePays() {
        return codePays;
    }

    public Pays codePays(String codePays) {
        this.codePays = codePays;
        return this;
    }

    public void setCodePays(String codePays) {
        this.codePays = codePays;
    }

    public String getLibellePays() {
        return libellePays;
    }

    public Pays libellePays(String libellePays) {
        this.libellePays = libellePays;
        return this;
    }

    public void setLibellePays(String libellePays) {
        this.libellePays = libellePays;
    }

   

   

    public Set<FournisseurService> getFournisseurServices() {
        return fournisseurServices;
    }

    public Pays fournisseurServices(Set<FournisseurService> fournisseurServices) {
        this.fournisseurServices = fournisseurServices;
        return this;
    }

    public Pays addFournisseurService(FournisseurService fournisseurService) {
        this.fournisseurServices.add(fournisseurService);
        fournisseurService.setPays(this);
        return this;
    }

    public Pays removeFournisseurService(FournisseurService fournisseurService) {
        this.fournisseurServices.remove(fournisseurService);
        fournisseurService.setPays(null);
        return this;
    }

    public void setFournisseurServices(Set<FournisseurService> fournisseurServices) {
        this.fournisseurServices = fournisseurServices;
    }

    public Devise getDevise() {
        return devise;
    }

    public Pays devise(Devise devise) {
        this.devise = devise;
        return this;
    }

    public void setDevise(Devise devise) {
        this.devise = devise;
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
        Pays pays = (Pays) o;
        if (pays.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), pays.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "Pays{" +
            "id=" + getId() +
            ", codePays='" + getCodePays() + "'" +
            ", libellePays='" + getLibellePays() + "'" +
            "}";
    }
}
