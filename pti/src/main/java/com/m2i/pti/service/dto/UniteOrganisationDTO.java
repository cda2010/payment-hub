package com.m2i.pti.service.dto;

import com.m2i.pti.domain.Localite;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import jakarta.persistence.ColumnResult;

/**
 * A DTO for the UniteOrganisation entity.
 */
public class UniteOrganisationDTO implements Serializable {

    private Long id;

    private String libelleUniteOrganisation;

    private Boolean estSiege;

    private String codeUO;

    private Integer numeroOrdre;

    private Boolean feuille;

    private String codeFse;

    private Boolean estFiliale;

    private String telephone;

    private String codePaysTelephone;

    private String adresse;

    private String email;

    private Integer niveau;
    private Integer niveauTypeUo;
    private Long pereUOId;

    private String pereUOLibelleUniteOrganisation;

    private Long typeUOId;

    private String typeUOLibelleTypeUO;
    private String typeUOCode;

    private Long representantId;

    private Long villeId;

    private String villeNom;

    private Long fournisseurId;

    private String fournisseurLibelleFse;

    private Boolean withPV;

    private String uoPaysLibelle;

    private String codeBanque;

    private PersonneAllDTO personneAllDTO;

    private LocalDate dateCreation;

    private String codePays;

    private Boolean estActif;

    private Long commercialeId;
    private String commercialeNom;
    private String commercialePrenom;
    private Long serviceProfilId;
    private String serviceProfilNom;
    private Long profilActeurId;
    private String profilActeurNom;
    private Long groupeProfilActeurId;
    private String groupeProfilActeurNom;

    private Boolean estUniteRegroupement;

    private LocaliteDTO localiteDTO;

    private Long localiteId;

    private String localiteRegion;

    private String localiteProvince;

    private String localiteCommune;

    private String localiteVille;

    private String localiteSecteur;

    private String localiteQuartier;
    private Long uoPaysId;
    private Integer agentId;
    private Integer marchandId;
    private Integer tagIdentityRef;
    private Integer cosId;
    private Instant createdDate;
    private String createdBy;

    public UniteOrganisationDTO() {
    }

    public UniteOrganisationDTO(
            Long id, String libelleUniteOrganisation, Boolean estSiege, String codeUO, Integer numeroOrdre, String codeFse, Boolean estFiliale,
            String codePaysTelephone, String codePays, String adresse, String email, Integer niveau, Boolean estActif, Long pereUOId, String telephone,
            Long typeUOId, Long profilActeurId, Long serviceProfilId, String codeBanque,
            String pereUOLibelleUniteOrganisation, String typeUOLibelleTypeUO, String typeUOCode, Long representantId, Long villeId, String villeNom, Long fournisseurId, String fournisseurLibelleFse, Boolean withPV, String uoPaysLibelle, PersonneAllDTO personneAllDTO, LocalDate dateCreation, Long commercialeId, String commercialeNom, String commercialePrenom, String serviceProfilNom, String profilActeurNom, String groupeProfilActeurNom, Boolean estUniteRegroupement, LocaliteDTO localiteDTO, Long localiteId, String localiteRegion, String localiteProvince, String localiteCommune, String localiteVille, String localiteSecteur, String localiteQuartier, Boolean feuille, Long groupeProfilActeurId) {

        this.id = id;
        this.libelleUniteOrganisation = libelleUniteOrganisation;
        this.estSiege = estSiege;
        this.codeUO = codeUO;
        this.numeroOrdre = numeroOrdre;
        this.feuille = feuille;
        this.codeFse = codeFse;
        this.estFiliale = estFiliale;
        this.telephone = telephone;
        this.codePaysTelephone = codePaysTelephone;
        this.adresse = adresse;
        this.email = email;
        this.niveau = niveau;
        this.pereUOId = pereUOId;
        this.pereUOLibelleUniteOrganisation = pereUOLibelleUniteOrganisation;
        this.typeUOId = typeUOId;
        this.typeUOLibelleTypeUO = typeUOLibelleTypeUO;
        this.typeUOCode = typeUOCode;
        this.representantId = representantId;
        this.villeId = villeId;
        this.villeNom = villeNom;
        this.fournisseurId = fournisseurId;
        this.fournisseurLibelleFse = fournisseurLibelleFse;
        this.withPV = withPV;
        this.uoPaysLibelle = uoPaysLibelle;
        this.codeBanque = codeBanque;
        this.personneAllDTO = personneAllDTO;
        this.dateCreation = dateCreation;
        this.codePays = codePays;
        this.estActif = estActif;
        this.commercialeId = commercialeId;
        this.commercialeNom = commercialeNom;
        this.commercialePrenom = commercialePrenom;
        this.serviceProfilId = serviceProfilId;
        this.serviceProfilNom = serviceProfilNom;
        this.profilActeurId = profilActeurId;
        this.profilActeurNom = profilActeurNom;
        this.groupeProfilActeurId = groupeProfilActeurId;
        this.groupeProfilActeurNom = groupeProfilActeurNom;
        this.estUniteRegroupement = estUniteRegroupement;
        this.localiteDTO = localiteDTO;
        this.localiteId = localiteId;
        this.localiteRegion = localiteRegion;
        this.localiteProvince = localiteProvince;
        this.localiteCommune = localiteCommune;
        this.localiteVille = localiteVille;
        this.localiteSecteur = localiteSecteur;
        this.localiteQuartier = localiteQuartier;
    }

