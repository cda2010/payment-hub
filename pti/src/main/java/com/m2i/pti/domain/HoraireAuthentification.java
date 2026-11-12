package com.m2i.pti.domain;

import com.m2i.pti.domain.enumeration.Jour;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A HoraireAuthentification.
 */
@Entity
@Table(name = "horaire_authentification")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class HoraireAuthentification extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "jour", nullable = false)
    private Jour jour;

    @NotNull
    @Column(name = "heure_debut", nullable = false)
    private String heureDebut;

    @NotNull
    @Column(name = "heure_fin", nullable = false)
    private String heureFin;

    @Column(name = "date_debut")
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    @Column
    private boolean activated = false;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Jour getJour() {
        return jour;
    }

    public HoraireAuthentification jour(Jour jour) {
        this.jour = jour;
        return this;
    }

    public void setJour(Jour jour) {
        this.jour = jour;
    }

    public String getHeureDebut() {
        return heureDebut;
    }

    public HoraireAuthentification heureDebut(String heureDebut) {
        this.heureDebut = heureDebut;
        return this;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public void setHeureDebut(String heureDebut) {
        this.heureDebut = heureDebut;
    }

    public String getHeureFin() {
        return heureFin;
    }

    public HoraireAuthentification heureFin(String heureFin) {
        this.heureFin = heureFin;
        return this;
    }

    public void setHeureFin(String heureFin) {
        this.heureFin = heureFin;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public HoraireAuthentification dateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
        return this;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public HoraireAuthentification dateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
        return this;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
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
        HoraireAuthentification horaireAuthentification = (HoraireAuthentification) o;
        if (horaireAuthentification.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), horaireAuthentification.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "HoraireAuthentification{" +
            "id=" + getId() +
            ", jour='" + getJour() + "'" +
            ", heureDebut='" + getHeureDebut() + "'" +
            ", heureFin='" + getHeureFin() + "'" +
            ", dateDebut='" + getDateDebut() + "'" +
            ", dateFin='" + getDateFin() + "'" +
            "}";
    }
}
