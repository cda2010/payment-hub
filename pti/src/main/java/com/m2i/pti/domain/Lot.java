package com.m2i.pti.domain;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import jakarta.persistence.*;

import com.m2i.pti.config.audit.EntityAuditEventListener;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "lot")
@Cacheable(value = true)
@EntityListeners(value = {EntityAuditEventListener.class})
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Lot extends AbstractAuditingEntity implements Serializable{
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "etat")
    private Boolean etat;

    @Column(name = "date_ouverture")
    private Instant dateOuverture;

    @Column(name = "date_fermeture")
    private Instant dateFermeture;

    @Column(name = "est_imprime")
    private Boolean estImprime;

    @Column(name = "date_impression")
    private Instant dateImpression;

    @Column(name = "numero")
    private String numero;
    
    @Column(name = "type_acteur")
    private String typeActeur;

    @ManyToOne
    private UniteOrganisation organisation;

   

    public Lot() {
    }
    

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getEtat() {
        return etat;
    }

    public void setEtat(Boolean etat) {
        this.etat = etat;
    }

    public Instant getDateOuverture() {
        return dateOuverture;
    }

    public void setDateOuverture(Instant dateOuverture) {
        this.dateOuverture = dateOuverture;
    }

    public Instant getDateFermeture() {
        return dateFermeture;
    }

    public void setDateFermeture(Instant dateFermeture) {
        this.dateFermeture = dateFermeture;
    }

    public Boolean getEstImprime() {
        return estImprime;
    }

    public void setEstImprime(Boolean estImprime) {
        this.estImprime = estImprime;
    }

    public Instant getDateImpression() {
        return dateImpression;
    }

    public void setDateImpression(Instant dateImpression) {
        this.dateImpression = dateImpression;
    }

    public String getNumero() {
        return numero;
    }


    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getTypeActeur() {
        return typeActeur;
    }

    public void setTypeActeur(String typeActeur) {
        this.typeActeur = typeActeur;
    }
    
    


    // public Set<DetailsLot> getDetails() {
    //     return details;
    // }


    // public void setDetails(Set<DetailsLot> details) {
    //     this.details = details;
    // }


    public UniteOrganisation getOrganisation() {
        return organisation;
    }

    public void setOrganisation(UniteOrganisation organisation) {
        this.organisation = organisation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Lot lot = (Lot) o;
        if (lot.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), lot.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }



    public String toString() {
        return "Lot [dateFermeture=" + dateFermeture + ", dateImpression=" + dateImpression + ", dateOuverture="
                + dateOuverture  + ", estImprime=" + estImprime + ", etat=" + etat + ", id="
                + id + ", numero=" + numero + ", organisation=" + organisation + "]";
    }


    // @Override
    // public String toString() {
    //     return "Lot{" +
    //             "id=" + getId() +
    //             ", etat='" + getEtat() + "'" +
    //             ", dateOuverture='" + getDateOuverture() + "'" +
    //             ", dateFermeture='" + getDateFermeture() + "'" +
    //             ", estImprime='" + getEstImprime() + "'" +
    //             ", dateImpression='" + getDateImpression() + "'" +
    //             ", details='" + getDateImpression() + "'" +
    //             "}";
    // }

    
    
}
