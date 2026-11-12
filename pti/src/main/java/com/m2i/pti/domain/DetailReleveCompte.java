package com.m2i.pti.domain;

import com.m2i.pti.domain.enumeration.EtatValidite;
import com.m2i.pti.domain.enumeration.TypeTransactionCompte;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * A DetailReleveCompte.
 */
@Entity
@Table(name = "detail_releve_compte")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class DetailReleveCompte extends AbstractAuditingEntity  implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "jhi_date")
    private ZonedDateTime date;

    @Column(name = "etat")
    private EtatValidite etat;

    @Column(name = "montant", precision=10, scale=2)
    private BigDecimal montant;

    @Column(name = "solde_courant", precision=10, scale=2)
    private BigDecimal soldeCourant;

    @Column(name = "solde_apres", precision=10, scale=2)
    private BigDecimal soldeApres;

    @Column(name = "jhi_type")
    private TypeTransactionCompte type;

    @ManyToOne
    private ReleveCompte releve;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ZonedDateTime getDate() {
        return date;
    }

    public DetailReleveCompte date(ZonedDateTime date) {
        this.date = date;
        return this;
    }

    public void setDate(ZonedDateTime date) {
        this.date = date;
    }

    public EtatValidite getEtat() {
        return etat;
    }

    public void setEtat(EtatValidite etat) {
        this.etat = etat;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public DetailReleveCompte montant(BigDecimal montant) {
        this.montant = montant;
        return this;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }

    public BigDecimal getSoldeCourant() {
        return soldeCourant;
    }

    public DetailReleveCompte soldeCourant(BigDecimal soldeCourant) {
        this.soldeCourant = soldeCourant;
        return this;
    }

    public void setSoldeCourant(BigDecimal soldeCourant) {
        this.soldeCourant = soldeCourant;
    }

    public BigDecimal getSoldeApres() {
        return soldeApres;
    }

    public DetailReleveCompte soldeApres(BigDecimal soldeApres) {
        this.soldeApres = soldeApres;
        return this;
    }

    public void setSoldeApres(BigDecimal soldeApres) {
        this.soldeApres = soldeApres;
    }

    public TypeTransactionCompte getType() {
        return type;
    }

    public void setType(TypeTransactionCompte type) {
        this.type = type;
    }

    public ReleveCompte getReleve() {
        return releve;
    }

    public DetailReleveCompte releve(ReleveCompte releveCompte) {
        this.releve = releveCompte;
        return this;
    }

    public void setReleve(ReleveCompte releveCompte) {
        this.releve = releveCompte;
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
        DetailReleveCompte detailReleveCompte = (DetailReleveCompte) o;
        if (detailReleveCompte.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), detailReleveCompte.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "DetailReleveCompte{" +
            "id=" + getId() +
            ", date='" + getDate() + "'" +
            ", etat='" + getEtat() + "'" +
            ", montant='" + getMontant() + "'" +
            ", soldeCourant='" + getSoldeCourant() + "'" +
            ", soldeApres='" + getSoldeApres() + "'" +
            ", type='" + getType() + "'" +
            "}";
    }
}
