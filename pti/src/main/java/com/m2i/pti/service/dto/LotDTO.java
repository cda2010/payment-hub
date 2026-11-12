package com.m2i.pti.service.dto;

import java.io.Serializable;
import java.time.Instant;
import java.util.*;

import com.m2i.pti.domain.Client;
import com.m2i.pti.domain.DetailsLot;
import com.m2i.pti.domain.LigneChargementFichier;
import com.m2i.pti.domain.UniteOrganisation;

public class LotDTO implements Serializable{
    
    private Long id;

    private Boolean etat;

    private Instant dateOuverture;

    private Instant dateFermeture;

    private Boolean estImprime;

    private Instant dateImpression;

    private UniteOrganisation organisation;

    private Set<DetailsLot> details = new HashSet<>();

    private Set<Client> clients = new HashSet<>();
    private Set<LigneChargementFichier> ligneChargementFichiers = new HashSet<>();

    public String numero;
    public String typeActeur;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getEtat() {
        return etat;
    }

    public void setEtat(Boolean etat) {
        this.etat = etat;
    }

    public Instant getDateOuverture() {
        return dateOuverture;
    }

    public void setDateOuverture(Instant dateOuverture) {
        this.dateOuverture = dateOuverture;
    }

    public Instant getDateFermeture() {
        return dateFermeture;
    }

    public void setDateFermeture(Instant dateFermeture) {
        this.dateFermeture = dateFermeture;
    }

    public Boolean getEstImprime() {
        return estImprime;
    }

    public void setEstImprime(Boolean estImprime) {
        this.estImprime = estImprime;
    }

    public Instant getDateImpression() {
        return dateImpression;
    }

    public void setDateImpression(Instant dateImpression) {
        this.dateImpression = dateImpression;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public UniteOrganisation getOrganisation() {
        return organisation;
    }

    public void setOrganisation(UniteOrganisation organisation) {
        this.organisation = organisation;
    }

    public Set<DetailsLot> getDetails() {
        return details;
    }

    public void setDetails(Set<DetailsLot> details) {
        this.details = details;
    }

    
    

    public void setClients(Set<Client> clients) {
        this.clients = clients;
    }

    public Set<Client> getClients() {
        return clients;
    }

    public String getTypeActeur() {
        return typeActeur;
    }

    public void setTypeActeur(String typeActeur) {
        this.typeActeur = typeActeur;
    }

    public Set<LigneChargementFichier> getLigneChargementFichiers() {
        return ligneChargementFichiers;
    }

    public void setLigneChargementFichiers(Set<LigneChargementFichier> ligneChargementFichier) {
        this.ligneChargementFichiers = ligneChargementFichier;
    }
    
    

    @Override
    public String toString() {
        return "LotDTO [clients=" + clients + ", dateFermeture=" + dateFermeture + ", dateImpression=" + dateImpression
                + ", dateOuverture=" + dateOuverture + ", details=" + details + ", estImprime=" + estImprime + ", etat="
                + etat + ", id=" + id + ", organisation=" + organisation + "]";
    }

    // @Override
    // public String toString() {
    //     return "LotDTO{" +
    //     "id=" + getId() +
    //     ", etat='" + getEtat() + "'" +
    //     ", dateOuverture='" + getDateOuverture() + "'" +
    //     ", dateFermeture='" + getDateFermeture() + "'" +
    //     ", estImprime='" + getEstImprime() + "'" +
    //     ", dateImpression='" + getDateImpression() + "'" +
    //     ", clients='" + getClients() + "'" +
    //     "}";
    // }

}
