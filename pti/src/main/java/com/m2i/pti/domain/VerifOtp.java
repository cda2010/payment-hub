package com.m2i.pti.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Objects;

/**
 * A VerifOtp.
 */
@Entity
@Table(name = "verif_otp")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class VerifOtp implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "otp")
    private String otp;

    @Column(name = "login")
    private String login;

    @Column(name = "host_id")
    private String hostId;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOtp() {
        return otp;
    }

    public VerifOtp otp(String otp) {
        this.otp = otp;
        return this;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public String getLogin() {
        return login;
    }

    public VerifOtp login(String login) {
        this.login = login;
        return this;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getHostId() {
        return hostId;
    }

    public VerifOtp hostId(String hostId) {
        this.hostId = hostId;
        return this;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
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
        VerifOtp verifOtp = (VerifOtp) o;
        if (verifOtp.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), verifOtp.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "VerifOtp{" +
            "id=" + getId() +
            ", otp='" + getOtp() + "'" +
            ", login='" + getLogin() + "'" +
            ", hostId='" + getHostId() + "'" +
            "}";
    }
}
