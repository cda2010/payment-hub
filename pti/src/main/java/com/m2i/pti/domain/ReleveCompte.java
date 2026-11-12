package com.m2i.pti.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

/**
 * A ReleveCompte.
 */
@Entity
@Table(name = "releve_compte")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class ReleveCompte extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Compte  compte;

    @Column(name = "solde_initial", precision=20, scale=2)
    private BigDecimal soldeInitial;

    @Column(name = "date_debut")
    private ZonedDateTime dateDebut;

    @Column(name = "solde_final", precision=20, scale=2)
    private BigDecimal soldeFinal;

    @Column(name = "date_fin")
    private ZonedDateTime dateFin;

    @OneToMany(mappedBy = "releve")
    @JsonIgnore
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<DetailReleveCompte> detailReleveComptes = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getSoldeInitial() {
        return soldeInitial;
    }

    public ReleveCompte soldeInitial(BigDecimal soldeInitial) {
        this.soldeInitial = soldeInitial;
        return this;
    }

    public void setSoldeInitial(BigDecimal soldeInitial) {
        this.soldeInitial = soldeInitial;
    }

    public ZonedDateTime getDateDebut() {
        return dateDebut;
    }

    public ReleveCompte dateDebut(ZonedDateTime dateDebut) {
        this.dateDebut = dateDebut;
        return this;
    }

    public void setDateDebut(ZonedDateTime dateDebut) {
        this.dateDebut = dateDebut;
    }

    public BigDecimal getSoldeFinal() {
        return soldeFinal;
    }

    public ReleveCompte soldeFinal(BigDecimal soldeFinal) {
        this.soldeFinal = soldeFinal;
        return this;
    }

    public void setSoldeFinal(BigDecimal soldeFinal) {
        this.soldeFinal = soldeFinal;
    }

    public ZonedDateTime getDateFin() {
        return dateFin;
    }

    public ReleveCompte dateFin(ZonedDateTime dateFin) {
        this.dateFin = dateFin;
        return this;
    }

    public void setDateFin(ZonedDateTime dateFin) {
        this.dateFin = dateFin;
    }

    public Set<DetailReleveCompte> getDetailReleveComptes() {
        return detailReleveComptes;
    }

    public ReleveCompte detailReleveComptes(Set<DetailReleveCompte> detailReleveComptes) {
        this.detailReleveComptes = detailReleveComptes;
        return this;
    }

    public ReleveCompte addDetailReleveCompte(DetailReleveCompte detailReleveCompte) {
        this.detailReleveComptes.add(detailReleveCompte);
        detailReleveCompte.setReleve(this);
        return this;
    }

    public ReleveCompte removeDetailReleveCompte(DetailReleveCompte detailReleveCompte) {
        this.detailReleveComptes.remove(detailReleveCompte);
        detailReleveCompte.setReleve(null);
        return this;
    }

    public void setDetailReleveComptes(Set<DetailReleveCompte> detailReleveComptes) {
        this.detailReleveComptes = detailReleveComptes;
    }

    public Compte getCompte() {
        return compte;
    }

    public void setCompte(Compte compte) {
        this.compte = compte;
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
        ReleveCompte releveCompte = (ReleveCompte) o;
        if (releveCompte.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), releveCompte.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "ReleveCompte{" +
            "id=" + getId() +
            ", soldeInitial='" + getSoldeInitial() + "'" +
            ", dateDebut='" + getDateDebut() + "'" +
            ", soldeFinal='" + getSoldeFinal() + "'" +
            ", dateFin='" + getDateFin() + "'" +
            "}";
    }
}
