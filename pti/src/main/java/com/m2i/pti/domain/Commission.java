package com.m2i.pti.domain;

import com.m2i.pti.domain.enumeration.Acteur;
import com.m2i.pti.domain.enumeration.EtatValidite;
import com.m2i.pti.service.dto.CommissionDTO;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Objects;
@NamedNativeQueries({
    @NamedNativeQuery(name = "Commission.findCommissionNonPaye",
            query = "select id,montant, operation_id,organisation_id, etat , est_comptabilise ,est_paye, est_regle from commission where est_paye=false and est_comptabilise=false and etat='VALIDE' and montant >0",
            resultSetMapping = "Mapping.CommissionDTO",hints={@QueryHint(name="org.hibernate.cacheable",value="false")}),
    
     @NamedNativeQuery(name = "Commission.findCommissionNonPayeOrganisation",
            query = "select id,montant, operation_id,organisation_id, etat , est_comptabilise ,est_paye, est_regle from commission where est_paye=false and est_comptabilise=false and etat='VALIDE' and montant >0  and organisation_id= CAST(CAST (:uoId AS TEXT) AS BIGINT )",
            resultSetMapping = "Mapping.CommissionDTO",hints={@QueryHint(name="org.hibernate.cacheable",value="false")}),
    
    
     @NamedNativeQuery(name = "Commission.findLigneReglement",
            query = "select id,montant, operation_id,organisation_id, etat , est_comptabilise ,est_paye, est_regle from commission where ligne_reglement_id=CAST(CAST (:ligne AS TEXT) AS BIGINT )",
            resultSetMapping = "Mapping.CommissionDTO",hints={@QueryHint(name="org.hibernate.cacheable",value="false")}),

    @NamedNativeQuery(name = "Commission.findAllByCriteria",
                query = "SELECT c.id, c.montant, c.est_emetteur as estEmetteur, c.est_paye as estPaye," +
                        "c.est_regle as estRegle, c.est_comptabilise as estComptabilise, c.date_paiement as datePaiement," +
                        "c.date_enregistrement as dateEnregistrement, c.operation_id as operationId,op.code_operation as operationCodeOperation," +
                        "c.organisation_id as organisationId, uo.libelle_unite_organisation as organisationLibelleUniteOrganisation," +
                        "c.acteur, c.etat, ts.code as typeService, op.montant_op as montantOp" +
                        " FROM commission c" +
                        " INNER JOIN operation op on c.operation_id=op.id" +
                        " INNER JOIN unite_organisation uo on c.organisation_id=uo.id" +
                        " INNER JOIN type_service ts on op.type_service_id=ts.id" +
                        " Where (" +
                        "(:estPaye is null or c.est_paye= CAST(CAST (:estPaye AS TEXT) AS boolean ))" +
                        "and (:uoId is null or c.organisation_id= CAST(CAST (:uoId AS TEXT) AS BIGINT ) )" +
                        "and (CAST(CAST(:dateEnregistrement as text) as timestamp) is null or c.date_enregistrement BETWEEN CAST(CAST(:dateEnregistrement AS text) AS timestamp ) and CAST(CAST(:dateEnregistrement2 AS text) AS timestamp ))) order by c.id desc limit :taille offset :page ",
                resultSetMapping = "Mapping.CommissionDTOS",hints={@QueryHint(name="org.hibernate.cacheable",value="false")}),


     @NamedNativeQuery(name = "Commission.findAllByUoIdAndEstPayer",
                query = "SELECT c.id, c.montant, c.est_emetteur as estEmetteur, c.est_paye as estPaye," +
                        "c.est_regle as estRegle, c.est_comptabilise as estComptabilise, c.date_paiement as datePaiement," +
                        "c.date_enregistrement as dateEnregistrement, c.operation_id as operationId,op.code_operation as operationCodeOperation," +
                        "c.organisation_id as organisationId, uo.libelle_unite_organisation as organisationLibelleUniteOrganisation," +
                        "c.acteur, c.etat, ts.code as typeService, op.montant_op as montantOp" +
                        " FROM commission c" +
                        " INNER JOIN operation op on c.operation_id=op.id" +
                        " INNER JOIN unite_organisation uo on c.organisation_id=uo.id" +
                        " INNER JOIN type_service ts on op.type_service_id=ts.id" +
                        " Where (" +
                        "(:estPaye is null or c.est_paye= CAST(CAST (:estPaye AS TEXT) AS boolean ))" +
                        "and (:uoId is null or c.organisation_id= CAST(CAST (:uoId AS TEXT) AS BIGINT ) ))" +
                        "order by c.id desc  ",
                resultSetMapping = "Mapping.CommissionDTOS",hints={@QueryHint(name="org.hibernate.cacheable",value="false")}),
     
     @NamedNativeQuery(name = "Commission.findListCommissionOfUoBeforeDateReglement",
                query = "SELECT c.id, c.montant, c.est_emetteur as estEmetteur, c.est_paye as estPaye," +
                        "c.est_regle as estRegle, c.est_comptabilise as estComptabilise,c.en_traitement as enTraitement, c.date_paiement as datePaiement," +
                        "c.date_enregistrement as dateEnregistrement, c.operation_id as operationId,op.code_operation as operationCodeOperation," +
                        "c.organisation_id as organisationId, uo.libelle_unite_organisation as organisationLibelleUniteOrganisation," +
                        "c.acteur, c.etat, ts.code as typeService, op.montant_op as montantOp,c.ligne_reglement_id as ligneReglementId" +
                        " FROM commission c" +
                        " INNER JOIN operation op on c.operation_id=op.id" +
                        " INNER JOIN unite_organisation uo on c.organisation_id=uo.id" +
                        " INNER JOIN type_service ts on op.type_service_id=ts.id" +
                        " Where (" +
                        "(c.montant >0 and  c.ligne_reglement_id  is null and est_paye=true and (c.est_regle is null or c.est_regle=false) and c.date_enregistrement <= CAST(CAST (:dateEnregistrement AS TEXT) AS timestamp ))" +
                        "and (c.organisation_id= CAST(CAST (:UoId AS TEXT) AS BIGINT ) ))" +
                        "order  by c.montant asc ",
                resultSetMapping = "Mapping.CommissionDTOSNew",hints={@QueryHint(name="org.hibernate.cacheable",value="false")})


})

