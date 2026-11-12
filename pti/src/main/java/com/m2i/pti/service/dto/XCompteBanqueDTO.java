package com.m2i.pti.service.dto;

import jakarta.validation.constraints.NotNull;

public class XCompteBanqueDTO {
    private Long id;

    private String codeAgence;

    private String codeBanque;

    private String codeFse;

    private Long clientId;

    private String valeurCompte;

    private String rib;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodeAgence() {
        return codeAgence;
    }

    public void setCodeAgence(String codeAgence) {
        this.codeAgence = codeAgence;
    }

    public String getCodeBanque() {
        return codeBanque;
    }

    public void setCodeBanque(String codeBanque) {
        this.codeBanque = codeBanque;
    }

    public String getCodeFse() {
        return codeFse;
    }

    public void setCodeFse(String codeFse) {
        this.codeFse = codeFse;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public String getValeurCompte() {
        return valeurCompte;
    }

    public void setValeurCompte(String valeurCompte) {
        this.valeurCompte = valeurCompte;
    }

    public String getRib() {
        return rib;
    }

    public void setRib(String rib) {
        this.rib = rib;
    }


    @Override
    public String toString() {
        return "XCompteBanqueDTO{" +
                "id=" + id +
                ", codeAgence='" + codeAgence + '\'' +
                ", codeBanque='" + codeBanque + '\'' +
                ", codeFse='" + codeFse + '\'' +
                ", clientId=" + clientId +
                ", valeurCompte='" + valeurCompte + '\'' +
                ", rib='" + rib + '\'' +
                '}';
    }
}
