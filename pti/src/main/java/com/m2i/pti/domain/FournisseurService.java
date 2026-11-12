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
 * A FournisseurService.
 */
@Entity
@Table(name = "fournisseur_service")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class FournisseurService extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "code_fse", nullable = false)
    private String codeFse;

    @NotNull
    @Column(name = "libelle_fse", nullable = false)
    private String libelleFse;

    @OneToMany(mappedBy = "fournisseur")
    @JsonIgnore
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<UniteOrganisation> organisations = new HashSet<>();

    @ManyToOne
    private Pays pays;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodeFse() {
        return codeFse;
    }

    public FournisseurService codeFse(String codeFse) {
        this.codeFse = codeFse;
        return this;
    }

    public void setCodeFse(String codeFse) {
        this.codeFse = codeFse;
    }

    public String getLibelleFse() {
        return libelleFse;
    }

    public FournisseurService libelleFse(String libelleFse) {
        this.libelleFse = libelleFse;
        return this;
    }

    public void setLibelleFse(String libelleFse) {
        this.libelleFse = libelleFse;
    }

    public Set<UniteOrganisation> getOrganisations() {
        return organisations;
    }

    public FournisseurService organisations(Set<UniteOrganisation> uniteOrganisations) {
        this.organisations = uniteOrganisations;
        return this;
    }

    public FournisseurService addOrganisation(UniteOrganisation uniteOrganisation) {
        this.organisations.add(uniteOrganisation);
        uniteOrganisation.setFournisseur(this);
        return this;
    }

    public FournisseurService removeOrganisation(UniteOrganisation uniteOrganisation) {
        this.organisations.remove(uniteOrganisation);
        uniteOrganisation.setFournisseur(null);
        return this;
    }

    public void setOrganisations(Set<UniteOrganisation> uniteOrganisations) {
        this.organisations = uniteOrganisations;
    }

    public Pays getPays() {
        return pays;
    }

    public FournisseurService pays(Pays pays) {
        this.pays = pays;
        return this;
    }

    public void setPays(Pays pays) {
        this.pays = pays;
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
        FournisseurService fournisseurService = (FournisseurService) o;
        if (fournisseurService.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), fournisseurService.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "FournisseurService{" +
            "id=" + getId() +
            ", codeFse='" + getCodeFse() + "'" +
            ", libelleFse='" + getLibelleFse() + "'" +
            "}";
    }
}
