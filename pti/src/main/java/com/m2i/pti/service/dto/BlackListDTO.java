package com.m2i.pti.service.dto;


import com.m2i.pti.domain.enumeration.ActionBlackList;

import java.io.Serializable;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * A DTO for the BlackList entity.
 */
public class BlackListDTO implements Serializable {

    private Long id;

    private Long clientId;

    private ActionBlackList actionBlackList;
    private  String nomClient;
    private  String prenomClient;

    private  String telephone;

    private  String typePersonne;

    private String nomMereClient;
    private String createdBy;
    private Boolean supprime;
    private Instant createdDate;
    private String actionBlack;

    public BlackListDTO() {
    }

    public BlackListDTO(Long id, Long clientId, String actionBlack, String createdBy, Instant createdDate, Boolean supprime) {
        this.id = id;
        this.clientId = clientId;
        this.actionBlack = actionBlack;
        this.createdBy = createdBy;
        this.createdDate = createdDate;
        this.supprime = supprime;
    }

    public ActionBlackList getActionBlackList() {
        return actionBlackList;
    }

    public void setActionBlackList(ActionBlackList actionBlackList) {
        this.actionBlackList = actionBlackList;
    }


    public String getPrenomClient() {
        return prenomClient;
    }

    public void setPrenomClient(String prenomClient) {
        this.prenomClient = prenomClient;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getTypePersonne() {
        return typePersonne;
    }

    public void setTypePersonne(String typePersonne) {
        this.typePersonne = typePersonne;
    }



    public String getNomMereClient() {
        return nomMereClient;
    }

    public void setNomMereClient(String nomMereClient) {
        this.nomMereClient = nomMereClient;
    }



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Boolean getSupprime() {
        return supprime;
    }

    public void setSupprime(Boolean supprime) {
        this.supprime = supprime;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public String getActionBlack() {
        return actionBlack;
    }

    public void setActionBlack(String actionBlack) {
        this.actionBlack = actionBlack;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BlackListDTO blackListDTO = (BlackListDTO) o;
        if(blackListDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), blackListDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "BlackListDTO{" +
            "id=" + getId() +
            "}";
    }
}
