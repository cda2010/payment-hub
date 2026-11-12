package com.m2i.omnicanal.presentation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import java.io.Serializable;
import java.time.ZonedDateTime;

@MappedSuperclass
public abstract class Alive implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private MethodeFonction methode;

    @Column(name = "date_request")
    private ZonedDateTime dateRequest;

    @Column(name = "statut")
    private Long statut;

    @Column(name = "code_reponse")
    private String codeReponse;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MethodeFonction getMethode() {
        return methode;
    }

    public void setMethode(MethodeFonction methode) {
        this.methode = methode;
    }

    public ZonedDateTime getDateRequest() {
        return dateRequest;
    }

    public void setDateRequest(ZonedDateTime dateRequest) {
        this.dateRequest = dateRequest;
    }

    public Long getStatut() {
        return statut;
    }

    public void setStatut(Long statut) {
        this.statut = statut;
    }

    public String getCodeReponse() {
        return codeReponse;
    }

    public void setCodeReponse(String codeReponse) {
        this.codeReponse = codeReponse;
    }
}
