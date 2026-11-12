package com.m2i.pti.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;

import com.m2i.pti.domain.enumeration.HistoryType;
import com.m2i.pti.domain.util.JpaJsonCompteConverter;
import com.m2i.pti.domain.util.JsonLigneHistoriqueCompte;

/**
 * A Historique.
 */
@Entity
@Table(name = "historique")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Historique implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fournisseur_service")
    private String fournisseurService;

    @Column(name = "date_historique")
    private ZonedDateTime dateHistorique;

    @Enumerated(EnumType.STRING)
    @Column(name = "etiquette")
    private HistoryType etiquette;

    

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFournisseurService() {
        return fournisseurService;
    }

    public Historique fournisseurService(String fournisseurService) {
        this.fournisseurService = fournisseurService;
        return this;
    }

    public void setFournisseurService(String fournisseurService) {
        this.fournisseurService = fournisseurService;
    }

    public ZonedDateTime getDateHistorique() {
        return dateHistorique;
    }

    public Historique dateHistorique(ZonedDateTime dateHistorique) {
        this.dateHistorique = dateHistorique;
        return this;
    }

    public void setDateHistorique(ZonedDateTime dateHistorique) {
        this.dateHistorique = dateHistorique;
    }

    public HistoryType getEtiquette() {
        return etiquette;
    }

    public Historique etiquette(HistoryType etiquette) {
        this.etiquette = etiquette;
        return this;
    }

    public void setEtiquette(HistoryType etiquette) {
        this.etiquette = etiquette;
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
        Historique historique = (Historique) o;
        if (historique.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), historique.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "Historique{"
                + "id=" + getId()
                + ", fournisseurService='" + getFournisseurService() + "'"
                + ", dateHistorique='" + getDateHistorique() + "'"
                + ", etiquette='" + getEtiquette() + "'"
                + "}";
    }
}
