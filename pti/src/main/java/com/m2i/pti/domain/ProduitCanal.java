package com.m2i.pti.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A ProduitCanal.
 */
@Entity
@Table(name = "produit_canal")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class ProduitCanal implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Canal canal;

    @ManyToOne
    private Produit produit;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Canal getCanal() {
        return canal;
    }

    public ProduitCanal canal(Canal canal) {
        this.canal = canal;
        return this;
    }

    public void setCanal(Canal canal) {
        this.canal = canal;
    }

    public Produit getProduit() {
        return produit;
    }

    public ProduitCanal produit(Produit produit) {
        this.produit = produit;
        return this;
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
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
        ProduitCanal produitCanal = (ProduitCanal) o;
        if (produitCanal.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), produitCanal.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "ProduitCanal{" +
            "id=" + getId() +
            "}";
    }
}
