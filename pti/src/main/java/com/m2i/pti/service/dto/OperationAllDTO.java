package com.m2i.pti.service.dto;


public class OperationAllDTO {
    private OperationDTO operationDTO;
    private PersonneAllDTO beneficiaire;
    private PersonneAllDTO emetteur;
    private CompteDTO compteDTOEmetteur;
    private CompteDTO compteDTOBeneficiaire;
    private String typeBeneficiaire;
    private String rrn;
    private String typeService;
    private String codeUo;


    public String getCodeUo() {
        return codeUo;
    }

    public void setCodeUo(String codeUo) {
        this.codeUo = codeUo;
    }

    public String getTypeService() {
        return typeService;
    }

    public void setTypeService(String typeService) {
        this.typeService = typeService;
    }

    public OperationDTO getOperationDTO() {
        return operationDTO;
    }

    public void setOperationDTO(OperationDTO operationDTO) {
        this.operationDTO = operationDTO;
    }

    public PersonneAllDTO getBeneficiaire() {
        return beneficiaire;
    }

    public void setBeneficiaire(PersonneAllDTO beneficiaire) {
        this.beneficiaire = beneficiaire;
    }

    public PersonneAllDTO getEmetteur() {
        return emetteur;
    }

    public void setEmetteur(PersonneAllDTO emetteur) {
        this.emetteur = emetteur;
    }

    public String getRrn() {
        return rrn;
    }

    public void setRrn(String rrn) {
        this.rrn = rrn;
    }

    public String getTypeBeneficiaire() {
        return typeBeneficiaire;
    }

    public void setTypeBeneficiaire(String typeBeneficiaire) {
        this.typeBeneficiaire = typeBeneficiaire;
    }

    public CompteDTO getCompteDTOEmetteur() {
        return compteDTOEmetteur;
    }

    public void setCompteDTOEmetteur(CompteDTO compteDTOEmetteur) {
        this.compteDTOEmetteur = compteDTOEmetteur;
    }

    public CompteDTO getCompteDTOBeneficiaire() {
        return compteDTOBeneficiaire;
    }

    public void setCompteDTOBeneficiaire(CompteDTO compteDTOBeneficiaire) {
        this.compteDTOBeneficiaire = compteDTOBeneficiaire;
    }
}
