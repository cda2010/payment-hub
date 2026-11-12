package com.m2i.pti.service.dto;

import com.m2i.pti.domain.enumeration.TypePersonne;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import jakarta.persistence.ColumnResult;

/**
 * A DTO for the Personne entity.
 */
public class PersonneDTO implements Serializable {

    private Long id;

    private TypePersonne typePersonne;

    private String telephone;

    private String codePaysTelephone;

    private Long personnePhysiqueId;

    private String email;

    private String adresse;

    private Long personneMoraleId;

    private Long paysCompteId;

    private String paysCompteLibelle;

    private String brancheActiviteLibelle;

    private String keyHash;

    private Long brancheActiviteId;

    private boolean activated;


    private BigDecimal clientSalaire;

    private Long personneLien;

    private String personnePhysiqueNomClient;

    private String personnePhysiquePrenomClient;

    private LocalDate personneDateNaissance;

    private String personneNomMereClient;

    private String typePiece;

    private String refPiece;

    private Long localiteId;

    private String localiteRegion;

    private String localiteProvince;

    private String localiteCommune;

    private String localiteVille;

    private String localiteSecteur;

    private String localiteQuartier;
    private String memo;

    private boolean modifable;

    public PersonneDTO() {
    }

    public PersonneDTO(Long id, String typePersonne, Long brancheActiviteId, Long personneMoraleId, Long personnePhysiqueId,
            String codePaysTelephone, String telephone, Long paysCompteId, String email, String adresse,Long localiteId, String localiteCommune, String localiteProvince, String localiteRegion, String localiteVille, String localiteQuartier, String localiteSecteur) {
        this.id = id;
        this.typePersonne = TypePersonne.valueOf(typePersonne) ;
        this.brancheActiviteId = brancheActiviteId;
        this.personneMoraleId = personneMoraleId;
        this.personnePhysiqueId = personnePhysiqueId;
        this.codePaysTelephone = codePaysTelephone;
        this.telephone = telephone;
        this.paysCompteId = paysCompteId;
        this.email = email;
        this.adresse = adresse;
        this.localiteId=localiteId;
        this.localiteCommune=localiteCommune;
        this.localiteProvince=localiteProvince;
        this.localiteRegion=localiteRegion;
        this.localiteVille=localiteVille;
        this.localiteQuartier=localiteQuartier;
        this.localiteSecteur=localiteSecteur;

    }

    public PersonneDTO(Long id, TypePersonne typePersonne, Long brancheActiviteId, Long personneMoraleId, Long personnePhysiqueId, String codePaysTelephone, String telephone, boolean activated, Long paysCompteId, String email, String adresse, String paysCompteLibelle, String brancheActiviteLibelle, String keyHash, BigDecimal clientSalaire, Long personneLien, String personnePhysiqueNomClient, String personnePhysiquePrenomClient, LocalDate personneDateNaissance, String personneNomMereClient, String typePiece, String refPiece, Long localiteId, String localiteRegion, String localiteProvince, String localiteCommune, String localiteVille, String localiteSecteur, String localiteQuartier) {
        this.id = id;
        this.typePersonne = typePersonne;
        this.brancheActiviteId = brancheActiviteId;
        this.telephone = telephone;
        this.personneMoraleId = personneMoraleId;
        this.personnePhysiqueId = personnePhysiqueId;
        this.codePaysTelephone = codePaysTelephone;
        this.email = email;
        this.adresse = adresse;
        this.paysCompteId = paysCompteId;
        this.paysCompteLibelle = paysCompteLibelle;
        this.brancheActiviteLibelle = brancheActiviteLibelle;
        this.keyHash = keyHash;
        this.activated = activated;
        this.clientSalaire = clientSalaire;
        this.personneLien = personneLien;
        this.personnePhysiqueNomClient = personnePhysiqueNomClient;
        this.personnePhysiquePrenomClient = personnePhysiquePrenomClient;
        this.personneDateNaissance = personneDateNaissance;
        this.personneNomMereClient = personneNomMereClient;
        this.typePiece = typePiece;
        this.refPiece = refPiece;
        this.localiteId = localiteId;
        this.localiteRegion = localiteRegion;
        this.localiteProvince = localiteProvince;
        this.localiteCommune = localiteCommune;
        this.localiteVille = localiteVille;
        this.localiteSecteur = localiteSecteur;
        this.localiteQuartier = localiteQuartier;
    }

