package com.m2i.pti.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DetailListeDiffusion.
 */
@Entity
@Table(name = "detail_liste_diffusion")
//@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class DetailListeDiffusion extends AbstractAuditingEntity  implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero")
    private String numero;

    @ManyToOne
    private ListeDiffusion liste;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumero() {
        return numero;
    }

    public DetailListeDiffusion numero(String numero) {
        this.numero = numero;
        return this;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public ListeDiffusion getListe() {
        return liste;
    }

    public DetailListeDiffusion liste(ListeDiffusion listeDiffusion) {
        this.liste = listeDiffusion;
        return this;
    }

    public void setListe(ListeDiffusion listeDiffusion) {
        this.liste = listeDiffusion;
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
        DetailListeDiffusion detailListeDiffusion = (DetailListeDiffusion) o;
        if (detailListeDiffusion.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), detailListeDiffusion.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "DetailListeDiffusion{" +
            "id=" + getId() +
            ", numero='" + getNumero() + "'" +
            "}";
    }
}
