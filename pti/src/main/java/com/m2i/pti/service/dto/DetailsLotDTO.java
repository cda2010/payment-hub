package com.m2i.pti.service.dto;

import java.io.Serializable;
import java.util.Objects;

import com.m2i.pti.domain.Lot;
import com.m2i.pti.domain.Client;


public class DetailsLotDTO implements Serializable {

    private Long id;
    private Long lotId;
    private Long clientId;
    private String nomClient;
    private String prenomClient;
    private String codeClient;
    private Long ligneChargementFichierId;
    private String identifiant;
    private String telephone;
    private String type;
    private String libelle;

    public DetailsLotDTO(Long id, Long lotId, Long clientId) {
        this.id = id;
        this.lotId = lotId;
        this.clientId = clientId;
    }
    
    public DetailsLotDTO(Long id, Long lotId, Long clientId, Long ligneChargementFichierId) {
        this.id = id;
        this.lotId = lotId;
        this.clientId = clientId;
        this.ligneChargementFichierId = ligneChargementFichierId;
    }

    public DetailsLotDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getLotId() {
        return lotId;
    }

    public void setLotId(Long lotId) {
        this.lotId = lotId;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public String getNomClient() {
        return nomClient;
    }

    public void setNomClient(String nomClient) {
        this.nomClient = nomClient;
    }

    public String getPrenomClient() {
        return prenomClient;
    }

    public void setPrenomClient(String prenomClient) {
        this.prenomClient = prenomClient;
    }

    public String getCodeClient() {
        return codeClient;
    }

    public void setCodeClient(String codeClient) {
        this.codeClient = codeClient;
    }
    
    public Long getLigneChargementFichierId() {
        return ligneChargementFichierId;
    }

    public void setLigneChargementFichierId(Long ligneChargementFichierId) {
        this.ligneChargementFichierId = ligneChargementFichierId;
    }

    public String getIdentifiant() {
        return identifiant;
    }

    public void setIdentifiant(String identifiant) {
        this.identifiant = identifiant;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DetailsLotDTO dLotDTO = (DetailsLotDTO) o;
        if (dLotDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), dLotDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "DetailsLotDTO{" +
                "id=" + getId() +
                "}";
    }
    
}
