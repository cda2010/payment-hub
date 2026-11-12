package com.m2i.pti.service.dto;


import com.m2i.pti.domain.enumeration.Action;
import com.m2i.pti.domain.enumeration.Operateur;
import com.m2i.pti.domain.enumeration.Temps;
import com.m2i.pti.domain.enumeration.TypeRegle;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A DTO for the TableauCumul entity.
 */
public class TableauCumulDTO implements Serializable {

    private Long id;

    private Integer poidsNum;

    private String codeRegle;

    private Temps temps;

    private Integer nombre;

    private BigDecimal montant;

    private Action action;

    private LocalDate dateDebut;

    private LocalDate dateFin;

    private TypeRegle typeRegle;

    private Operateur operateur;

    private Long typeServiceId;

    private String typeServiceLibelle;

    private Long canalId;

    private String canalLibelleCanal;

    private Long beneficiaireId;

    private String beneficiaireCodeClient;

    private Long emetteurId;

    private String emetteurCodeClient;

    private Long organisationEmId;

    private String organisationEmLibelleUniteOrganisation;

    private Long organisationAcqId;

    private String organisationAcqLibelleUniteOrganisation;

    private String message ;

    private Boolean activated;

    public Boolean getActivated() {
        return activated;
    }

    public void setActivated(Boolean activated) {
        this.activated = activated;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getPoidsNum() {
        return poidsNum;
    }

    public void setPoidsNum(Integer poidsNum) {
        this.poidsNum = poidsNum;
    }

    public String getCodeRegle() {
        return codeRegle;
    }

    public void setCodeRegle(String codeRegle) {
        this.codeRegle = codeRegle;
    }

    public Temps getTemps() {
        return temps;
    }

    public void setTemps(Temps temps) {
        this.temps = temps;
    }

    public Integer getNombre() {
        return nombre;
    }

    public void setNombre(Integer nombre) {
        this.nombre = nombre;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
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

    public TypeRegle getTypeRegle() {
        return typeRegle;
    }

    public void setTypeRegle(TypeRegle typeRegle) {
        this.typeRegle = typeRegle;
    }

    public Operateur getOperateur() {
        return operateur;
    }

    public void setOperateur(Operateur operateur) {
        this.operateur = operateur;
    }

    public Long getTypeServiceId() {
        return typeServiceId;
    }

    public void setTypeServiceId(Long typeServiceId) {
        this.typeServiceId = typeServiceId;
    }

    public String getTypeServiceLibelle() {
        return typeServiceLibelle;
    }

    public void setTypeServiceLibelle(String typeServiceLibelle) {
        this.typeServiceLibelle = typeServiceLibelle;
    }

    public Long getCanalId() {
        return canalId;
    }

    public void setCanalId(Long canalId) {
        this.canalId = canalId;
    }

    public String getCanalLibelleCanal() {
        return canalLibelleCanal;
    }

    public void setCanalLibelleCanal(String canalLibelleCanal) {
        this.canalLibelleCanal = canalLibelleCanal;
    }

    public Long getBeneficiaireId() {
        return beneficiaireId;
    }

    public void setBeneficiaireId(Long clientId) {
        this.beneficiaireId = clientId;
    }

    public String getBeneficiaireCodeClient() {
        return beneficiaireCodeClient;
    }

    public void setBeneficiaireCodeClient(String clientCodeClient) {
        this.beneficiaireCodeClient = clientCodeClient;
    }

    public Long getEmetteurId() {
        return emetteurId;
    }

    public void setEmetteurId(Long clientId) {
        this.emetteurId = clientId;
    }

    public String getEmetteurCodeClient() {
        return emetteurCodeClient;
    }

    public void setEmetteurCodeClient(String clientCodeClient) {
        this.emetteurCodeClient = clientCodeClient;
    }

    public Long getOrganisationEmId() {
        return organisationEmId;
    }

    public void setOrganisationEmId(Long uniteOrganisationId) {
        this.organisationEmId = uniteOrganisationId;
    }

    public String getOrganisationEmLibelleUniteOrganisation() {
        return organisationEmLibelleUniteOrganisation;
    }

    public void setOrganisationEmLibelleUniteOrganisation(String uniteOrganisationLibelleUniteOrganisation) {
        this.organisationEmLibelleUniteOrganisation = uniteOrganisationLibelleUniteOrganisation;
    }

    public Long getOrganisationAcqId() {
        return organisationAcqId;
    }

    public void setOrganisationAcqId(Long uniteOrganisationId) {
        this.organisationAcqId = uniteOrganisationId;
    }

    public String getOrganisationAcqLibelleUniteOrganisation() {
        return organisationAcqLibelleUniteOrganisation;
    }

    public void setOrganisationAcqLibelleUniteOrganisation(String uniteOrganisationLibelleUniteOrganisation) {
        this.organisationAcqLibelleUniteOrganisation = uniteOrganisationLibelleUniteOrganisation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TableauCumulDTO tableauCumulDTO = (TableauCumulDTO) o;
        if(tableauCumulDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), tableauCumulDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "TableauCumulDTO{" +
            "id=" + getId() +
            ", poidsNum='" + getPoidsNum() + "'" +
            ", codeRegle='" + getCodeRegle() + "'" +
            ", temps='" + getTemps() + "'" +
            ", nombre='" + getNombre() + "'" +
            ", montant='" + getMontant() + "'" +
            ", action='" + getAction() + "'" +
            ", dateDebut='" + getDateDebut() + "'" +
            ", dateFin='" + getDateFin() + "'" +
            ", typeRegle='" + getTypeRegle() + "'" +
            ", operateur='" + getOperateur() + "'" +
            "}";
    }
}