    public PersonneDTO(Long id, String typePersonne, String telephone, String codePaysTelephone, Long personnePhysiqueId, String email, String adresse, Long personneMoraleId, Long paysCompteId, String paysCompteLibelle, String keyHash, Long brancheActiviteId, boolean activated, BigDecimal clientSalaire, String personnePhysiqueNomClient, String personnePhysiquePrenomClient, LocalDate personneDateNaissance, String personneNomMereClient, Long localiteId, String localiteRegion, String localiteProvince, String localiteCommune, String localiteVille, String localiteSecteur, String localiteQuartier) {
        this.id = id;
        this.typePersonne = typePersonne!=null?TypePersonne.valueOf(typePersonne):null;
        this.telephone = telephone;
        this.codePaysTelephone = codePaysTelephone;
        this.personnePhysiqueId = personnePhysiqueId;
        this.email = email;
        this.adresse = adresse;
        this.personneMoraleId = personneMoraleId;
        this.paysCompteId = paysCompteId;
        this.paysCompteLibelle = paysCompteLibelle;
        this.keyHash = keyHash;
        this.brancheActiviteId = brancheActiviteId;
        this.activated = activated;
        this.clientSalaire = clientSalaire;
        this.personnePhysiqueNomClient = personnePhysiqueNomClient;
        this.personnePhysiquePrenomClient = personnePhysiquePrenomClient;
        this.personneDateNaissance = personneDateNaissance;
        this.personneNomMereClient = personneNomMereClient;
        this.localiteId = localiteId;
        this.localiteRegion = localiteRegion;
        this.localiteProvince = localiteProvince;
        this.localiteCommune = localiteCommune;
        this.localiteVille = localiteVille;
        this.localiteSecteur = localiteSecteur;
        this.localiteQuartier = localiteQuartier;
    }

    public PersonneDTO(Long id, String typePersonne, Long brancheActiviteId, Long personneMoraleId, Long personnePhysiqueId,
                       String codePaysTelephone, String telephone, Long paysCompteId, String email, String adresse) {
        this.id = id;
        this.typePersonne = TypePersonne.valueOf(typePersonne) ;
        this.brancheActiviteId = brancheActiviteId;
        this.personneMoraleId = personneMoraleId;
        this.personnePhysiqueId = personnePhysiqueId;
        this.codePaysTelephone = codePaysTelephone;
        this.telephone = telephone;
        this.paysCompteId = paysCompteId;
        this.email = email;
        this.adresse = adresse;
    }

    public PersonneDTO(Long id, String typePersonne, Long brancheActiviteId, Long personneMoraleId, Long personnePhysiqueId,
                       String codePaysTelephone, String telephone, Long paysCompteId, String email, String adresse,Boolean activated,Boolean modifable,BigDecimal clientSalaire,Long localiteId, String localiteCommune, String localiteProvince, String localiteRegion, String localiteVille, String localiteQuartier, String localiteSecteur) {
        this.id = id;
        this.typePersonne = TypePersonne.valueOf(typePersonne) ;
        this.brancheActiviteId = brancheActiviteId;
        this.personneMoraleId = personneMoraleId;
        this.personnePhysiqueId = personnePhysiqueId;
        this.codePaysTelephone = codePaysTelephone;
        this.telephone = telephone;
        this.paysCompteId = paysCompteId;
        this.email = email;
        this.adresse = adresse;
        this.activated=activated;
        this.modifable=modifable;
        this.clientSalaire=clientSalaire;
        this.localiteId=localiteId;
        this.localiteCommune=localiteCommune;
        this.localiteProvince=localiteProvince;
        this.localiteRegion=localiteRegion;
        this.localiteVille=localiteVille;
        this.localiteQuartier=localiteQuartier;
        this.localiteSecteur=localiteSecteur;

    }
    
     public PersonneDTO(Long id, String keyHash, String refPiece,String typePiece,String typePersonne,String telephone,String adresse) {
        this.id = id;
        this.keyHash = keyHash;
        this.typePiece = typePiece;
        this.refPiece = refPiece;
        this.typePersonne= TypePersonne.valueOf(typePersonne);
        this.telephone= telephone;
        this.adresse= adresse;
    }

