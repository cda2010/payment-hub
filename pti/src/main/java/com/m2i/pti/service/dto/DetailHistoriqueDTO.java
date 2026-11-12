package com.m2i.pti.service.dto;


import com.m2i.pti.domain.enumeration.HistoryType;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

/**
 * A DTO for the DetailHistorique entity.
 */
public class DetailHistoriqueDTO implements Serializable {

    private Long id;

    private String numeroCpt;

    private String libelle;

    private BigDecimal solde;

    private Long historiqueId;

    private HistoryType etiquette;

    private String telephone;

    private String codeUo;

    private String typeUo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumeroCpt() {
        return numeroCpt;
    }

    public void setNumeroCpt(String numeroCpt) {
        this.numeroCpt = numeroCpt;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public BigDecimal getSolde() {
        return solde;
    }

    public void setSolde(BigDecimal solde) {
        this.solde = solde;
    }

    public Long getHistoriqueId() {
        return historiqueId;
    }

    public void setHistoriqueId(Long historiqueId) {
        this.historiqueId = historiqueId;
    }

    public HistoryType getEtiquette() {
        return etiquette;
    }

    public void setEtiquette(HistoryType etiquette) {
        this.etiquette = etiquette;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getCodeUo() {
        return codeUo;
    }

    public void setCodeUo(String codeUo) {
        this.codeUo = codeUo;
    }

    public String getTypeUo() {
        return typeUo;
    }

    public void setTypeUo(String typeUo) {
        this.typeUo = typeUo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DetailHistoriqueDTO detailHistoriqueDTO = (DetailHistoriqueDTO) o;
        if(detailHistoriqueDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), detailHistoriqueDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "DetailHistoriqueDTO{" +
            "id=" + getId() +
            ", numeroCpt='" + getNumeroCpt() + "'" +
            ", libelle='" + getLibelle() + "'" +
            ", solde='" + getSolde() + "'" +
            "}";
    }
}
