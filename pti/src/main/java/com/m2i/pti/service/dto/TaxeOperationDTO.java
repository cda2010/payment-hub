package com.m2i.pti.service.dto;

import com.m2i.pti.domain.enumeration.MontantsAComptabilise;
import com.m2i.pti.domain.enumeration.TypeTarif;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A DTO for the Taxe entity.
 */
public class TaxeOperationDTO implements Serializable {

    private Long id;

    @NotNull
    private TypeTarif modeCalcul;

    @NotNull
    private MontantsAComptabilise appliqueSur;

    @NotNull
    private BigDecimal valeur;

    private LocalDate dateDebut;

    private LocalDate dateFin;

    private Set<TypeServiceDTO> typeServices = new HashSet<>();


    private Long uniteOrganisationId;

    private String libelleUniteOrganisation;

    private Long zoneId;

    private String libelleZone;

    private Long taxeId;

    private String taxeNom;

    private String libelle;

    private BigDecimal montant;


    private Boolean activated;

    public Boolean getActivated() {
        return activated;
    }

    public void setActivated(Boolean activated) {
        this.activated = activated;
    }

    public Long getZoneId() {
        return zoneId;
    }

    public void setZoneId(Long zoneId) {
        this.zoneId = zoneId;
    }

    public String getLibelleZone() {
        return libelleZone;
    }

    public void setLibelleZone(String libelleZone) {
        this.libelleZone = libelleZone;
    }

    public String getLibelleUniteOrganisation() {
        return libelleUniteOrganisation;
    }

    public Long getTaxeId() {
        return taxeId;
    }

    public void setTaxeId(Long taxeId) {
        this.taxeId = taxeId;
    }

    public String getTaxeNom() {
        return taxeNom;
    }

    public void setTaxeNom(String taxeNom) {
        this.taxeNom = taxeNom;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }

    public void setLibelleUniteOrganisation(String libelleUniteOrganisation) {
        this.libelleUniteOrganisation = libelleUniteOrganisation;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUniteOrganisationId() {
        return uniteOrganisationId;
    }

    public void setUniteOrganisationId(Long uniteOrganisationId) {
        this.uniteOrganisationId = uniteOrganisationId;
    }

    public TypeTarif getModeCalcul() {
        return modeCalcul;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public void setModeCalcul(TypeTarif modeCalcul) {
        this.modeCalcul = modeCalcul;
    }

    public MontantsAComptabilise getAppliqueSur() {
        return appliqueSur;
    }

    public void setAppliqueSur(MontantsAComptabilise appliqueSur) {
        this.appliqueSur = appliqueSur;
    }

    public BigDecimal getValeur() {
        return valeur;
    }

    public void setValeur(BigDecimal valeur) {
        this.valeur = valeur;
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

    public Set<TypeServiceDTO> getTypeServices() {
        return typeServices;
    }

    public void setTypeServices(Set<TypeServiceDTO> typeServices) {
        this.typeServices = typeServices;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TaxeDTO taxeDTO = (TaxeDTO) o;
        if (taxeDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), taxeDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "TaxeDTO{" +
                "id=" + getId() +
                ", modeCalcul='" + getModeCalcul() + "'" +
                ", appliqueSur='" + getAppliqueSur() + "'" +
                ", valeur=" + getValeur() +
                ", dateDebut='" + getDateDebut() + "'" +
                ", dateFin='" + getDateFin() + "'" +
                ",type services='"+getTypeServices()+"'"+
                "}";
    }
}
