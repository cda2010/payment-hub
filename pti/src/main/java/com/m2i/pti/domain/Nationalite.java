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
 * A Nationalite.
 */
@Entity
@Table(name = "nationalite")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Nationalite extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code")
    private String code;

    @Column(name = "libelle")
    private String libelle;

    @OneToMany(mappedBy = "nationalite")
    @JsonIgnore
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<PersonnePhysique> personnePhysiques = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public Nationalite code(String code) {
        this.code = code;
        return this;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLibelle() {
        return libelle;
    }

    public Nationalite libelle(String libelle) {
        this.libelle = libelle;
        return this;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public Set<PersonnePhysique> getPersonnePhysiques() {
        return personnePhysiques;
    }

    public Nationalite personnePhysiques(Set<PersonnePhysique> personnePhysiques) {
        this.personnePhysiques = personnePhysiques;
        return this;
    }

    public Nationalite addPersonnePhysique(PersonnePhysique personnePhysique) {
        this.personnePhysiques.add(personnePhysique);
        personnePhysique.setNationalite(this);
        return this;
    }

    public Nationalite removePersonnePhysique(PersonnePhysique personnePhysique) {
        this.personnePhysiques.remove(personnePhysique);
        personnePhysique.setNationalite(null);
        return this;
    }

    public void setPersonnePhysiques(Set<PersonnePhysique> personnePhysiques) {
        this.personnePhysiques = personnePhysiques;
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
        Nationalite nationalite = (Nationalite) o;
        if (nationalite.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), nationalite.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "Nationalite{" +
            "id=" + getId() +
            ", code='" + getCode() + "'" +
            ", libelle='" + getLibelle() + "'" +
            "}";
    }
}
