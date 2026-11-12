package com.m2i.pti.service.dto;

import java.util.List;

public class PersonneAllDTO {
    private PersonneDTO personneDTO;
    private PersonnePhysiqueDTO personnePhysiqueDTO;
    private PersonneMoraleDTO personneMoraleDTO;
    private PieceDTO pieceDTO;
    private ClientDTO clientDTO;
    private CompteCBI compteCBIDTO;
    private List<CompteCBI> compteCBIS;

    public PersonneAllDTO(PersonneAllDTO other) {
        this.personneDTO = other.personneDTO;
        this.personnePhysiqueDTO = other.personnePhysiqueDTO;
        this.personneMoraleDTO = other.personneMoraleDTO;
        this.pieceDTO = other.pieceDTO;
        this.clientDTO = other.clientDTO;
        this.compteCBIDTO = other.compteCBIDTO;
        this.compteCBIS = other.compteCBIS;
        this.pieces = other.pieces;
    }

    public PersonneAllDTO() {
    }

    private List<PieceDTO> pieces;

    public List<PieceDTO> getPieces() {
        return pieces;
    }

    public void setPieces(List<PieceDTO> pieces) {
        this.pieces = pieces;
    }

    public ClientDTO getClientDTO() {
        return clientDTO;
    }

    public void setClientDTO(ClientDTO clientDTO) {
        this.clientDTO = clientDTO;
    }

    public CompteCBI getCompteCBIDTO() {
        return compteCBIDTO;
    }

    public void setCompteCBIDTO(CompteCBI compteCBIDTO) {
        this.compteCBIDTO = compteCBIDTO;
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

    public List<CompteCBI> getCompteCBIS() {
        return compteCBIS;
    }

    public void setCompteCBIS(List<CompteCBI> compteCBIS) {
        this.compteCBIS = compteCBIS;
    }
}
