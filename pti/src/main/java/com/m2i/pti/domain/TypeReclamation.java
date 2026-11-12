package com.m2i.pti.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;

import java.io.Serializable;
/**
 * A TypeReclamation.
 */
@Entity
@Table(name = "type_reclamation")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class TypeReclamation extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "libelle_type_reclamation")
    private String libelleTypeReclamation;

   /* @OneToMany(mappedBy = "typeReclamation")
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<Reclamation> reclamations = new HashSet<>();*/

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLibelleTypeReclamation() {
        return libelleTypeReclamation;
    }

    public TypeReclamation libelleTypeReclamation(String libelleTypeReclamation) {
        this.libelleTypeReclamation = libelleTypeReclamation;
        return this;
    }

    public void setLibelleTypeReclamation(String libelleTypeReclamation) {
        this.libelleTypeReclamation = libelleTypeReclamation;
    }

   /* public Set<Reclamation> getReclamations() {
        return reclamations;
    }

    /*public TypeReclamation reclamations(Set<Reclamation> reclamations) {
        this.reclamations = reclamations;
        return this;
    }/

    public TypeReclamation addReclamation(Reclamation reclamation) {
        this.reclamations.add(reclamation);
        reclamation.setTypeReclamation(this);
        return this;
    }

    public TypeReclamation removeReclamation(Reclamation reclamation) {
        this.reclamations.remove(reclamation);
        reclamation.setTypeReclamation(null);
        return this;
    }

    public void setReclamations(Set<Reclamation> reclamations) {
        this.reclamations = reclamations;
    }*/
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TypeReclamation)) {
            return false;
        }
        return id != null && id.equals(((TypeReclamation) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "TypeReclamation{" +
            "id=" + getId() +
            ", libelleTypeReclamation='" + getLibelleTypeReclamation() + "'" +
            "}";
    }
}
