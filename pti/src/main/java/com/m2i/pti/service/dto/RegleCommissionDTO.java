package com.m2i.pti.service.dto;


import com.m2i.pti.domain.enumeration.CategorieCompte;
import com.m2i.pti.domain.enumeration.TypeOperateur;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import jakarta.persistence.Column;

/**
 * A DTO for the RegleCommission entity.
 */
public class RegleCommissionDTO implements Serializable {

    private Long id;

    private String etiquette;

    private TypeOperateur typeOperateur;

    private CategorieCompte categorieCompte;

    private LocalDate dateDebut;

    private LocalDate dateFin;


    private Long priseEnChargeId;

    private String priseEnChargeLibelle;

    private Long typeServiceId;

    private Long teneurSrcId;

    private Long teneurDestId;

    private Long uoSrcId;

    private Long uoDestId;

    private String teneurSrcLibelle;

    private String teneurDestLibelle;

    private String uoSrcLibelle;

    private String uoDestLibelle;

    
    private BigDecimal min;

   
    private BigDecimal max;

    private String typeServiceLibelle;

    private String valeurCompte;

    private String codeAgence;

    private String codeBanque;


    private String rib;

    private String codeFse;

    private Boolean activated;

    public Boolean getActivated() {
        return activated;
    }

    public void setActivated(Boolean activated) {
        this.activated = activated;
    }

    public Long getPriseEnChargeId() {
        return priseEnChargeId;
    }

    public void setPriseEnChargeId(Long priseEnChargeId) {
        this.priseEnChargeId = priseEnChargeId;
    }

    public String getPriseEnChargeLibelle() {
        return priseEnChargeLibelle;
    }

    public void setPriseEnChargeLibelle(String priseEnChargeLibelle) {
        this.priseEnChargeLibelle = priseEnChargeLibelle;
    }

    public CategorieCompte getCategorieCompte() {
        return categorieCompte;
    }

    public void setCategorieCompte(CategorieCompte categorieCompte) {
        this.categorieCompte = categorieCompte;
    }

    public String getValeurCompte() {
        return valeurCompte;
    }

    public void setValeurCompte(String valeurCompte) {
        this.valeurCompte = valeurCompte;
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

    public String getRib() {
        return rib;
    }

    public void setRib(String rib) {
        this.rib = rib;
    }

    public String getCodeFse() {
        return codeFse;
    }

    public void setCodeFse(String codeFse) {
        this.codeFse = codeFse;
    }


    public Long getUoSrcId() {
        return uoSrcId;
    }

    public void setUoSrcId(Long uoSrcId) {
        this.uoSrcId = uoSrcId;
    }

    public Long getUoDestId() {
        return uoDestId;
    }

    public void setUoDestId(Long uoDestId) {
        this.uoDestId = uoDestId;
    }

    public String getTypeServiceLibelle() {
        return typeServiceLibelle;
    }

    public void setTypeServiceLibelle(String typeServiceLibelle) {
        this.typeServiceLibelle = typeServiceLibelle;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTeneurSrcLibelle() {
        return teneurSrcLibelle;
    }

    public void setTeneurSrcLibelle(String teneurSrcLibelle) {
        this.teneurSrcLibelle = teneurSrcLibelle;
    }

    public String getTeneurDestLibelle() {
        return teneurDestLibelle;
    }

    public void setTeneurDestLibelle(String teneurDestLibelle) {
        this.teneurDestLibelle = teneurDestLibelle;
    }

    public String getEtiquette() {
        return etiquette;
    }

    public void setEtiquette(String etiquette) {
        this.etiquette = etiquette;
    }

    public TypeOperateur getTypeOperateur() {
        return typeOperateur;
    }

    public void setTypeOperateur(TypeOperateur typeOperateur) {
        this.typeOperateur = typeOperateur;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }

    public Long getTypeServiceId() {
        return typeServiceId;
    }

    public void setTypeServiceId(Long typeServiceId) {
        this.typeServiceId = typeServiceId;
    }

    public Long getTeneurSrcId() {
        return teneurSrcId;
    }

    public void setTeneurSrcId(Long uniteOrganisationId) {
        this.teneurSrcId = uniteOrganisationId;
    }

    public Long getTeneurDestId() {
        return teneurDestId;
    }

    public void setTeneurDestId(Long uniteOrganisationId) {
        this.teneurDestId = uniteOrganisationId;
    }

    public String getUoSrcLibelle() {
        return uoSrcLibelle;
    }

    public void setUoSrcLibelle(String uoSrcLibelle) {
        this.uoSrcLibelle = uoSrcLibelle;
    }

    public String getUoDestLibelle() {
        return uoDestLibelle;
    }

    public void setUoDestLibelle(String uoDestLibelle) {
        this.uoDestLibelle = uoDestLibelle;
    }

    public BigDecimal getMin() {
        return min;
    }

    public void setMin(BigDecimal min) {
        this.min = min;
    }

    public BigDecimal getMax() {
        return max;
    }

    public void setMax(BigDecimal max) {
        this.max = max;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RegleCommissionDTO regleCommissionDTO = (RegleCommissionDTO) o;
        if (regleCommissionDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), regleCommissionDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "RegleCommissionDTO{" +
                "id=" + id +
                ", etiquette='" + etiquette + '\'' +
                ", typeOperateur=" + typeOperateur +
                ", dateDebut=" + dateDebut +
                ", dateFin=" + dateFin +
                ", typeServiceId=" + typeServiceId +
                ", teneurSrcId=" + teneurSrcId +
                ", teneurDestId=" + teneurDestId +
                ", uoSrcId=" + uoSrcId +
                ", uoDestId=" + uoDestId +
                ", teneurSrcLibelle='" + teneurSrcLibelle + '\'' +
                ", teneurDestLibelle='" + teneurDestLibelle + '\'' +
                ", typeServiceLibelle='" + typeServiceLibelle + '\'' +
                '}';
    }
}
