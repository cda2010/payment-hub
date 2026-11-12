package com.m2i.pti.domain;

import com.m2i.pti.domain.enumeration.CategorieCompte;
import com.m2i.pti.domain.enumeration.TypeOperateur;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A RegleCommission.
 */
@Entity
@Table(name = "regle_commission")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class RegleCommission extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @NotNull
    @Column(name = "etiquette", nullable = false)
    private String etiquette;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_operateur", nullable = false)
    private TypeOperateur typeOperateur;

       @Column(name = "min")
    private BigDecimal min;

    @Column(name = "max")
    private BigDecimal max;
    
    
    @Column(name = "poids_num")
    private Integer poidsNum;

    @Column(name = "date_debut")
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    @Column
    private boolean activated = false;

    @Column(name = "valeur_compte")
    private String valeurCompte;

    @Column(name = "code_agence")
    private String codeAgence;

    @Column(name = "code_banque")
    private String codeBanque;


    @Column(name = "rib")
    private String rib;

    @Column(name = "code_fse")
    private String codeFse;

    @Enumerated(EnumType.STRING)
    @Column(name = "categorie_compte")
    private CategorieCompte categorieCompte;

    @ManyToOne
    private TypeService typeService;

    @ManyToOne
    private UniteOrganisation teneurSrc;

    @ManyToOne
    private UniteOrganisation teneurDest;

    @ManyToOne
    private UniteOrganisation uoSrc;

    @ManyToOne
    private UniteOrganisation uoDest;

    @ManyToOne
    private UniteOrganisation priseEnCharge;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getValeurCompte() {
        return valeurCompte;
    }

    public void setValeurCompte(String valeurCompte) {
        this.valeurCompte = valeurCompte;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
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

    public CategorieCompte getCategorieCompte() {
        return categorieCompte;
    }

    public void setCategorieCompte(CategorieCompte categorieCompte) {
        this.categorieCompte = categorieCompte;
    }

    public Integer getPoidsNum() {
        return poidsNum;
    }

    public void setPoidsNum(Integer poidsNum) {
        this.poidsNum = poidsNum;
    }

    public String getEtiquette() {
        return etiquette;
    }

    public RegleCommission etiquette(String etiquette) {
        this.etiquette = etiquette;
        return this;
    }

    public void setEtiquette(String etiquette) {
        this.etiquette = etiquette;
    }

    public TypeOperateur getTypeOperateur() {
        return typeOperateur;
    }

    public RegleCommission typeOperateur(TypeOperateur typeOperateur) {
        this.typeOperateur = typeOperateur;
        return this;
    }

    public void setTypeOperateur(TypeOperateur typeOperateur) {
        this.typeOperateur = typeOperateur;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public RegleCommission dateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
        return this;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public RegleCommission dateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
        return this;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }

    public TypeService getTypeService() {
        return typeService;
    }

    public RegleCommission typeService(TypeService typeService) {
        this.typeService = typeService;
        return this;
    }

    public UniteOrganisation getPriseEnCharge() {
        return priseEnCharge;
    }

    public void setPriseEnCharge(UniteOrganisation priseEnCharge) {
        this.priseEnCharge = priseEnCharge;
    }

    public void setTypeService(TypeService typeService) {
        this.typeService = typeService;
    }

    public UniteOrganisation getTeneurSrc() {
        return teneurSrc;
    }

    public RegleCommission teneurSrc(UniteOrganisation uniteOrganisation) {
        this.teneurSrc = uniteOrganisation;
        return this;
    }

    public void setTeneurSrc(UniteOrganisation uniteOrganisation) {
        this.teneurSrc = uniteOrganisation;
    }

    public UniteOrganisation getTeneurDest() {
        return teneurDest;
    }

    public RegleCommission teneurDest(UniteOrganisation uniteOrganisation) {
        this.teneurDest = uniteOrganisation;
        return this;
    }

    public void setTeneurDest(UniteOrganisation uniteOrganisation) {
        this.teneurDest = uniteOrganisation;
    }

    public UniteOrganisation getUoSrc() {
        return uoSrc;
    }

    public void setUoSrc(UniteOrganisation uoSrc) {
        this.uoSrc = uoSrc;
    }

    public UniteOrganisation getUoDest() {
        return uoDest;
    }

    public void setUoDest(UniteOrganisation uoDest) {
        this.uoDest = uoDest;
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

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove
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
        RegleCommission regleCommission = (RegleCommission) o;
        if (regleCommission.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), regleCommission.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "RegleCommission{" +
                "id=" + id +
                ", etiquette='" + etiquette + '\'' +
                ", typeOperateur=" + typeOperateur +
                ", dateDebut=" + dateDebut +
                ", dateFin=" + dateFin +
                ", typeService=" + typeService +
                ", teneurSrc=" + teneurSrc +
                ", teneurDest=" + teneurDest +
                ", uoSrc=" + uoSrc +
                ", uoDest=" + uoDest +
                '}';
    }
}
