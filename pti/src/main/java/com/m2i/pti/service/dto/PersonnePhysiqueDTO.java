package com.m2i.pti.service.dto;


import com.m2i.pti.domain.enumeration.Sexe;

import jakarta.persistence.Lob;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A DTO for the PersonnePhysique entity.
 */
public class PersonnePhysiqueDTO implements Serializable {

    private Long id;

    private String nomClient;

    private String prenomClient;

    private LocalDate dateNaissance;

    private String personnePrevenir;

    private String lieuNaissance;

    private Sexe sexe;

    private Long nationaliteId;

    private String nationaliteLibelle;

    private Long paysResidenceId;

    private String paysResidenceLibelle;
    private String nomMereClient;
    private String profession;
    private String nomJeuneFille;


    @Lob
    private byte[] photo;
    private String photoContentType;

    public PersonnePhysiqueDTO() {
    }

    public PersonnePhysiqueDTO(Long id, String nomClient,
                               String prenomClient,
                               String lieuNaissance,
                               LocalDate dateNaissance,
                               Long paysResidenceId,
                               String sexe,
                               String nationaliteLibelle,
                               Long nationaliteId
    ) {
        this.id = id;
        this.nomClient = nomClient;
        this.prenomClient = prenomClient;
        this.lieuNaissance = lieuNaissance;
        this.dateNaissance = dateNaissance;
        this.paysResidenceId = paysResidenceId;
        this.sexe = sexe!=null?Sexe.valueOf(sexe):null;
        this.nationaliteLibelle = nationaliteLibelle;
        this.nationaliteId = nationaliteId;
    }

    public PersonnePhysiqueDTO(Long id, String nomClient,
                               String prenomClient,
                               String lieuNaissance,
                               LocalDate dateNaissance,
                               String nomMereClient,
                               String personnePrevenir,
                               Long paysResidenceId,
                               String sexe,
                               String nationaliteLibelle,
                               Long nationaliteId
    ) {
        this.id = id;
        this.nomClient = nomClient;
        this.prenomClient = prenomClient;
        this.lieuNaissance = lieuNaissance;
        this.dateNaissance = dateNaissance;
        this.nomMereClient=nomMereClient;
        this.personnePrevenir=personnePrevenir;
        this.paysResidenceId = paysResidenceId;
        this.sexe = sexe!=null?Sexe.valueOf(sexe):null;
        this.nationaliteLibelle = nationaliteLibelle;
        this.nationaliteId = nationaliteId;
    }



    public Long getPaysResidenceId() {
        return paysResidenceId;
    }

    public void setPaysResidenceId(Long paysResidenceId) {
        this.paysResidenceId = paysResidenceId;
    }

    public String getPaysResidenceLibelle() {
        return paysResidenceLibelle;
    }

    public void setPaysResidenceLibelle(String paysResidenceLibelle) {
        this.paysResidenceLibelle = paysResidenceLibelle;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public LocalDate getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(LocalDate dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    public String getPersonnePrevenir() {
        return personnePrevenir;
    }

    public void setPersonnePrevenir(String personnePrevenir) {
        this.personnePrevenir = personnePrevenir;
    }

    public String getLieuNaissance() {
        return lieuNaissance;
    }

    public void setLieuNaissance(String lieuNaissance) {
        this.lieuNaissance = lieuNaissance;
    }

    public Sexe getSexe() {
        return sexe;
    }

    public void setSexe(Sexe sexe) {
        this.sexe = sexe;
    }

    public Long getNationaliteId() {
        return nationaliteId;
    }

    public void setNationaliteId(Long nationaliteId) {
        this.nationaliteId = nationaliteId;
    }

    public String getNationaliteLibelle() {
        return nationaliteLibelle;
    }

    public void setNationaliteLibelle(String nationaliteLibelle) {
        this.nationaliteLibelle = nationaliteLibelle;
    }

    public byte[] getPhoto() {
        return photo;
    }

    public void setPhoto(byte[] photo) {
        this.photo = photo;
    }

    public String getPhotoContentType() {
        return photoContentType;
    }

    public void setPhotoContentType(String photoContentType) {
        this.photoContentType = photoContentType;
    }

    public String getNomMereClient() {
        return nomMereClient;
    }

    public void setNomMereClient(String nomMereClient) {
        this.nomMereClient = nomMereClient;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public String getNomJeuneFille() {
        return nomJeuneFille;
    }

    public void setNomJeuneFille(String nomJeuneFille) {
        this.nomJeuneFille = nomJeuneFille;
    }

    @Override
    public String toString() {
        return "PersonnePhysiqueDTO{" +
            "id=" + getId() +
            ", nomClient='" + getNomClient() + "'" +
            ", prenomClient='" + getPrenomClient() + "'" +
            ", dateNaissance='" + getDateNaissance() + "'" +
            ", personnePrevenir='" + getPersonnePrevenir() + "'" +
            ", lieuNaissance='" + getLieuNaissance() + "'" +
            ", sexe='" + getSexe() + "'" +
            "}";
    }
}
