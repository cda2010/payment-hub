package com.m2i.pti.domain;

import com.m2i.pti.domain.enumeration.StatutLigneChargement;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A LigneMessage.
 */
@Entity
@Table(name = "ligne_message")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class LigneMessage extends LigneChargement implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code_indicatif")
    private String codeIndicatif;

    @Column(name = "numero_telephone")
    private String numeroTelephone;

    @Column(name = "email")
    private String email;

    @Column(name = "message")
    private String message;

    @Column(name = "error")
    private String error;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_ligne")
    private StatutLigneChargement statutLigne;

    @ManyToOne
    private ChargementMessage chargementMessage;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodeIndicatif() {
        return codeIndicatif;
    }

    public LigneMessage codeIndicatif(String codeIndicatif) {
        this.codeIndicatif = codeIndicatif;
        return this;
    }

    public void setCodeIndicatif(String codeIndicatif) {
        this.codeIndicatif = codeIndicatif;
    }

    public String getNumeroTelephone() {
        return numeroTelephone;
    }

    public LigneMessage numeroTelephone(String numeroTelephone) {
        this.numeroTelephone = numeroTelephone;
        return this;
    }

    public void setNumeroTelephone(String numeroTelephone) {
        this.numeroTelephone = numeroTelephone;
    }

    public String getEmail() {
        return email;
    }

    public LigneMessage email(String email) {
        this.email = email;
        return this;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMessage() {
        return message;
    }

    public LigneMessage message(String message) {
        this.message = message;
        return this;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getError() {
        return error;
    }

    public LigneMessage error(String error) {
        this.error = error;
        return this;
    }

    public void setError(String error) {
        this.error = error;
    }

    public ChargementMessage getChargementMessage() {
        return chargementMessage;
    }

    public LigneMessage chargementMessage(ChargementMessage chargementMessage) {
        this.chargementMessage = chargementMessage;
        return this;
    }

    public void setChargementMessage(ChargementMessage chargementMessage) {
        this.chargementMessage = chargementMessage;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove


    public StatutLigneChargement getStatutLigne() {
        return statutLigne;
    }

    public void setStatutLigne(StatutLigneChargement statutLigne) {
        this.statutLigne = statutLigne;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LigneMessage ligneMessage = (LigneMessage) o;
        if (ligneMessage.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), ligneMessage.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "LigneMessage{" +
            "id=" + getId() +
            ", codeIndicatif='" + getCodeIndicatif() + "'" +
            ", numeroTelephone='" + getNumeroTelephone() + "'" +
            ", email='" + getEmail() + "'" +
            ", message='" + getMessage() + "'" +
            ", error='" + getError() + "'" +
            "}";
    }
}
