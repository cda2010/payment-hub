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
 * A TypeCompte.
 */
@Entity
@Table(name = "type_compte")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class TypeCompte extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "code_type_compte", nullable = false)
    private String codeTypeCompte;

    @NotNull
    @Column(name = "libelle_type_compte", nullable = false)
    private String libelleTypeCompte;

    @OneToMany(mappedBy = "typeCompte")
    @JsonIgnore
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<Compte> comptes = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodeTypeCompte() {
        return codeTypeCompte;
    }

    public TypeCompte codeTypeCompte(String codeTypeCompte) {
        this.codeTypeCompte = codeTypeCompte;
        return this;
    }

    public void setCodeTypeCompte(String codeTypeCompte) {
        this.codeTypeCompte = codeTypeCompte;
    }

    public String getLibelleTypeCompte() {
        return libelleTypeCompte;
    }

    public TypeCompte libelleTypeCompte(String libelleTypeCompte) {
        this.libelleTypeCompte = libelleTypeCompte;
        return this;
    }

    public void setLibelleTypeCompte(String libelleTypeCompte) {
        this.libelleTypeCompte = libelleTypeCompte;
    }

    public Set<Compte> getComptes() {
        return comptes;
    }

    public TypeCompte comptes(Set<Compte> comptes) {
        this.comptes = comptes;
        return this;
    }

    public TypeCompte addCompte(Compte compte) {
        this.comptes.add(compte);
        compte.setTypeCompte(this);
        return this;
    }

    public TypeCompte removeCompte(Compte compte) {
        this.comptes.remove(compte);
        compte.setTypeCompte(null);
        return this;
    }

    public void setComptes(Set<Compte> comptes) {
        this.comptes = comptes;
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
        TypeCompte typeCompte = (TypeCompte) o;
        if (typeCompte.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), typeCompte.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "TypeCompte{" +
            "id=" + getId() +
            ", codeTypeCompte=" + getCodeTypeCompte() +
            ", libelleTypeCompte='" + getLibelleTypeCompte() + "'" +
            "}";
    }
}