    public PersonneDTO(Long id,String keyHash, String typePersonne, String telephone, String adresse) {
        this.id = id;
        this.typePersonne = TypePersonne.valueOf(typePersonne);
        this.telephone = telephone;
        this.adresse = adresse;
        this.keyHash = keyHash;
    }



    public String getCodePaysTelephone() {
        return codePaysTelephone;
    }

    public void setCodePaysTelephone(String codePaysTelephone) {
        this.codePaysTelephone = codePaysTelephone;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public TypePersonne getTypePersonne() {
        return typePersonne;
    }

    public void setTypePersonne(TypePersonne typePersonne) {
        this.typePersonne = typePersonne;
    }

    public String getTelephone() {
        return telephone;
    }

    public String getBrancheActiviteLibelle() {
        return brancheActiviteLibelle;
    }

    public void setBrancheActiviteLibelle(String brancheActiviteLibelle) {
        this.brancheActiviteLibelle = brancheActiviteLibelle;
    }

    public Long getBrancheActiviteId() {
        return brancheActiviteId;
    }

    public void setBrancheActiviteId(Long brancheActiviteId) {
        this.brancheActiviteId = brancheActiviteId;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public Long getPersonnePhysiqueId() {
        return personnePhysiqueId;
    }

    public void setPersonnePhysiqueId(Long personnePhysiqueId) {
        this.personnePhysiqueId = personnePhysiqueId;
    }

    public Long getPersonneMoraleId() {
        return personneMoraleId;
    }

    public void setPersonneMoraleId(Long personneMoraleId) {
        this.personneMoraleId = personneMoraleId;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public Long getPaysCompteId() {
        return paysCompteId;
    }

    public void setPaysCompteId(Long paysCompteId) {
        this.paysCompteId = paysCompteId;
    }

    public String getPaysCompteLibelle() {
        return paysCompteLibelle;
    }

    public void setPaysCompteLibelle(String paysCompteLibelle) {
        this.paysCompteLibelle = paysCompteLibelle;
    }

    public Long getPersonneLien() {
        return personneLien;
    }

    public void setPersonneLien(Long personneLien) {
        this.personneLien = personneLien;
    }

    public String getPersonnePhysiqueNomClient() {
        return personnePhysiqueNomClient;
    }

    public void setPersonnePhysiqueNomClient(String personnePhysiqueNomClient) {
        this.personnePhysiqueNomClient = personnePhysiqueNomClient;
    }

    public String getPersonnePhysiquePrenomClient() {
        return personnePhysiquePrenomClient;
    }

    public void setPersonnePhysiquePrenomClient(String personnePhysiquePrenomClient) {
        this.personnePhysiquePrenomClient = personnePhysiquePrenomClient;
    }

    public LocalDate getPersonneDateNaissance() {
        return personneDateNaissance;
    }

    public void setPersonneDateNaissance(LocalDate personneDateNaissance) {
        this.personneDateNaissance = personneDateNaissance;
    }

    public String getPersonneNomMereClient() {
        return personneNomMereClient;
    }

    public void setPersonneNomMereClient(String personneNomMereClient) {
        this.personneNomMereClient = personneNomMereClient;
    }

    public String getTypePiece() {
        return typePiece;
    }

    public void setTypePiece(String typePiece) {
        this.typePiece = typePiece;
    }

    public String getRefPiece() {
        return refPiece;
    }

    public void setRefPiece(String refPiece) {
        this.refPiece = refPiece;
    }

    public String getKeyHash() {
        return keyHash;
    }

    public void setKeyHash(String keyHash) {
        this.keyHash = keyHash;
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

    public BigDecimal getClientSalaire() {
        return clientSalaire;
    }

    public void setClientSalaire(BigDecimal clientSalaire) {
        this.clientSalaire = clientSalaire;
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

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }
    

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PersonneDTO personneDTO = (PersonneDTO) o;
        if (personneDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), personneDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "PersonneDTO{"
                + "id=" + getId()
                + ", typePersonne='" + getTypePersonne() + "'"
                + ", telephone='" + getTelephone() + "'"
                + "}";
    }
}