@SqlResultSetMappings({
        @SqlResultSetMapping(name = "Mapping.CommissionDTO",
                classes = @ConstructorResult(targetClass = CommissionDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = Long.class),
                                @ColumnResult(name = "montant", type = BigDecimal.class),
                                @ColumnResult(name = "operation_id", type = Long.class),
                                @ColumnResult(name = "organisation_id", type = Long.class),
                                @ColumnResult(name = "etat", type = String.class),
                                @ColumnResult(name = "est_comptabilise", type = Boolean.class),
                                @ColumnResult(name = "est_paye", type = Boolean.class),
                                @ColumnResult(name = "est_regle", type = Boolean.class),
                        })),
        @SqlResultSetMapping(name = "Mapping.CommissionDTOS",
                classes = @ConstructorResult(targetClass = CommissionDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = Long.class),
                                @ColumnResult(name = "montant", type = BigDecimal.class),
                                @ColumnResult(name = "estEmetteur", type = Boolean.class),
                                @ColumnResult(name = "estPaye", type = Boolean.class),
                                @ColumnResult(name = "estRegle", type = Boolean.class),
                                @ColumnResult(name = "estComptabilise", type = Boolean.class),
                                @ColumnResult(name = "datePaiement", type = ZonedDateTime.class),
                                @ColumnResult(name = "dateEnregistrement", type = ZonedDateTime.class),
                                @ColumnResult(name = "operationId", type = Long.class),
                                @ColumnResult(name = "operationCodeOperation", type = String.class),
                                @ColumnResult(name = "organisationId", type = Long.class),
                                @ColumnResult(name = "organisationLibelleUniteOrganisation", type = String.class),
                                @ColumnResult(name = "acteur", type = String.class),
                                @ColumnResult(name = "etat", type = String.class),
                                @ColumnResult(name = "typeService", type = String.class),
                                @ColumnResult(name = "montantOp", type = BigDecimal.class),
                        })),
        @SqlResultSetMapping(name = "Mapping.CommissionDTOSNew",
                classes = @ConstructorResult(targetClass = CommissionDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = Long.class),
                                @ColumnResult(name = "montant", type = BigDecimal.class),
                                @ColumnResult(name = "estEmetteur", type = Boolean.class),
                                @ColumnResult(name = "estPaye", type = Boolean.class),
                                @ColumnResult(name = "estRegle", type = Boolean.class),
                                @ColumnResult(name = "estComptabilise", type = Boolean.class),
                                @ColumnResult(name = "enTraitement", type = Boolean.class),
                                @ColumnResult(name = "datePaiement", type = ZonedDateTime.class),
                                @ColumnResult(name = "dateEnregistrement", type = ZonedDateTime.class),
                                @ColumnResult(name = "operationId", type = Long.class),
                                @ColumnResult(name = "operationCodeOperation", type = String.class),
                                @ColumnResult(name = "organisationId", type = Long.class),
                                @ColumnResult(name = "organisationLibelleUniteOrganisation", type = String.class),
                                @ColumnResult(name = "acteur", type = String.class),
                                @ColumnResult(name = "etat", type = String.class),
                                @ColumnResult(name = "typeService", type = String.class),
                                @ColumnResult(name = "montantOp", type = BigDecimal.class),
                                @ColumnResult(name = "ligneReglementId", type = Long.class),
                                
                        })),


})

/**
 * A Commission.
 */
