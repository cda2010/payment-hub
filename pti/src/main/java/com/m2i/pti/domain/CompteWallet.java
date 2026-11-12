package com.m2i.pti.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * A CompteAutre.
 */
@Entity
@Table(name = "compte_wallet")
public class CompteWallet extends AbstractAuditingEntity implements Serializable {

    @Transient
    @JsonIgnore
    private final Object lock = new Object();
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Version
    private Integer version;
    @Column(name = "solde")
    private volatile BigDecimal solde = BigDecimal.ZERO;

    @Column(name = "codeFse")
    private String codeFse;

    @Column(name = "date_derniere_transaction")
    private ZonedDateTime dateDerniereTransaction;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public synchronized BigDecimal getSolde() {
        synchronized (lock) {
            return solde;
        }

    }

    public String getCodeFse() {
        return codeFse;
    }

    public void setCodeFse(String codeFse) {
        this.codeFse = codeFse;
    }

    public void setSolde(BigDecimal solde) {
        synchronized (lock) {
            this.solde = solde;
        }

    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove
    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public ZonedDateTime getDateDerniereTransaction() {
        return dateDerniereTransaction;
    }

    public void setDateDerniereTransaction(ZonedDateTime dateDerniereTransaction) {
        this.dateDerniereTransaction = dateDerniereTransaction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CompteWallet compteWallet = (CompteWallet) o;
        if (compteWallet.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), compteWallet.getId());
    }

    @Override
    public Object clone() {
        CompteWallet compteWallet = new CompteWallet();
        try {
            compteWallet = (CompteWallet) super.clone();
        } catch (CloneNotSupportedException e) {
            compteWallet.setCodeFse(codeFse);
            compteWallet.setSolde(solde);

            compteWallet.setVersion(version);
            compteWallet.setId(id);
        }

        return compteWallet;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "CompteAutre{"
                + "id=" + getId()
                + ", solde=" + getSolde()
                + "}";
    }
}