    public UniteOrganisationDTO(Long id, String libelleUniteOrganisation, Boolean estSiege, String codeUO, Integer numeroOrdre, String codeFse, Boolean estFiliale,
            String codePaysTelephone, String codePays, String adresse, String email, Integer niveau, Boolean estActif, Long pereUOId, String telephone,
            Long typeUOId, Long profilActeurId, Long serviceProfilId, String codeBanque, Long uoPaysId, String typeUOCode, String typeUOLibelleTypeUO, Integer niveauTypeUo) {
        this.id = id;
        this.libelleUniteOrganisation = libelleUniteOrganisation;
        this.estSiege = estSiege;
        this.codeUO = codeUO;
        this.numeroOrdre = numeroOrdre;
        this.codeFse = codeFse;
        this.estFiliale = estFiliale;
        this.codePaysTelephone = codePaysTelephone;
        this.codePays = codePays;
        this.adresse = adresse;
        this.email = email;
        this.niveau = niveau;
        this.estActif = estActif;
        this.pereUOId = pereUOId;
        this.telephone = telephone;
        this.typeUOId = typeUOId;
        this.profilActeurId = profilActeurId;
        this.serviceProfilId = serviceProfilId;
        this.codeBanque = codeBanque;
        this.uoPaysId = uoPaysId;
        this.typeUOLibelleTypeUO = typeUOLibelleTypeUO;
        this.typeUOCode = typeUOCode;
        this.niveauTypeUo = niveauTypeUo;
    }

    public UniteOrganisationDTO(Long id, String libelleUniteOrganisation, Boolean estSiege, String codeUO, Integer numeroOrdre, String codeFse, Boolean estFiliale, String telephone, String codePaysTelephone, String adresse, String email, Integer niveau, Long pereUOId, Long typeUOId, String codeBanque, String codePays, Boolean estActif, Long serviceProfilId, Long profilActeurId, Long uoPaysId) {
        this.id = id;
        this.libelleUniteOrganisation = libelleUniteOrganisation;
        this.estSiege = estSiege;
        this.codeUO = codeUO;
        this.numeroOrdre = numeroOrdre;
        this.codeFse = codeFse;
        this.estFiliale = estFiliale;
        this.telephone = telephone;
        this.codePaysTelephone = codePaysTelephone;
        this.adresse = adresse;
        this.email = email;
        this.niveau = niveau;
        this.pereUOId = pereUOId;
        this.typeUOId = typeUOId;
        this.codeBanque = codeBanque;
        this.codePays = codePays;
        this.estActif = estActif;
        this.serviceProfilId = serviceProfilId;
        this.profilActeurId = profilActeurId;
        this.uoPaysId = uoPaysId;
    }

    public UniteOrganisationDTO(Long id, String libelleUniteOrganisation, Boolean estSiege, String codeUO, String codeFse, String codePaysTelephone) {
        this.id = id;
        this.libelleUniteOrganisation = libelleUniteOrganisation;
        this.estSiege = estSiege;
        this.codeUO = codeUO;
        this.codeFse = codeFse;
        this.codePaysTelephone = codePaysTelephone;
    }

