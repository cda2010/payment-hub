package com.m2i.pti.domain;

import com.m2i.pti.config.audit.EntityAuditEventListener;
import com.m2i.pti.domain.enumeration.Action;
import com.m2i.pti.domain.enumeration.Operateur;
import com.m2i.pti.domain.enumeration.Temps;
import com.m2i.pti.domain.enumeration.TypeRegle;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A TableauCumul.
 */
@Entity
@Table(name = "tableau_cumul")
@EntityListeners(value = {EntityAuditEventListener.class})
@Cacheable(value = true)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class TableauCumul extends AbstractAuditingEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "poids_num")
    private Integer poidsNum;

    @Column(name = "code_regle")
    private String codeRegle;

    @Enumerated(EnumType.STRING)
    @Column(name = "temps")
    private Temps temps;

    @Column(name = "nombre")
    private Integer nombre;

    @Column(name = "montant", precision=10, scale=2)
    private BigDecimal montant;

    @Enumerated(EnumType.STRING)
    @Column(name = "action")
    private Action action;

    @Column(name = "date_debut")
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    @Column
    private boolean activated = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_regle")
    private TypeRegle typeRegle;

    @Enumerated(EnumType.STRING)
    @Column(name = "operateur")
    private Operateur operateur;

    @ManyToOne
    private TypeService typeService;

    @ManyToOne
    private Canal canal;

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

    public TableauCumul poidsNum(Integer poidsNum) {
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

    public String getCodeRegle() {
        return codeRegle;
    }

    public TableauCumul codeRegle(String codeRegle) {
        this.codeRegle = codeRegle;
        return this;
    }

    public void setCodeRegle(String codeRegle) {
        this.codeRegle = codeRegle;
    }

    public Temps getTemps() {
        return temps;
    }

    public TableauCumul temps(Temps temps) {
        this.temps = temps;
        return this;
    }

    public void setTemps(Temps temps) {
        this.temps = temps;
    }

    public Integer getNombre() {
        return nombre;
    }

    public TableauCumul nombre(Integer nombre) {
        this.nombre = nombre;
        return this;
    }

    public void setNombre(Integer nombre) {
        this.nombre = nombre;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public TableauCumul montant(BigDecimal montant) {
        this.montant = montant;
        return this;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }

    public Action getAction() {
        return action;
    }

    public TableauCumul action(Action action) {
        this.action = action;
        return this;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public TableauCumul dateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
        return this;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public TableauCumul dateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
        return this;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }

    public TypeRegle getTypeRegle() {
        return typeRegle;
    }

    public TableauCumul typeRegle(TypeRegle typeRegle) {
        this.typeRegle = typeRegle;
        return this;
    }

    public void setTypeRegle(TypeRegle typeRegle) {
        this.typeRegle = typeRegle;
    }

    public Operateur getOperateur() {
        return operateur;
    }

    public TableauCumul operateur(Operateur operateur) {
        this.operateur = operateur;
        return this;
    }

    public void setOperateur(Operateur operateur) {
        this.operateur = operateur;
    }

    public TypeService getTypeService() {
        return typeService;
    }

    public TableauCumul typeService(TypeService typeService) {
        this.typeService = typeService;
        return this;
    }

    public void setTypeService(TypeService typeService) {
        this.typeService = typeService;
    }

    public Canal getCanal() {
        return canal;
    }

    public TableauCumul canal(Canal canal) {
        this.canal = canal;
        return this;
    }

    public void setCanal(Canal canal) {
        this.canal = canal;
    }

    public Client getBeneficiaire() {
        return beneficiaire;
    }

    public TableauCumul beneficiaire(Client client) {
        this.beneficiaire = client;
        return this;
    }

    public void setBeneficiaire(Client client) {
        this.beneficiaire = client;
    }

    public Client getEmetteur() {
        return emetteur;
    }

    public TableauCumul emetteur(Client client) {
        this.emetteur = client;
        return this;
    }

    public void setEmetteur(Client client) {
        this.emetteur = client;
    }

    public UniteOrganisation getOrganisationEm() {
        return organisationEm;
    }

    public TableauCumul organisationEm(UniteOrganisation uniteOrganisation) {
        this.organisationEm = uniteOrganisation;
        return this;
    }

    public void setOrganisationEm(UniteOrganisation uniteOrganisation) {
        this.organisationEm = uniteOrganisation;
    }

    public UniteOrganisation getOrganisationAcq() {
        return organisationAcq;
    }

    public TableauCumul organisationAcq(UniteOrganisation uniteOrganisation) {
        this.organisationAcq = uniteOrganisation;
        return this;
    }

    public void setOrganisationAcq(UniteOrganisation uniteOrganisation) {
        this.organisationAcq = uniteOrganisation;
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
        TableauCumul tableauCumul = (TableauCumul) o;
        if (tableauCumul.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), tableauCumul.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "TableauCumul{" +
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
