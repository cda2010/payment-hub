package com.m2i.pti.service.dto;


import com.m2i.pti.domain.enumeration.EtatValidite;
import com.m2i.pti.domain.enumeration.TypeTransactionCompte;

import java.time.ZonedDateTime;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

/**
 * A DTO for the DetailReleveCompte entity.
 */
public class DetailReleveCompteDTO implements Serializable {

    private Long id;

    private ZonedDateTime date;

    private EtatValidite etat;

    private BigDecimal montant;

    private BigDecimal soldeCourant;

    private BigDecimal soldeApres;

    private TypeTransactionCompte type;

    private Long releveId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ZonedDateTime getDate() {
        return date;
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

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }

    public BigDecimal getSoldeCourant() {
        return soldeCourant;
    }

    public void setSoldeCourant(BigDecimal soldeCourant) {
        this.soldeCourant = soldeCourant;
    }

    public BigDecimal getSoldeApres() {
        return soldeApres;
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

    public Long getReleveId() {
        return releveId;
    }

    public void setReleveId(Long releveCompteId) {
        this.releveId = releveCompteId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DetailReleveCompteDTO detailReleveCompteDTO = (DetailReleveCompteDTO) o;
        if(detailReleveCompteDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), detailReleveCompteDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "DetailReleveCompteDTO{" +
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
