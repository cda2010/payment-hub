package com.m2i.pti.domain;

import com.m2i.pti.config.audit.EntityAuditEventListener;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.io.Serializable;
import java.util.Objects;

/**
 * A CompteBanque.
 */
@Entity
@Table(name = "compte_banque")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@EntityListeners(value = {EntityAuditEventListener.class})
@Cacheable(value = false)
public class CompteBanque extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "code_agence", nullable = false)
    private String codeAgence;

    @NotNull
    @Column(name = "code_banque", nullable = false)
    private String codeBanque;


    @Column(name = "rib", nullable = true)
    private String rib;

    @Column(name = "code_fse")
    private String codeFse;
  @Column(name = "alias")
    private String alias;

    @ManyToOne
    private Devise devise;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodeAgence() {
        return codeAgence;
    }

    public CompteBanque codeAgence(String codeAgence) {
        this.codeAgence = codeAgence;
        return this;
    }

    public String getRib() {
        return rib;
    }

    public CompteBanque rib(String rib) {
        this.rib = rib;
        return this;
    }

    public void setRib(String rib) {
        this.rib = rib;
    }

    public void setCodeAgence(String codeAgence) {
        this.codeAgence = codeAgence;
    }

    public String getCodeBanque() {
        return codeBanque;
    }

    public CompteBanque codeBanque(String codeBanque) {
        this.codeBanque = codeBanque;
        return this;
    }

    public void setCodeBanque(String codeBanque) {
        this.codeBanque = codeBanque;
    }

    public String getCodeFse() {
        return codeFse;
    }

    public CompteBanque codeFse(String codeFse) {
        this.codeFse = codeFse;
        return this;
    }

    public void setCodeFse(String codeFse) {
        this.codeFse = codeFse;
    }

    public Devise getDevise() {
        return devise;
    }

    public CompteBanque devise(Devise devise) {
        this.devise = devise;
        return this;
    }

    public void setDevise(Devise devise) {
        this.devise = devise;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CompteBanque compteBanque = (CompteBanque) o;
        if (compteBanque.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), compteBanque.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "CompteBanque{" +
                "id=" + getId() +
                ", codeAgence='" + getCodeAgence() + "'" +
                ", codeBanque='" + getCodeBanque() + "'" +
                ", codeFse='" + getCodeFse() + "'" +
                "}";
    }
}
