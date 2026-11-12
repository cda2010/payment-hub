package com.m2i.pti.service.dto;

import com.m2i.pti.domain.Client;
import com.m2i.pti.domain.Compte;
import com.m2i.pti.domain.CompteBanque;

public class CompteCBI {
    Compte compteDTO;
    CompteBanque compteBanqueDTO;
    Client clientDTO;
    String numeroCompte;
    String agence;
    String cle;
    String devise;
    String codeBanque;
    String codeAgence;
    String type="Banque";
    Long codeFse;

    public Long getCodeFse() {
        return codeFse;
    }

    public void setCodeFse(Long codeFse) {
        this.codeFse = codeFse;
    }

    public Compte getCompteDTO() {
        return compteDTO;
    }

    public void setCompteDTO(Compte compteDTO) {
        this.compteDTO = compteDTO;
    }

    public CompteBanque getCompteBanqueDTO() {
        return compteBanqueDTO;
    }

    public void setCompteBanqueDTO(CompteBanque compteBanqueDTO) {
        this.compteBanqueDTO = compteBanqueDTO;
    }

    public Client getClientDTO() {
        return clientDTO;
    }

    public void setClientDTO(Client clientDTO) {
        this.clientDTO = clientDTO;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNumeroCompte() {
        return numeroCompte;
    }

    public void setNumeroCompte(String numeroCompte) {
        this.numeroCompte = numeroCompte;
    }

    public String getAgence() {
        return agence;
    }

    public void setAgence(String agence) {
        this.agence = agence;
    }

    public String getCle() {
        return cle;
    }

    public void setCle(String cle) {
        this.cle = cle;
    }

    public String getDevise() {
        return devise;
    }

    public void setDevise(String devise) {
        this.devise = devise;
    }

    public String getCodeBanque() {
        return codeBanque;
    }

    public void setCodeBanque(String codeBanque) {
        this.codeBanque = codeBanque;
    }

    public String getCodeAgence() {
        return codeAgence;
    }

    public void setCodeAgence(String codeAgence) {
        this.codeAgence = codeAgence;
    }
}
