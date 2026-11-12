package com.m2i.omnicanal.application.domain;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import java.io.Serializable;
import java.time.ZonedDateTime;

@MappedSuperclass
public abstract class HistoriqueAppel implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code_fse")
    private String codeFse;

    @Column(name = "code_reponse")
    private String codeReponse;

    @Column(name = "msg")
    private String msg;

    @Column(name = "nom_api")
    private String nomApi;

    @Column(name = "duree")
    private Long duree;

    @Column(name = "nom_fonction")
    private String nomFonction;

    @Column(name = "date_request")
    private ZonedDateTime dateRequest;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodeFse() {
        return codeFse;
    }

    public void setCodeFse(String codeFse) {
        this.codeFse = codeFse;
    }

    public String getCodeReponse() {
        return codeReponse;
    }

    public void setCodeReponse(String codeReponse) {
        this.codeReponse = codeReponse;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getNomApi() {
        return nomApi;
    }

    public void setNomApi(String nomApi) {
        this.nomApi = nomApi;
    }

    public Long getDuree() {
        return duree;
    }

    public void setDuree(Long duree) {
        this.duree = duree;
    }

    public String getNomFonction() {
        return nomFonction;
    }

    public void setNomFonction(String nomFonction) {
        this.nomFonction = nomFonction;
    }

    public ZonedDateTime getDateRequest() {
        return dateRequest;
    }

    public void setDateRequest(ZonedDateTime dateRequest) {
        this.dateRequest = dateRequest;
    }
}
