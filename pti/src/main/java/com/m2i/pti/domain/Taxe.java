package com.m2i.pti.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.m2i.pti.domain.enumeration.TypePersonne;
import com.m2i.pti.domain.enumeration.TypeTaxe;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A Taxe.
 */
@Entity
@Table(name = "taxe")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Taxe extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "code", nullable = false,unique = true)
    private String code;

    @NotNull
    @Column(name = "nom", nullable = false,unique = true)
    private String nom;

    @NotNull
    @Column(name = "etiquette", nullable = false)
    private String etiquette;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_taxe")
    private TypeTaxe type;

    @OneToMany(mappedBy = "taxe")
    @JsonIgnore
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<TaxeOperation> taxeOperations = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TypeTaxe getType() {
        return type;
    }

    public void setType(TypeTaxe type) {
        this.type = type;
    }

    public Set<TaxeOperation> getTaxeOperations() {
        return taxeOperations;
    }

    public void setTaxeOperations(Set<TaxeOperation> taxeOperations) {
        this.taxeOperations = taxeOperations;
    }

    public String getCode() {
        return code;
    }

    public Taxe code(String code) {
        this.code = code;
        return this;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getNom() {
        return nom;
    }

    public Taxe nom(String nom) {
        this.nom = nom;
        return this;
    }

    public String getEtiquette() {
        return etiquette;
    }

    public void setEtiquette(String etiquette) {
        this.etiquette = etiquette;
    }

    public void setNom(String nom) {
        this.nom = nom;
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
        Taxe taxe = (Taxe) o;
        if (taxe.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), taxe.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "Taxe{" +
            "id=" + getId() +
            ", code='" + getCode() + "'" +
            ", nom='" + getNom() + "'" +
            "}";
    }
}
