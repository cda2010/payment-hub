package com.m2i.pti.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.io.Serializable;

/**
 * A BwCustomer.
 */
@Entity
@Table(name = "bw_customer")
public class BwCustomer implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    @Column(name = "nom_customer", nullable = false)
    private String nomCustomer;
    @NotNull
    @Column(name = "prenom_customer", nullable = false)
    private String prenomCustomer;
    @Column(name = "type_piece")
    private String typePiece;
    @NotNull
    @Column(name = "numero_piece", nullable = false)
    private String numeroPiece;

    @Column(name = "code_pays")
    private String codePays;

    @Column(name = "msisdn")
    private String msisdn;
    @Column(name = "valide")
    private boolean valide;
    @ManyToOne
    private UniteOrganisation uniteOrganisation;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNomCustomer() {
        return nomCustomer;
    }

    public BwCustomer nomCustomer(String nomCustomer) {
        this.nomCustomer = nomCustomer;
        return this;
    }

    public void setNomCustomer(String nomCustomer) {
        this.nomCustomer = nomCustomer;
    }

    public String getPrenomCustomer() {
        return prenomCustomer;
    }

    public BwCustomer prenomCustomer(String prenomCustomer) {
        this.prenomCustomer = prenomCustomer;
        return this;
    }

    public void setPrenomCustomer(String prenomCustomer) {
        this.prenomCustomer = prenomCustomer;
    }

    public String getTypePiece() {
        return typePiece;
    }

    public BwCustomer typePiece(String typePiece) {
        this.typePiece = typePiece;
        return this;
    }

    public void setTypePiece(String typePiece) {
        this.typePiece = typePiece;
    }

    public String getNumeroPiece() {
        return numeroPiece;
    }

    public BwCustomer numeroPiece(String numeroPiece) {
        this.numeroPiece = numeroPiece;
        return this;
    }

    public void setNumeroPiece(String numeroPiece) {
        this.numeroPiece = numeroPiece;
    }

    public String getCodePays() {
        return codePays;
    }

    public BwCustomer codePays(String codePays) {
        this.codePays = codePays;
        return this;
    }

    public void setCodePays(String codePays) {
        this.codePays = codePays;
    }

    public String getMsisdn() {
        return msisdn;
    }

    public BwCustomer msisdn(String msisdn) {
        this.msisdn = msisdn;
        return this;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public boolean isValide() {
        return valide;
    }

    public void setValide(boolean valide) {
        this.valide = valide;
    }

    public UniteOrganisation getUniteOrganisation() {
        return uniteOrganisation;
    }

    public void setUniteOrganisation(UniteOrganisation uniteOrganisation) {
        this.uniteOrganisation = uniteOrganisation;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BwCustomer)) {
            return false;
        }
        return id != null && id.equals(((BwCustomer) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "BwCustomer{"
                + "id=" + getId()
                + ", nomCustomer='" + getNomCustomer() + "'"
                + ", prenomCustomer='" + getPrenomCustomer() + "'"
                + ", typePiece='" + getTypePiece() + "'"
                + ", numeroPiece='" + getNumeroPiece() + "'"
                + ", codePays='" + getCodePays() + "'"
                + ", msisdn='" + getMsisdn() + "'"
                + "}";
    }
}