@Entity
@Table(name = "commission")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Commission extends AbstractAuditingEntity  implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "montant", precision = 10, scale = 2)
    private BigDecimal montant;
    @Column(name = "taxe", precision = 10, scale = 2)
    private BigDecimal taxe;
    @Column(name = "est_paye")
    private Boolean estPaye;

      @Column(name = "est_regle")
    private Boolean estRegle;
    
    
    @Column(name = "est_emetteur")
    private Boolean estEmetteur;

    @Enumerated(EnumType.STRING)
    @Column(name = "acteur")
    private Acteur acteur;

    @Enumerated(EnumType.STRING)
    @Column(name = "etat")
    private EtatValidite etat;


    @Column(name = "date_paiement")
    private ZonedDateTime datePaiement;

    @Column(name = "date_enregistrement")
    private ZonedDateTime dateEnregistrement;

    @ManyToOne
    private Operation operation;
    
     @ManyToOne
    private LigneReglement ligneReglement;
    
    

    @ManyToOne
    private UniteOrganisation organisation;

    @Column(name = "est_comptabilise")
    private Boolean estComptabilise;

        @Column(name = "en_traitement")
    private Boolean enTraitement;
@Column(name = "reste_a_paye", precision = 10, scale = 2)
    private BigDecimal resteApayer;
    public BigDecimal getTaxe() {
        return taxe;
    }

    public void setTaxe(BigDecimal taxe) {
        this.taxe = taxe;
    }
    

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public Commission montant(BigDecimal montant) {
        this.montant = montant;
        return this;
    }
     public Commission taxe(BigDecimal taxe) {
        this.taxe = taxe;
        return this;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }

    public Boolean isEstPaye() {
        return estPaye;
    }

    public Commission estPaye(Boolean estPaye) {
        this.estPaye = estPaye;
        return this;
    }

    public void setEstPaye(Boolean estPaye) {
        this.estPaye = estPaye;
    }

    public Acteur getActeur() {
        return acteur;
    }

    public void setActeur(Acteur acteur) {
        this.acteur = acteur;
    }

    public Commission acteur(Acteur acteur) {
        this.acteur = acteur;
        return this;
    }

    public ZonedDateTime getDatePaiement() {
        return datePaiement;
    }

    public Commission datePaiement(ZonedDateTime datePaiement) {
        this.datePaiement = datePaiement;
        return this;
    }

    public void setDatePaiement(ZonedDateTime datePaiement) {
        this.datePaiement = datePaiement;
    }

    public ZonedDateTime getDateEnregistrement() {
        return dateEnregistrement;
    }

    public Commission dateEnregistrement(ZonedDateTime dateEnregistrement) {
        this.dateEnregistrement = dateEnregistrement;
        return this;
    }

    public void setDateEnregistrement(ZonedDateTime dateEnregistrement) {
        this.dateEnregistrement = dateEnregistrement;
    }

    public Operation getOperation() {
        return operation;
    }

    public Commission operation(Operation operation) {
        this.operation = operation;
        return this;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public UniteOrganisation getOrganisation() {
        return organisation;
    }

    public Commission organisation(UniteOrganisation uniteOrganisation) {
        this.organisation = uniteOrganisation;
        return this;
    }

    public Boolean getEstPaye() {
        return estPaye;
    }

    public Boolean getEstEmetteur() {
        return estEmetteur;
    }

    public void setEstEmetteur(Boolean estEmetteur) {
        this.estEmetteur = estEmetteur;
    }

    public Commission estEmetteur(Boolean estEmetteur) {
        this.estEmetteur = estEmetteur;
        return this;
    }

    public Boolean getEstComptabilise() {
        return estComptabilise;
    }

    public void setEstComptabilise(Boolean estComptabilise) {
        this.estComptabilise = estComptabilise;
    }

    public Commission estComptabilise(Boolean estComptabilise) {
        this.estComptabilise = estComptabilise;
        return this;
    }

    public EtatValidite getEtat() {
        return etat;
    }

    public void setEtat(EtatValidite etat) {
        this.etat = etat;
    }

    public Commission etat(EtatValidite etat) {
        this.etat = etat;
        return this;
    }

    public void setOrganisation(UniteOrganisation uniteOrganisation) {
        this.organisation = uniteOrganisation;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    public Boolean getEstRegle() {
        return estRegle;
    }

    public void setEstRegle(Boolean estRegle) {
        this.estRegle = estRegle;
    }

    public LigneReglement getLigneReglement() {
        return ligneReglement;
    }

    public void setLigneReglement(LigneReglement ligneReglement) {
        this.ligneReglement = ligneReglement;
    }

    public BigDecimal getResteApayer() {
        return resteApayer;
    }

    public void setResteApayer(BigDecimal resteApayer) {
        this.resteApayer = resteApayer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Commission commission = (Commission) o;
        if (commission.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), commission.getId());
    }

    public Boolean getEnTraitement() {
        return enTraitement;
    }

    public void setEnTraitement(Boolean enTraitement) {
        this.enTraitement = enTraitement;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "Commission{" +
                "id=" + getId() +
                ", montant=" + getMontant() +
                ", estPaye='" + isEstPaye() + "'" +
                ", datePaiement='" + getDatePaiement() + "'" +
                ", dateEnregistrement='" + getDateEnregistrement() + "'" +
                "}";
    }
}
