package com.m2i.pti.service.dto;

import com.m2i.pti.domain.Compte;
import com.m2i.pti.domain.Localite;

import java.util.List;

public class ClientAllDTO {
    private ClientDTO clientDTO;
    private String nom;
     private Long codeFse ;
    private boolean isCreateCompte;
    private String telephone;
    private PersonneDTO personneDTO;
    private PersonnePhysiqueDTO personnePhysiqueDTO;
    private PersonneMoraleDTO personneMoraleDTO;
    private PieceDTO pieceDTO;
    private List<CompteDTO> compteDTOS;
    private  List<NotificationDTO> notificationDTOS;
    private List<Long> typeServiceIds;
    private List<DocumentDTO> documentDTOS;
    private List<Long> compteIdRemoved;
    private Localite localite;

    public ClientDTO getClientDTO() {
        return clientDTO;
    }

    public void setClientDTO(ClientDTO clientDTO) {
        this.clientDTO = clientDTO;
    }

    public List<Long> getTypeServiceIds() {
        return typeServiceIds;
    }

    public void setTypeServiceIds(List<Long> typeServiceIds) {
        this.typeServiceIds = typeServiceIds;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public List<CompteDTO> getCompteDTOS() {
        return compteDTOS;
    }

    public void setCompteDTOS(List<CompteDTO> compteDTOS) {
        this.compteDTOS = compteDTOS;
    }

    public PersonneDTO getPersonneDTO() {
        return personneDTO;
    }

    public void setPersonneDTO(PersonneDTO personneDTO) {
        this.personneDTO = personneDTO;
    }

    public PersonnePhysiqueDTO getPersonnePhysiqueDTO() {
        return personnePhysiqueDTO;
    }

    public void setPersonnePhysiqueDTO(PersonnePhysiqueDTO personnePhysiqueDTO) {
        this.personnePhysiqueDTO = personnePhysiqueDTO;
    }

    public boolean isCreateCompte() {
        return isCreateCompte;
    }

    public void setCreateCompte(boolean createCompte) {
        isCreateCompte = createCompte;
    }

    public List<NotificationDTO> getNotificationDTOS() {
        return notificationDTOS;
    }

    public void setNotificationDTOS(List<NotificationDTO> notificationDTOS) {
        this.notificationDTOS = notificationDTOS;
    }

    public PersonneMoraleDTO getPersonneMoraleDTO() {
        return personneMoraleDTO;
    }

    public void setPersonneMoraleDTO(PersonneMoraleDTO personneMoraleDTO) {
        this.personneMoraleDTO = personneMoraleDTO;
    }

    public PieceDTO getPieceDTO() {
        return pieceDTO;
    }

    public void setPieceDTO(PieceDTO pieceDTO) {
        this.pieceDTO = pieceDTO;
    }

    public List<DocumentDTO> getDocumentDTOS() {
        return documentDTOS;
    }

    public void setDocumentDTOS(List<DocumentDTO> documentDTOS) {
        this.documentDTOS = documentDTOS;
    }

    public Long getCodeFse() {
        return codeFse;
    }

    public void setCodeFse(Long codeFse) {
        this.codeFse = codeFse;
    }

    public boolean isIsCreateCompte() {
        return isCreateCompte;
    }

    public void setIsCreateCompte(boolean isCreateCompte) {
        this.isCreateCompte = isCreateCompte;
    }


    public List<Long> getCompteIdRemoved() {
        return compteIdRemoved;
    }

    public Localite getLocalite() {
        return localite;
    }

    public void setLocalite(Localite localite) {
        this.localite = localite;
    }

    public void setCompteIdRemoved(List<Long> compteIdRemoved) {
        this.compteIdRemoved = compteIdRemoved;
    }
}
