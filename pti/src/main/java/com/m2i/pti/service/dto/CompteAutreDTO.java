package com.m2i.pti.service.dto;


import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

/**
 * A DTO for the CompteAutre entity.
 */
public class CompteAutreDTO implements Serializable {

    private Long id;

    private BigDecimal solde;

    private String codeFse;
    private String nomClient;
    private String prenomClient;
    private String raisonSociale;
    private String profession;
    private String telephone;
    private String codeTel;
    private String libelle;
    private String profile;
    private String telephoneOrganisation;
    private String indicatifPaysOrganisation;



    public String getTelephoneOrganisation() {
        return this.telephoneOrganisation;
    }

    public void setTelephoneOrganisation(String telephoneOrganisation) {
        this.telephoneOrganisation = telephoneOrganisation;
    }

    public String getIndicatifPaysOrganisation() {
        return this.indicatifPaysOrganisation;
    }

    public void setIndicatifPaysOrganisation(String indicatifPaysOrganisation) {
        this.indicatifPaysOrganisation = indicatifPaysOrganisation;
    }

    public String getLibelle() {
        return this.libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public String getProfile() {
        return this.profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }


    public String getCodeTel() {
        return this.codeTel;
    }

    public void setCodeTel(String codeTel) {
        this.codeTel = codeTel;
    }

    public String getTelephone() {
        return this.telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getRaisonSociale() {
        return this.raisonSociale;
    }

    public void setRaisonSociale(String raisonSociale) {
        this.raisonSociale = raisonSociale;
    }


    public String getNomClient() {
        return this.nomClient;
    }

    public void setNomClient(String nomClient) {
        this.nomClient = nomClient;
    }

    public String getPrenomClient() {
        return this.prenomClient;
    }

    public void setPrenomClient(String prenomClient) {
        this.prenomClient = prenomClient;
    }

    public String getProfession() {
        return this.profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    
    public CompteAutreDTO() {
    }

    public CompteAutreDTO(Long id, BigDecimal solde, String codeFse) {
        this.id = id;
        this.solde = solde;
        this.codeFse = codeFse;
    }

    public CompteAutreDTO(Long id, BigDecimal solde, String codeFse, String nomClient, String prenomClient, String raisonSociale, String telephone, 
        String codeTel,String libelle, String profile, String telephoneOrganisation, String indicatifPaysOrganisation) {
        this.id = id;
        this.solde = solde;
        this.codeFse = codeFse;
        this.nomClient = nomClient;
        this.prenomClient = prenomClient;
        this.raisonSociale = raisonSociale;
        this.telephone = telephone;
        this.codeTel = codeTel;
        this.libelle = libelle;
        this.profile = profile;
        this.telephoneOrganisation = telephoneOrganisation;
        this.indicatifPaysOrganisation = indicatifPaysOrganisation;
    }

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

    public BigDecimal getSolde() {
        return solde;
    }

    public void setSolde(BigDecimal solde) {
        this.solde = solde;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CompteAutreDTO compteAutreDTO = (CompteAutreDTO) o;
        if(compteAutreDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), compteAutreDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }



    @Override
    public String toString() {
        return "{" +
            " id='" + getId() + "'" +
            ", solde='" + getSolde() + "'" +
            ", codeFse='" + getCodeFse() + "'" +
            ", nomClient='" + getNomClient() + "'" +
            ", prenomClient='" + getPrenomClient() + "'" +
            ", raisonSociale='" + getRaisonSociale() + "'" +
            ", profession='" + getProfession() + "'" +
            ", telephone='" + getTelephone() + "'" +
            ", codeTel='" + getCodeTel() + "'" +
            ", libelle='" + getLibelle() + "'" +
            ", profile='" + getProfile() + "'" +
            "}";
    }
    
    
}
