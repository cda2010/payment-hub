package com.m2i.pti.domain;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
public class ExternalOTP extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "code_pays")
    private String codePays;
    @Column(name = "telephone")
    private String telephone;
    @Column(name = "otp")
    private String otp;
    @Column(name = "secret")
    private String secret;
    @Column(name = "valide")
    private Boolean valide = true;
    @Column(name = "identifiant")
    private String identifiant;
    @Column(name = "codeFacturier")
    private String codeFacturier;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodePays() {
        return codePays;
    }

    public void setCodePays(String codePays) {
        this.codePays = codePays;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public Boolean getValide() {
        return valide;
    }

    public void setValide(Boolean valide) {
        this.valide = valide;
    }

    public String getIdentifiant() {
        return identifiant;
    }

    public void setIdentifiant(String identifiant) {
        this.identifiant = identifiant;
    }

    public String getCodeFacturier() {
        return codeFacturier;
    }

    public void setCodeFacturier(String codeFacturier) {
        this.codeFacturier = codeFacturier;
    }

    @Override
    public String toString() {
        return "ExternatOTP{"
                + "id=" + id
                + ", codePays='" + codePays + '\''
                + ", telephone='" + telephone + '\''
                + ", otp='" + otp + '\''
                + ", secret='" + secret + '\''
                + '}';
    }
}