    public UniteOrganisationDTO(Long id, String libelleUniteOrganisation, Boolean estSiege, String codeUO, Integer numeroOrdre, String codeFse, Boolean feuille, Boolean estFiliale,
            String telephone, String codePaysTelephone, String codePays, String adresse, String email, Integer niveau, Integer niveauTypeUo,
            Boolean estActif, Long pereUOId, Long typeUOId, Long profilActeurId, Long serviceProfilId, String codeBanque, Long uoPaysId, String typeUOCode, String typeUOLibelleTypeUO, Long representantId, LocalDate dateCreation, String serviceProfilNom, String profilActeurNom, Long groupeProfilActeurId, String groupeProfilActeurNom, Long villeId,Integer agentId,Integer tagIdentityRef,Integer marchandId,Integer cosId,Long uoZoneId,Long uoFseId,Instant createdDate) {
        this.id = id;
        this.libelleUniteOrganisation = libelleUniteOrganisation;
        this.estSiege = estSiege;
        this.codeUO = codeUO;
        this.numeroOrdre = numeroOrdre;
        this.codeFse = codeFse;
        this.feuille = feuille;
        this.estFiliale = estFiliale;
        this.telephone = telephone;
        this.codePaysTelephone = codePaysTelephone;
        this.codePays = codePays;
        this.adresse = adresse;
        this.email = email;
        this.niveau = niveau;
        this.niveauTypeUo = niveauTypeUo;
        this.estActif = estActif;
        this.pereUOId = pereUOId;
        this.typeUOId = typeUOId;
        this.profilActeurId = profilActeurId;
        this.serviceProfilId = serviceProfilId;
        this.codeBanque = codeBanque;
        this.uoPaysId = uoPaysId;
        this.typeUOCode = typeUOCode;
        this.typeUOLibelleTypeUO = typeUOLibelleTypeUO;
        this.representantId = representantId;
        this.dateCreation = dateCreation;
        this.serviceProfilNom = serviceProfilNom;
        this.profilActeurNom = profilActeurNom;
        this.groupeProfilActeurId = groupeProfilActeurId;
        this.groupeProfilActeurNom = groupeProfilActeurNom;
        this.villeId = villeId;
        this.agentId=agentId;
        this.tagIdentityRef=tagIdentityRef;
        this.marchandId=marchandId;
        this.cosId=cosId;
        this.uoZoneId=uoZoneId;
        this.uoFseId=uoFseId;
        this.createdDate=createdDate;

    }

    private Long uoZoneId;
    private Long uoFseId;

    public String getTypeUOCode() {
        return typeUOCode;
    }

    public void setTypeUOCode(String typeUOCode) {
        this.typeUOCode = typeUOCode;
    }

    public String getCodePays() {
        return codePays;
    }

    public void setCodePays(String codePays) {
        this.codePays = codePays;
    }

    public Long getProfilActeurId() {
        return profilActeurId;
    }

    public void setProfilActeurId(Long profilActeurId) {
        this.profilActeurId = profilActeurId;
    }

    public String getVilleNom() {
        return villeNom;
    }

    public void setVilleNom(String villeNom) {
        this.villeNom = villeNom;
    }

    public Long getVilleId() {
        return villeId;
    }

    public void setVilleId(Long villeId) {
        this.villeId = villeId;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUoPaysLibelle() {
        return uoPaysLibelle;
    }

    public void setUoPaysLibelle(String uoPaysLibelle) {
        this.uoPaysLibelle = uoPaysLibelle;
    }

    public String getLibelleUniteOrganisation() {
        return libelleUniteOrganisation;
    }

    public void setLibelleUniteOrganisation(String libelleUniteOrganisation) {
        this.libelleUniteOrganisation = libelleUniteOrganisation;
    }

    public LocalDate getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDate dateCreation) {
        this.dateCreation = dateCreation;
    }

    public String getCodeBanque() {
        return codeBanque;
    }

    public void setCodeBanque(String codeBanque) {
        this.codeBanque = codeBanque;
    }

    public Boolean isEstSiege() {
        return estSiege;
    }

    public void setEstSiege(Boolean estSiege) {
        this.estSiege = estSiege;
    }

