package com.m2i.pti.domain;

import com.m2i.pti.batch.reader.LigneChargementTransDTO;
import com.m2i.pti.domain.enumeration.Etape;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * A ChargementTransfert.
 */
@NamedNativeQueries({
    @NamedNativeQuery(name = "chargementTransfert.findByChargementId",
            query = "SELECT l.id as idLigneChargement, :codeoperation as idOperation ,:compteId as compteId FROM ligne_chargement_transfert l  where l.chargement_transfert_id= :idChargement and statut_ligne!='OC' and compte_id is not null order by id",
            resultSetMapping = "Mapping.LigneChargementTransDTO", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})})

@SqlResultSetMappings(value = {
    @SqlResultSetMapping(name = "Mapping.LigneChargementTransDTO",
            classes = @ConstructorResult(targetClass = LigneChargementTransDTO.class,
                    columns = {
                        @ColumnResult(name = "idLigneChargement", type = Long.class)
                        ,
                        @ColumnResult(name = "idOperation", type = Long.class)
                        ,
                        @ColumnResult(name = "compteId", type = Long.class)

                    }))
})
@Entity
@Table(name = "chargement_transfert")
//@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class ChargementTransfert extends Chargement implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom_fichier")
    private String nomFichier;

    @Column(name = "montant_total")
    private Double montantTotal;

    @Column(name = "compte_debit")
    private String compteDebit;
    /*
    0 transfert vers compte 
    1 transfert vers cash
     */
    @Column(name = "type_transfert")
    private Integer typeTransfert;

    @Column(name = "code_pays")
    private String codePays;

    @Column(name = "motif_operation")
    private String motifOperation;

    @Column(name = "description")
    private String description;

    @Column(name = "montant_entrp")
    private Double montantEntrp;
    @Enumerated(EnumType.STRING)
    @Column(name = "etape")
    private Etape etape;

    @Column(name = "solde_compte_avant")
    private BigDecimal soldeCompteAvant;
    @Column(name = "solde_compte_apres")
    private BigDecimal soldeCompteApres;
    @Column(name = "montant_charger")
    private Double montantCharger;

    @ManyToOne
//    @JsonIgnore
    @JoinColumn(name = "job_id")
    private BatchExecution batchExecution;

    
        @Column(name = "retrait_gratuit")
    private boolean retraitGratuit;
    public Integer getTypeTransfert() {
        return typeTransfert;
    }

    public String getMotifOperation() {
        return motifOperation;
    }

    public void setMotifOperation(String motifOperation) {
        this.motifOperation = motifOperation;
    }

    public String getCodePays() {
        return codePays;
    }

    public void setCodePays(String codePays) {
        this.codePays = codePays;
    }

    public void setTypeTransfert(Integer typeTransfert) {
        this.typeTransfert = typeTransfert;
    }

    public String getCompteDebit() {
        return compteDebit;
    }

    public void setCompteDebit(String compteDebit) {
        this.compteDebit = compteDebit;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getMontantEntrp() {
        return montantEntrp;
    }

    public void setMontantEntrp(Double montantEntrp) {
        this.montantEntrp = montantEntrp;
    }

    public Etape getEtape() {
        return etape;
    }

    public void setEtape(Etape etape) {
        this.etape = etape;
    }

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNomFichier() {
        return nomFichier;
    }

    public ChargementTransfert nomFichier(String nomFichier) {
        this.nomFichier = nomFichier;
        return this;
    }

    public void setNomFichier(String nomFichier) {
        this.nomFichier = nomFichier;
    }

    public Double getMontantTotal() {
        return montantTotal;
    }

    public ChargementTransfert montantTotal(Double montantTotal) {
        this.montantTotal = montantTotal;
        return this;
    }

    public void setMontantTotal(Double montantTotal) {
        this.montantTotal = montantTotal;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    public BigDecimal getSoldeCompteAvant() {
        return soldeCompteAvant;
    }

    public void setSoldeCompteAvant(BigDecimal soldeCompteAvant) {
        this.soldeCompteAvant = soldeCompteAvant;
    }

    public BigDecimal getSoldeCompteApres() {
        return soldeCompteApres;
    }

    public void setSoldeCompteApres(BigDecimal soldeCompteApres) {
        this.soldeCompteApres = soldeCompteApres;
    }

    public Double getMontantCharger() {
        return montantCharger;
    }

    public void setMontantCharger(Double montantCharger) {
        this.montantCharger = montantCharger;
    }

    public BatchExecution getBatchExecution() {
        return batchExecution;
    }

    public void setBatchExecution(BatchExecution batchExecution) {
        this.batchExecution = batchExecution;
    }

    public boolean getRetraitGratuit() {
        return retraitGratuit;
    }

    public void setRetraitGratuit(boolean retraitGratuit) {
        this.retraitGratuit = retraitGratuit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChargementTransfert chargementTransfert = (ChargementTransfert) o;
        if (chargementTransfert.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), chargementTransfert.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "ChargementTransfert{"
                + "id=" + getId()
                + ", nomFichier='" + getNomFichier() + "'"
                + ", montantTotal='" + getMontantTotal() + "'"
                + "}";
    }
}
