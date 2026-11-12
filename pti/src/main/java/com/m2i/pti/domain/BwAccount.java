package com.m2i.pti.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.io.Serializable;

/**
 * A BwAccount.
 */
@Entity
@Table(name = "bw_account")
public class BwAccount implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "code_banque", nullable = false)
    private String codeBanque;
    @NotNull
    @Column(name = "code_fse", nullable = false)
    private String codeFse;
    @NotNull
    @Column(name = "code_agence", nullable = false)
    private String codeAgence;

    @NotNull
    @Column(name = "numero_compte", nullable = false)
    private String numeroCompte;

    @ManyToOne
    @JsonIgnoreProperties("bwAccounts")
    private BwCustomer bwCustomer;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodeBanque() {
        return codeBanque;
    }

    public BwAccount codeBanque(String codeBanque) {
        this.codeBanque = codeBanque;
        return this;
    }

    public void setCodeBanque(String codeBanque) {
        this.codeBanque = codeBanque;
    }

    public String getCodeAgence() {
        return codeAgence;
    }

    public BwAccount codeAgence(String codeAgence) {
        this.codeAgence = codeAgence;
        return this;
    }

    public void setCodeAgence(String codeAgence) {
        this.codeAgence = codeAgence;
    }

    public String getNumeroCompte() {
        return numeroCompte;
    }

    public BwAccount numeroCompte(String numeroCompte) {
        this.numeroCompte = numeroCompte;
        return this;
    }

    public void setNumeroCompte(String numeroCompte) {
        this.numeroCompte = numeroCompte;
    }

    public BwCustomer getBwCustomer() {
        return bwCustomer;
    }

    public BwAccount bwCustomer(BwCustomer bwCustomer) {
        this.bwCustomer = bwCustomer;
        return this;
    }

    public void setBwCustomer(BwCustomer bwCustomer) {
        this.bwCustomer = bwCustomer;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    public String getCodeFse() {
        return codeFse;
    }

    public void setCodeFse(String codeFse) {
        this.codeFse = codeFse;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BwAccount)) {
            return false;
        }
        return id != null && id.equals(((BwAccount) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "BwAccount{"
                + "id=" + getId()
                + ", codeBanque='" + getCodeBanque() + "'"
                + ", codeAgence='" + getCodeAgence() + "'"
                + ", numeroCompte='" + getNumeroCompte() + "'"
                + "}";
    }
}
