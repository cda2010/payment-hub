package com.m2i.pti.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.m2i.pti.config.audit.EntityAuditEventListener;

import jakarta.persistence.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Objects;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A CompteAutre.
 */
@Entity
@Table(name = "carte")
@EntityListeners(value = {EntityAuditEventListener.class})
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Cacheable(value = true)
public class Carte extends AbstractAuditingEntity implements Serializable {

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

    @Column(name = "last_digit")
    private String last4Digit;
    @Column(name = "date_expired")
    private String dateExpired;
    
       @Column(name = "nom_on_card")
    private String nomOnCard;
       
       
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

    public String getLast4Digit() {
        return last4Digit;
    }

    public void setLast4Digit(String last4Digit) {
        this.last4Digit = last4Digit;
    }

    public String getDateExpired() {
        return dateExpired;
    }

    public void setDateExpired(String dateExpired) {
        this.dateExpired = dateExpired;
    }

    public String getNomOnCard() {
        return nomOnCard;
    }

    public void setNomOnCard(String nomOnCard) {
        this.nomOnCard = nomOnCard;
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
        Carte carte = new Carte();
        try {
            carte = (Carte) super.clone();
        } catch (CloneNotSupportedException e) {
            carte.setCodeFse(codeFse);
            carte.setSolde(solde);

            carte.setVersion(version);
            carte.setId(id);
        }

        return carte;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "Carte{"
                + "id=" + getId()
                + ", solde=" + getSolde()
                + "}";
    }
}
