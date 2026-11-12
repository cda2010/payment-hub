package com.m2i.pti.domain;

import com.m2i.pti.config.audit.EntityAuditEventListener;
import com.m2i.pti.domain.enumeration.DestinationProfilActeur;
import com.m2i.pti.domain.enumeration.TypeProfilActeur;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A GroupeProfilActeur.
 */
@Entity
@EntityListeners(value = {EntityAuditEventListener.class})
@Table(name = "groupe_profil_acteur")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class GroupeProfilActeur extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom")
    private String nom;

    @Column(name = "description")
    private String description;

    @Column(name = "jhi_type")
    private TypeProfilActeur type;


    @Column(name = "code_fse")
    private String codeFSE;

    @Column(name = "active")
    private Boolean active;

    @OneToMany(mappedBy = "groupe")
    Set<ProfilActeur> profilActeurs =new HashSet<>();

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

    public GroupeProfilActeur nom(String nom) {
        this.nom = nom;
        return this;
    }

    public Set<ProfilActeur> getProfilActeurs() {
        return profilActeurs;
    }

    public void setProfilActeurs(Set<ProfilActeur> profilActeurs) {
        this.profilActeurs = profilActeurs;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public GroupeProfilActeur description(String description) {
        this.description = description;
        return this;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TypeProfilActeur getType() {
        return type;
    }

    public void setType(TypeProfilActeur type) {
        this.type = type;
    }

    public String getCodeFSE() {
        return codeFSE;
    }

    public GroupeProfilActeur codeFSE(String codeFSE) {
        this.codeFSE = codeFSE;
        return this;
    }

    public void setCodeFSE(String codeFSE) {
        this.codeFSE = codeFSE;
    }

    public Boolean isActive() {
        return active;
    }

    public GroupeProfilActeur active(Boolean active) {
        this.active = active;
        return this;
    }

    public void setActive(Boolean active) {
        this.active = active;
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
        GroupeProfilActeur groupeProfilActeur = (GroupeProfilActeur) o;
        if (groupeProfilActeur.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), groupeProfilActeur.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "GroupeProfilActeur{" +
            "id=" + getId() +
            ", nom='" + getNom() + "'" +
            ", description='" + getDescription() + "'" +
            ", type='" + getType() + "'" +
            ", codeFSE='" + getCodeFSE() + "'" +
            ", active='" + isActive() + "'" +
            "}";
    }
}
