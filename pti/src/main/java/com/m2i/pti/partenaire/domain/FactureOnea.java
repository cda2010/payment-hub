package com.m2i.pti.partenaire.domain;

import com.m2i.pti.domain.Facture;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A FactureOnea.
 */
@Entity
@Table(name = "facture_onea")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class FactureOnea implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ref_abonne")
    private String refAbonne;

    @Column(name = "type_piece")
    private String typePiece;

    @Column(name = "numero_piece")
    private String numeroPiece;

    @Column(name = "solde_piece")
    private String soldePiece;

    @Column(name = "date_limite_piece")
    private String dateLimitePiece;

    @Column(name = "date_appli_penalite")
    private String dateAppliPenalite;

    @Column(name = "date_lettrage")
    private String dateLettrage;

    @Column(name = "categorie_com_abonne")
    private String categorieComAbonne;

    @Column(name = "nom_abonne")
    private String nomAbonne;

    @Column(name = "annee_facture")
    private String anneeFacture;

    @Column(name = "mois_facture")
    private String moisFacture;

    @ManyToOne
    private Facture facture;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRefAbonne() {
        return refAbonne;
    }

    public FactureOnea refAbonne(String refAbonne) {
        this.refAbonne = refAbonne;
        return this;
    }

    public void setRefAbonne(String refAbonne) {
        this.refAbonne = refAbonne;
    }

    public String getTypePiece() {
        return typePiece;
    }

    public FactureOnea typePiece(String typePiece) {
        this.typePiece = typePiece;
        return this;
    }

    public void setTypePiece(String typePiece) {
        this.typePiece = typePiece;
    }

    public String getNumeroPiece() {
        return numeroPiece;
    }

    public FactureOnea numeroPiece(String numeroPiece) {
        this.numeroPiece = numeroPiece;
        return this;
    }

    public void setNumeroPiece(String numeroPiece) {
        this.numeroPiece = numeroPiece;
    }

    public String getSoldePiece() {
        return soldePiece;
    }

    public FactureOnea soldePiece(String soldePiece) {
        this.soldePiece = soldePiece;
        return this;
    }

    public void setSoldePiece(String soldePiece) {
        this.soldePiece = soldePiece;
    }

    public String getDateLimitePiece() {
        return dateLimitePiece;
    }

    public FactureOnea dateLimitePiece(String dateLimitePiece) {
        this.dateLimitePiece = dateLimitePiece;
        return this;
    }

    public void setDateLimitePiece(String dateLimitePiece) {
        this.dateLimitePiece = dateLimitePiece;
    }

    public String getDateAppliPenalite() {
        return dateAppliPenalite;
    }

    public FactureOnea dateAppliPenalite(String dateAppliPenalite) {
        this.dateAppliPenalite = dateAppliPenalite;
        return this;
    }

    public void setDateAppliPenalite(String dateAppliPenalite) {
        this.dateAppliPenalite = dateAppliPenalite;
    }

    public String getDateLettrage() {
        return dateLettrage;
    }

    public FactureOnea dateLettrage(String dateLettrage) {
        this.dateLettrage = dateLettrage;
        return this;
    }

    public void setDateLettrage(String dateLettrage) {
        this.dateLettrage = dateLettrage;
    }

    public String getCategorieComAbonne() {
        return categorieComAbonne;
    }

    public FactureOnea categorieComAbonne(String categorieComAbonne) {
        this.categorieComAbonne = categorieComAbonne;
        return this;
    }

    public void setCategorieComAbonne(String categorieComAbonne) {
        this.categorieComAbonne = categorieComAbonne;
    }

    public String getNomAbonne() {
        return nomAbonne;
    }

    public FactureOnea nomAbonne(String nomAbonne) {
        this.nomAbonne = nomAbonne;
        return this;
    }

    public void setNomAbonne(String nomAbonne) {
        this.nomAbonne = nomAbonne;
    }

    public String getAnneeFacture() {
        return anneeFacture;
    }

    public FactureOnea anneeFacture(String anneeFacture) {
        this.anneeFacture = anneeFacture;
        return this;
    }

    public void setAnneeFacture(String anneeFacture) {
        this.anneeFacture = anneeFacture;
    }

    public String getMoisFacture() {
        return moisFacture;
    }

    public FactureOnea moisFacture(String moisFacture) {
        this.moisFacture = moisFacture;
        return this;
    }

    public void setMoisFacture(String moisFacture) {
        this.moisFacture = moisFacture;
    }

    public Facture getFacture() {
        return facture;
    }

    public FactureOnea facture(Facture facture) {
        this.facture = facture;
        return this;
    }

    public void setFacture(Facture facture) {
        this.facture = facture;
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
        FactureOnea factureOnea = (FactureOnea) o;
        if (factureOnea.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), factureOnea.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "FactureOnea{" +
            "id=" + getId() +
            ", refAbonne='" + getRefAbonne() + "'" +
            ", typePiece='" + getTypePiece() + "'" +
            ", numeroPiece='" + getNumeroPiece() + "'" +
            ", soldePiece='" + getSoldePiece() + "'" +
            ", dateLimitePiece='" + getDateLimitePiece() + "'" +
            ", dateAppliPenalite='" + getDateAppliPenalite() + "'" +
            ", dateLettrage='" + getDateLettrage() + "'" +
            ", categorieComAbonne='" + getCategorieComAbonne() + "'" +
            ", nomAbonne='" + getNomAbonne() + "'" +
            ", anneeFacture='" + getAnneeFacture() + "'" +
            ", moisFacture='" + getMoisFacture() + "'" +
            "}";
    }
}
