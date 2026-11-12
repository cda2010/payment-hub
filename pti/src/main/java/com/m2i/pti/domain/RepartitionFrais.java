package com.m2i.pti.domain;

import com.m2i.pti.domain.enumeration.Acteur;
import com.m2i.pti.domain.enumeration.TypeTarif;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A RepartitionFrais.
 */
@Entity
@Table(name = "repartition_frais")

@Cacheable(value = true)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class RepartitionFrais extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "acteur")
    private Acteur acteur;

    @Column(name = "jhi_max")
    private Double max;

    @Column(name = "jhi_min")
    private Double min;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_tarif")
    private TypeTarif typeTarif;

    @NotNull
    @Column(name = "valeur", nullable = false)
    private Double valeur;

    @ManyToOne
    private TypeService typeService;


    @Column(name = "date_debut")
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin;


    @ManyToOne
    private UniteOrganisation fseSource;

    @ManyToOne
    private UniteOrganisation fseDest;

    private Integer poidsNum;

    @Column
    private boolean activated = false;

    @Column(name = "sur_frais", nullable = true)
    private boolean surFrais = false;



    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getMin() {
        return min;
    }

    public RepartitionFrais min(Double min) {
        this.min = min;
        return this;
    }

    public void setMin(Double min) {
        this.min = min;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public Integer getPoidsNum() {
        return poidsNum;
    }

    public void setPoidsNum(Integer poidsNum) {
        this.poidsNum = poidsNum;
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

    public Double getMax() {
        return max;
    }

    public RepartitionFrais max(Double max) {
        this.max = max;
        return this;
    }

    public void setMax(Double max) {
        this.max = max;
    }

    public TypeTarif getTypeTarif() {
        return typeTarif;
    }

    public RepartitionFrais typeTarif(TypeTarif typeTarif) {
        this.typeTarif = typeTarif;
        return this;
    }

    public void setTypeTarif(TypeTarif typeTarif) {
        this.typeTarif = typeTarif;
    }

    public Acteur getActeur() {
        return acteur;
    }

    public RepartitionFrais acteur(Acteur acteur) {
        this.acteur = acteur;
        return this;
    }

    public void setActeur(Acteur acteur) {
        this.acteur = acteur;
    }

    public Double getValeur() {
        return valeur;
    }

    public RepartitionFrais valeur(Double valeur) {
        this.valeur = valeur;
        return this;
    }

    public void setValeur(Double valeur) {
        this.valeur = valeur;
    }

    public TypeService getTypeService() {
        return typeService;
    }

    public RepartitionFrais typeService(TypeService typeService) {
        this.typeService = typeService;
        return this;
    }

    public void setTypeService(TypeService typeService) {
        this.typeService = typeService;
    }

    public UniteOrganisation getFseSource() {
        return fseSource;
    }

    public void setFseSource(UniteOrganisation fseSource) {
        this.fseSource = fseSource;
    }

    public UniteOrganisation getFseDest() {
        return fseDest;
    }

    public void setFseDest(UniteOrganisation fseDest) {
        this.fseDest = fseDest;
    }

    public boolean getSurFrais() {
        return surFrais;
    }

    public void setSurFrais(boolean surFrais) {
        this.surFrais = surFrais;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RepartitionFrais repartitionFrais = (RepartitionFrais) o;
        if (repartitionFrais.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), repartitionFrais.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "RepartitionFrais{" +
            "id=" + getId() +
            ", min='" + getMin() + "'" +
            ", max='" + getMax() + "'" +
            ", typeTarif='" + getTypeTarif() + "'" +
            ", acteur='" + getActeur() + "'" +
            ", valeur='" + getValeur() + "'" +
            "}";
    }
}
