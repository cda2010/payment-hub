package com.m2i.pti.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

/**
 * A ListeDiffusion.
 */
@Entity
@Table(name = "liste_diffusion")
//@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class ListeDiffusion extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom_liste")
    private String nomListe;

    @OneToMany(mappedBy = "liste")
    @JsonIgnore
    
    private Set<DetailListeDiffusion> detailListeDiffusions = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNomListe() {
        return nomListe;
    }

    public ListeDiffusion nomListe(String nomListe) {
        this.nomListe = nomListe;
        return this;
    }

    public void setNomListe(String nomListe) {
        this.nomListe = nomListe;
    }

    public Set<DetailListeDiffusion> getDetailListeDiffusions() {
        return detailListeDiffusions;
    }

    public ListeDiffusion detailListeDiffusions(Set<DetailListeDiffusion> detailListeDiffusions) {
        this.detailListeDiffusions = detailListeDiffusions;
        return this;
    }

    public ListeDiffusion addDetailListeDiffusion(DetailListeDiffusion detailListeDiffusion) {
        this.detailListeDiffusions.add(detailListeDiffusion);
        detailListeDiffusion.setListe(this);
        return this;
    }

    public ListeDiffusion removeDetailListeDiffusion(DetailListeDiffusion detailListeDiffusion) {
        this.detailListeDiffusions.remove(detailListeDiffusion);
        detailListeDiffusion.setListe(null);
        return this;
    }

    public void setDetailListeDiffusions(Set<DetailListeDiffusion> detailListeDiffusions) {
        this.detailListeDiffusions = detailListeDiffusions;
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
        ListeDiffusion listeDiffusion = (ListeDiffusion) o;
        if (listeDiffusion.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), listeDiffusion.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "ListeDiffusion{" +
            "id=" + getId() +
            ", nomListe='" + getNomListe() + "'" +
            "}";
    }
}
