package com.m2i.pti.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.m2i.pti.domain.util.JpaJsonCompteConverter;
import com.m2i.pti.domain.util.JpaJsonDocumentsConverter;
import com.m2i.pti.domain.util.JsonLigneHistoriqueCompte;
import com.m2i.pti.domain.util.JsonLigneReglementTemplate;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * A DetailHistorique.
 */
@Entity
@Table(name = "detail_historique")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class DetailHistorique implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_cpt")
    private String numeroCpt;

    @Column(name = "libelle")
    private String libelle;

    @Column(name = "code_uo")
    private String codeUo;

    @Column(name = "type_uo")
    private String typeUo;

    @Column(name = "telephone")
    private String telephone;

    @Column(name = "description")
    private String description;

    @Column(name = "solde", precision = 20, scale = 2)
    private BigDecimal solde;

    @ManyToOne
    private Historique historique;
    @JsonProperty("textLigneReglementTemplate")
    @Convert(converter = JpaJsonCompteConverter.class)
    private JsonLigneHistoriqueCompte txtLigneHistoriqueCompte;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumeroCpt() {
        return numeroCpt;
    }

    public DetailHistorique numeroCpt(String numeroCpt) {
        this.numeroCpt = numeroCpt;
        return this;
    }

    public void setNumeroCpt(String numeroCpt) {
        this.numeroCpt = numeroCpt;
    }

    public String getLibelle() {
        return libelle;
    }

    public DetailHistorique libelle(String libelle) {
        this.libelle = libelle;
        return this;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public BigDecimal getSolde() {
        return solde;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getCodeUo() {
        return codeUo;
    }

    public void setCodeUo(String codeUo) {
        this.codeUo = codeUo;
    }

    public String getTypeUo() {
        return typeUo;
    }

    public void setTypeUo(String typeUo) {
        this.typeUo = typeUo;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DetailHistorique solde(BigDecimal solde) {
        this.solde = solde;
        return this;
    }

    public void setSolde(BigDecimal solde) {
        this.solde = solde;
    }

    public Historique getHistorique() {
        return historique;
    }

    public DetailHistorique historique(Historique historique) {
        this.historique = historique;
        return this;
    }

    public void setHistorique(Historique historique) {
        this.historique = historique;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    public JsonLigneHistoriqueCompte getTxtLigneHistoriqueCompte() {
        return txtLigneHistoriqueCompte;
    }

    public void setTxtLigneHistoriqueCompte(JsonLigneHistoriqueCompte txtLigneHistoriqueCompte) {
        this.txtLigneHistoriqueCompte = txtLigneHistoriqueCompte;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DetailHistorique detailHistorique = (DetailHistorique) o;
        if (detailHistorique.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), detailHistorique.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "DetailHistorique{"
                + "id=" + getId()
                + ", numeroCpt='" + getNumeroCpt() + "'"
                + ", libelle='" + getLibelle() + "'"
                + ", solde='" + getSolde() + "'"
                + ", telephone='" + getTelephone() + "'"
                + "}";
    }
}
