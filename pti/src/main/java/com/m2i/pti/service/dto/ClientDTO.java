package com.m2i.pti.service.dto;


import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

/**
 * A DTO for the Client entity.
 */
public class ClientDTO implements Serializable {

    private Long id;

    private String codeClient;

    private Boolean supprime=false;

    private Long personneId;

    private Boolean clientValide;
    private Boolean clientIdentifie;
    private Long profilId;
    private String profilNom;
    private Long profilActeurId;
    private String profilActeurNom;
    private Long groupeProfilActeurId;
    private String groupeProfilActeurNom;

    private Instant createdDate;
    private String createdBy;
    private String lastModifiedBy;
    private Instant lastModifiedDate;
    private String agentEnroleur;
    private Long uniteOrganisationCreateurId;
    private Long reseauId;
    private LocalDate dateValidation;
    private String userValide;
    private Long infosuppId;

  public ClientDTO(Long id, String codeClient, Long personneId, Long profilId, Long profilActeurId, Long groupeProfilActeurId, Boolean clientValide,Long reseauId) {
        this.id = id;
        this.codeClient = codeClient;
        this.personneId = personneId;
        this.profilId = profilId;
        this.profilActeurId = profilActeurId;
        this.groupeProfilActeurId = groupeProfilActeurId;
        this.clientValide=clientValide;
        this.reseauId=reseauId;
    }
    public ClientDTO(Long id, String codeClient, Long personneId, Long profilId, Long profilActeurId, Long groupeProfilActeurId, Boolean clientValide,Long reseauId,LocalDate dateValidation,String userValide) {
        this.id = id;
        this.codeClient = codeClient;
        this.personneId = personneId;
        this.profilId = profilId;
        this.profilActeurId = profilActeurId;
        this.groupeProfilActeurId = groupeProfilActeurId;
        this.clientValide=clientValide;
        this.reseauId=reseauId;
        this.dateValidation=dateValidation;
        this.userValide=userValide;
    }
  


    public ClientDTO() {
    }

    public ClientDTO(Long id, String codeClient, Long personneId, Boolean clientValide, Boolean clientIdentifie, Long profilId, Long profilActeurId,  Long groupeProfilActeurId, Instant createdDate, String createdBy, String lastModifiedBy, Instant lastModifiedDate,Long uniteOrganisationCreateurId,Long reseauId) {
        this.id = id;
        this.codeClient = codeClient;
        this.personneId = personneId;
        this.clientValide = clientValide;
        this.clientIdentifie = clientIdentifie;
        this.profilId = profilId;
        this.profilActeurId = profilActeurId;
        this.groupeProfilActeurId = groupeProfilActeurId;
        this.createdDate = createdDate;
        this.createdBy = createdBy;
        this.lastModifiedBy = lastModifiedBy;
        this.lastModifiedDate = lastModifiedDate;
        this.uniteOrganisationCreateurId = uniteOrganisationCreateurId;
        this.reseauId=reseauId;
    }



    public String getProfilActeurNom() {
        return profilActeurNom;
    }

    public void setProfilActeurNom(String profilActeurNom) {
        this.profilActeurNom = profilActeurNom;
    }

    public Long getProfilId() {
        return profilId;
    }

    public void setProfilId(Long profilId) {
        this.profilId = profilId;
    }

    public String getProfilNom() {
        return profilNom;
    }

    public void setProfilNom(String profilNom) {
        this.profilNom = profilNom;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodeClient() {
        return codeClient;
    }

    public void setCodeClient(String codeClient) {
        this.codeClient = codeClient;
    }

    public Long getPersonneId() {
        return personneId;
    }

    public void setPersonneId(Long personneId) {
        this.personneId = personneId;
    }

    public Boolean getClientValide() {
        return clientValide;
    }

    public void setClientValide(Boolean clientValide) {
        this.clientValide = clientValide;
    }

    public Long getProfilActeurId() {
        return profilActeurId;
    }

    public void setProfilActeurId(Long profilActeurId) {
        this.profilActeurId = profilActeurId;
    }

    public Boolean getClientIdentifie() {
        return clientIdentifie;
    }

    public void setClientIdentifie(Boolean clientIdentifie) {
        this.clientIdentifie = clientIdentifie;
    }

    public Long getGroupeProfilActeurId() {
        return groupeProfilActeurId;
    }

    public void setGroupeProfilActeurId(Long groupeProfilActeurId) {
        this.groupeProfilActeurId = groupeProfilActeurId;
    }

    public String getGroupeProfilActeurNom() {
        return groupeProfilActeurNom;
    }

    public void setGroupeProfilActeurNom(String groupeProfilActeurNom) {
        this.groupeProfilActeurNom = groupeProfilActeurNom;
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

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public Instant getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Instant lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getAgentEnroleur() {
        return agentEnroleur;
    }

    public void setAgentEnroleur(String agentEnroleur) {
        this.agentEnroleur = agentEnroleur;
    }

    public Long getUniteOrganisationCreateurId() {
        return uniteOrganisationCreateurId;
    }

    public Long getReseauId() {
        return reseauId;
    }

    public void setReseauId(Long reseauId) {
        this.reseauId = reseauId;
    }

    public void setUniteOrganisationCreateurId(Long uniteOrganisationCreateurId) {
        this.uniteOrganisationCreateurId = uniteOrganisationCreateurId;
    }

    public LocalDate getDateValidation() {
        return dateValidation;
    }

    public void setDateValidation(LocalDate dateValidation) {
        this.dateValidation = dateValidation;
    }

    public String getUserValide() {
        return userValide;
    }

    public void setUserValide(String userValide) {
        this.userValide = userValide;
    }

    public Long getInfosuppId() {
        return infosuppId;
    }

    public void setInfosuppId(Long infosuppId) {
        this.infosuppId = infosuppId;
    }
    

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ClientDTO clientDTO = (ClientDTO) o;
        if(clientDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), clientDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "ClientDTO{" +
            "id=" + getId() +
            ", codeClient='" + getCodeClient() + "'" +
                ",createDdate='"+getCreatedDate() +"'"+
            "}";
    }
}
