package com.m2i.pti.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.m2i.pti.config.audit.EntityAuditEventListener;
import com.m2i.pti.domain.enumeration.SourceCalcul;
import com.m2i.pti.domain.enumeration.TypeRegle;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A TableauFrais.
 */
@Entity
@Table(name = "tableau_frais")
@EntityListeners(value = {EntityAuditEventListener.class})
@Cacheable(value = true)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class TableauFrais extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "poids_num")
    private Integer poidsNum;

    @Column(name = "date_debut")
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    @Column
    private boolean activated;

    @Column(name = "libelle")
    private String libelle;
    @Enumerated(EnumType.STRING)
    @Column(name = "regle")
    private TypeRegle typeRegle;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "tableauFrais")
    @JsonIgnore
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<LigneFrais> ligneFrais = new HashSet<>();

    
    
   @Enumerated(EnumType.STRING)
    @Column(name = "source_calcul")
    private SourceCalcul sourceCalcul;
   
   
    @ManyToOne
    private TypeService typeService;

    @ManyToOne
    private Canal canal;

    @ManyToOne
    private TypeUniteOrganisation typeUoEmetteur;

    @ManyToOne
    private TypeUniteOrganisation typeUoAcquereur;

    @ManyToOne
    private Client beneficiaire;

    @ManyToOne
    private Client emetteur;

    @ManyToOne
    private UniteOrganisation organisationEm;

    @ManyToOne
    private UniteOrganisation organisationAcq;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getPoidsNum() {
        return poidsNum;
    }

    public TableauFrais poidsNum(Integer poidsNum) {
        this.poidsNum = poidsNum;
        return this;
    }

    public void setPoidsNum(Integer poidsNum) {
        this.poidsNum = poidsNum;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public TableauFrais dateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
        return this;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }

    public TableauFrais dateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
        return this;
    }

    public Set<LigneFrais> getLigneFrais() {
        return ligneFrais;
    }

    public TableauFrais ligneFrais(Set<LigneFrais> ligneFrais) {
        this.ligneFrais = ligneFrais;
        return this;
    }

    public TableauFrais addLigneFrais(LigneFrais ligneFrais) {
        this.ligneFrais.add(ligneFrais);
        ligneFrais.setTableauFrais(this);
        return this;
    }

    public TableauFrais removeLigneFrais(LigneFrais ligneFrais) {
        this.ligneFrais.remove(ligneFrais);
        ligneFrais.setTableauFrais(null);
        return this;
    }

    public void setLigneFrais(Set<LigneFrais> ligneFrais) {
        this.ligneFrais = ligneFrais;
    }

    public TypeService getTypeService() {
        return typeService;
    }

    public TableauFrais typeService(TypeService typeService) {
        this.typeService = typeService;
        return this;
    }

    public void setTypeService(TypeService typeService) {
        this.typeService = typeService;
    }

    public Canal getCanal() {
        return canal;
    }

    public TableauFrais canal(Canal canal) {
        this.canal = canal;
        return this;
    }

    public void setCanal(Canal canal) {
        this.canal = canal;
    }

    public TypeUniteOrganisation getTypeUoEmetteur() {
        return typeUoEmetteur;
    }

    public TableauFrais typeUoEmetteur(TypeUniteOrganisation typeUniteOrganisation) {
        this.typeUoEmetteur = typeUniteOrganisation;
        return this;
    }

    public void setTypeUoEmetteur(TypeUniteOrganisation typeUniteOrganisation) {
        this.typeUoEmetteur = typeUniteOrganisation;
    }

    public TypeUniteOrganisation getTypeUoAcquereur() {
        return typeUoAcquereur;
    }

    public TableauFrais typeUoAcquereur(TypeUniteOrganisation typeUniteOrganisation) {
        this.typeUoAcquereur = typeUniteOrganisation;
        return this;
    }

    public void setTypeUoAcquereur(TypeUniteOrganisation typeUniteOrganisation) {
        this.typeUoAcquereur = typeUniteOrganisation;
    }

    public Client getBeneficiaire() {
        return beneficiaire;
    }

    public TableauFrais beneficiaire(Client client) {
        this.beneficiaire = client;
        return this;
    }

    public void setBeneficiaire(Client client) {
        this.beneficiaire = client;
    }

    public Client getEmetteur() {
        return emetteur;
    }

    public TableauFrais emetteur(Client client) {
        this.emetteur = client;
        return this;
    }

    public void setEmetteur(Client client) {
        this.emetteur = client;
    }

    public UniteOrganisation getOrganisationEm() {
        return organisationEm;
    }

    public TableauFrais organisationEm(UniteOrganisation uniteOrganisation) {
        this.organisationEm = uniteOrganisation;
        return this;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public void setOrganisationEm(UniteOrganisation uniteOrganisation) {
        this.organisationEm = uniteOrganisation;
    }

    public UniteOrganisation getOrganisationAcq() {
        return organisationAcq;
    }

    public TableauFrais organisationAcq(UniteOrganisation uniteOrganisation) {
        this.organisationAcq = uniteOrganisation;
        return this;
    }

    public void setOrganisationAcq(UniteOrganisation uniteOrganisation) {
        this.organisationAcq = uniteOrganisation;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    public TypeRegle getTypeRegle() {
        return typeRegle;
    }

    public void setTypeRegle(TypeRegle typeRegle) {
        this.typeRegle = typeRegle;
    }

    public SourceCalcul getSourceCalcul() {
        return sourceCalcul;
    }

    public void setSourceCalcul(SourceCalcul sourceCalcul) {
        this.sourceCalcul = sourceCalcul;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TableauFrais tableauFrais = (TableauFrais) o;
        if (tableauFrais.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), tableauFrais.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "TableauFrais{"
                + "id=" + getId()
                + ", poidsNum=" + getPoidsNum()
                + "}";
    }
}
