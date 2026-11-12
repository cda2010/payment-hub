package com.m2i.pti.domain;

import com.m2i.pti.domain.enumeration.StatutLigneChargement;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author cda
 */
@MappedSuperclass
public abstract class  LigneChargement extends AbstractAuditingEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "error")
    private String error;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_ligne")
    private StatutLigneChargement statutLigne;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getError() {
        return error;
    }

    public LigneChargement error(String error) {
        this.error = error;
        return this;
    }

    public void setError(String error) {
        this.error = error;
    }

    public StatutLigneChargement getStatutLigne() {
        return statutLigne;
    }

    public LigneChargement statutLigne(StatutLigneChargement statutLigne) {
        this.statutLigne = statutLigne;
        return this;
    }

    public void setStatutLigne(StatutLigneChargement statutLigne) {
        this.statutLigne = statutLigne;
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
        LigneChargement ligneChargement = (LigneChargement) o;
        if (ligneChargement.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), ligneChargement.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "LigneChargement{" +
                "id=" + getId() +
                ", error='" + getError() + "'" +
                ", statutLigne='" + getStatutLigne() + "'" +
                "}";
    }
    private String padLeftZeros;
    public void setRrn(String padLeftZeros) {
        this.padLeftZeros = padLeftZeros;
    }
}