    public String getCodeUO() {
        return codeUO;
    }

    public void setCodeUO(String codeUO) {
        this.codeUO = codeUO;
    }

    public Integer getNumeroOrdre() {
        return numeroOrdre;
    }

    public void setNumeroOrdre(Integer numeroOrdre) {
        this.numeroOrdre = numeroOrdre;
    }

    public Boolean isFeuille() {
        return feuille;
    }

    public void setFeuille(Boolean feuille) {
        this.feuille = feuille;
    }

    public String getCodeFse() {
        return codeFse;
    }

    public void setCodeFse(String codeFse) {
        this.codeFse = codeFse;
    }

    public Boolean isEstFiliale() {
        return estFiliale;
    }

    public void setEstFiliale(Boolean estFiliale) {
        this.estFiliale = estFiliale;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getNiveau() {
        return niveau;
    }

    public void setNiveau(Integer niveau) {
        this.niveau = niveau;
    }

    public Boolean getEstSiege() {
        return estSiege;
    }

    public Boolean getFeuille() {
        return feuille;
    }

    public Boolean getEstFiliale() {
        return estFiliale;
    }

    public Long getPereUOId() {
        return pereUOId;
    }

    public void setPereUOId(Long pereUOId) {
        this.pereUOId = pereUOId;
    }

    public Boolean getEstActif() {
        return estActif;
    }

    public void setEstActif(Boolean estActif) {
        this.estActif = estActif;
    }

    public String getPereUOLibelleUniteOrganisation() {
        return pereUOLibelleUniteOrganisation;
    }

    public void setPereUOLibelleUniteOrganisation(String pereUOLibelleUniteOrganisation) {
        this.pereUOLibelleUniteOrganisation = pereUOLibelleUniteOrganisation;
    }

    public Long getTypeUOId() {
        return typeUOId;
    }

    public void setTypeUOId(Long typeUOId) {
        this.typeUOId = typeUOId;
    }

    public String getTypeUOLibelleTypeUO() {
        return typeUOLibelleTypeUO;
    }

    public void setTypeUOLibelleTypeUO(String typeUOLibelleTypeUO) {
        this.typeUOLibelleTypeUO = typeUOLibelleTypeUO;
    }

    public Long getRepresentantId() {
        return representantId;
    }

    public void setRepresentantId(Long representantId) {
        this.representantId = representantId;
    }

    public Long getFournisseurId() {
        return fournisseurId;
    }

    public void setFournisseurId(Long fournisseurId) {
        this.fournisseurId = fournisseurId;
    }

    public String getFournisseurLibelleFse() {
        return fournisseurLibelleFse;
    }

    public void setFournisseurLibelleFse(String fournisseurLibelleFse) {
        this.fournisseurLibelleFse = fournisseurLibelleFse;
    }

    public Boolean getWithPV() {
        return withPV;
    }

    public void setWithPV(Boolean withPV) {
        this.withPV = withPV;
    }

    public PersonneAllDTO getPersonneAllDTO() {
        return personneAllDTO;
    }

    public void setPersonneAllDTO(PersonneAllDTO personneAllDTO) {
        this.personneAllDTO = personneAllDTO;
    }

    public Long getServiceProfilId() {
        return serviceProfilId;
    }

    public void setServiceProfilId(Long serviceProfilId) {
        this.serviceProfilId = serviceProfilId;
    }

    public String getServiceProfilNom() {
        return serviceProfilNom;
    }

    public void setServiceProfilNom(String serviceProfilNom) {
        this.serviceProfilNom = serviceProfilNom;
    }

    public Long getCommercialeId() {
        return commercialeId;
    }

    public void setCommercialeId(Long commercialeID) {
        this.commercialeId = commercialeID;
    }

    public String getCommercialeNom() {
        return commercialeNom;
    }

    public void setCommercialeNom(String commercialeNom) {
        this.commercialeNom = commercialeNom;
    }

    public String getCommercialePrenom() {
        return commercialePrenom;
    }

    public void setCommercialePrenom(String commercialePrenom) {
        this.commercialePrenom = commercialePrenom;
    }

    public String getProfilActeurNom() {
        return profilActeurNom;
    }

    public void setProfilActeurNom(String profilActeurNom) {
        this.profilActeurNom = profilActeurNom;
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

    public String getCodePaysTelephone() {
        return codePaysTelephone;
    }

    public void setCodePaysTelephone(String codePaysTelephone) {
        this.codePaysTelephone = codePaysTelephone;
    }

    public Boolean getEstUniteRegroupement() {
        return estUniteRegroupement;
    }

    public void setEstUniteRegroupement(Boolean estUniteRegroupement) {
        this.estUniteRegroupement = estUniteRegroupement;
    }

    public LocaliteDTO getLocaliteDTO() {
        return localiteDTO;
    }

    public void setLocaliteDTO(LocaliteDTO localiteDTO) {
        this.localiteDTO = localiteDTO;
    }

    public Long getLocaliteId() {
        return localiteId;
    }

    public void setLocaliteId(Long localiteId) {
        this.localiteId = localiteId;
    }

    public String getLocaliteRegion() {
        return localiteRegion;
    }

    public void setLocaliteRegion(String localiteRegion) {
        this.localiteRegion = localiteRegion;
    }

    public String getLocaliteProvince() {
        return localiteProvince;
    }

    public void setLocaliteProvince(String localiteProvince) {
        this.localiteProvince = localiteProvince;
    }

    public String getLocaliteCommune() {
        return localiteCommune;
    }

    public void setLocaliteCommune(String localiteCommune) {
        this.localiteCommune = localiteCommune;
    }

    public String getLocaliteVille() {
        return localiteVille;
    }

    public void setLocaliteVille(String localiteVille) {
        this.localiteVille = localiteVille;
    }

    public String getLocaliteSecteur() {
        return localiteSecteur;
    }

    public void setLocaliteSecteur(String localiteSecteur) {
        this.localiteSecteur = localiteSecteur;
    }

    public String getLocaliteQuartier() {
        return localiteQuartier;
    }

    public void setLocaliteQuartier(String localiteQuartier) {
        this.localiteQuartier = localiteQuartier;
    }

    public Long getUoPaysId() {
        return uoPaysId;
    }

    public void setUoPaysId(Long uoPaysId) {
        this.uoPaysId = uoPaysId;
    }

    public Integer getNiveauTypeUo() {
        return niveauTypeUo;
    }

    public void setNiveauTypeUo(Integer niveauTypeUo) {
        this.niveauTypeUo = niveauTypeUo;
    }

    public Long getUoZoneId() {
        return uoZoneId;
    }

    public void setUoZoneId(Long uoZoneId) {
        this.uoZoneId = uoZoneId;
    }

    public Long getUoFseId() {
        return uoFseId;
    }

    public void setUoFseId(Long uoFseId) {
        this.uoFseId = uoFseId;
    }

    public Integer getAgentId() {
        return agentId;
    }

    public void setAgentId(Integer agentId) {
        this.agentId = agentId;
    }

    public Integer getMarchandId() {
        return marchandId;
    }

    public void setMarchandId(Integer marchandId) {
        this.marchandId = marchandId;
    }

    public Integer getTagIdentityRef() {
        return tagIdentityRef;
    }

    public void setTagIdentityRef(Integer tagIdentityRef) {
        this.tagIdentityRef = tagIdentityRef;
    }

    public Integer getCosId() {
        return cosId;
    }

    public void setCosId(Integer cosId) {
        this.cosId = cosId;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        UniteOrganisationDTO uniteOrganisationDTO = (UniteOrganisationDTO) o;
        if (uniteOrganisationDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), uniteOrganisationDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "UniteOrganisationDTO{"
                + "id=" + getId()
                + ", libelleUniteOrganisation='" + getLibelleUniteOrganisation() + "'"
                + ", estSiege='" + isEstSiege() + "'"
                + ", codeUO='" + getCodeUO() + "'"
                + ", numeroOrdre=" + getNumeroOrdre()
                + ", feuille='" + isFeuille() + "'"
                + ", codeFse='" + getCodeFse() + "'"
                + ", estFiliale='" + isEstFiliale() + "'"
                + ", telephone='" + getTelephone() + "'"
                + ", email='" + getEmail() + "'"
                + ", niveau=" + getNiveau()
                + ",serviceProfilId=" + getServiceProfilId()
                + "}";
    }
}
