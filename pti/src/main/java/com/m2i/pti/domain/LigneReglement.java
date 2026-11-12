package com.m2i.pti.domain;

import com.m2i.pti.service.LigneReglementDTO;
import com.m2i.pti.service.dto.ClientDTO;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A LigneReglement.
 */

@SqlResultSetMappings(value = {
        @SqlResultSetMapping(name = "Mapping.LigneReglementDTO",
                classes = @ConstructorResult(targetClass = LigneReglementDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = Long.class)
                                ,
                                @ColumnResult(name = "montantCommission", type = Double.class)
                                ,
                                @ColumnResult(name = "soldeCompteOp", type = Double.class)
                                ,
                                @ColumnResult(name = "valide", type = Boolean.class)
                                ,
                                @ColumnResult(name = "reglementId", type = Long.class)
                                ,
                                @ColumnResult(name = "operationId", type = Long.class),
                                @ColumnResult(name = "reglementStatut", type = Boolean.class),
                                @ColumnResult(name = "soldeCompteOpApres", type = Double.class),
                                @ColumnResult(name = "estDetacher", type = Boolean.class),

                        }))
})

@NamedNativeQueries({
        @NamedNativeQuery(name = "ligneReglement.findByIdReglement",
                query = "SELECT l.id, montant_commission as montantCommission, solde_compte_op as soldeCompteOp, valide,reglement_id as reglementId, operation_id as operationId,r.statut as reglementStatut, solde_compte_op_apres as soldeCompteOpApres,est_detacher as estDetacher" +
                        " FROM ligne_reglement l inner join reglement r on l.reglement_id=r.id  where  reglement_id= :idReglement",
                resultSetMapping = "Mapping.LigneReglementDTO", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})
})

@Entity
@Table(name = "ligne_reglement")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class LigneReglement extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "montant_commission")
    private Double montantCommission;

    @Column(name = "solde_compte_op")
    private Double soldeCompteOp;
     @Column(name = "solde_compte_com")
    private Double soldeCompteComm;
      @Column(name = "solde_compte_op_apres")
    private Double soldeCompteOpApres;
      
    @Column(name="erreur")
    private String erreur;

    @ManyToOne
    private Reglement reglement;

    @ManyToOne
    private UniteOrganisation organisation;

    
    @ManyToOne
    private Operation operation;
       
    private Boolean valide;

    @Column(name="est_detacher")
    private Boolean estDetacher= false;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getMontantCommission() {
        return montantCommission;
    }

    public LigneReglement montantCommission(Double montantCommission) {
        this.montantCommission = montantCommission;
        return this;
    }

    public void setMontantCommission(Double montantCommission) {
        this.montantCommission = montantCommission;
    }

    public Double getSoldeCompteOp() {
        return soldeCompteOp;
    }

    public LigneReglement soldeCompteOp(Double soldeCompteOp) {
        this.soldeCompteOp = soldeCompteOp;
        return this;
    }

    public void setSoldeCompteOp(Double soldeCompteOp) {
        this.soldeCompteOp = soldeCompteOp;
    }

    public Reglement getReglement() {
        return reglement;
    }

    public LigneReglement reglement(Reglement reglement) {
        this.reglement = reglement;
        return this;
    }

    public void setReglement(Reglement reglement) {
        this.reglement = reglement;
    }

    public UniteOrganisation getOrganisation() {
        return organisation;
    }

    public LigneReglement organisation(UniteOrganisation uniteOrganisation) {
        this.organisation = uniteOrganisation;
        return this;
    }

    public void setOrganisation(UniteOrganisation uniteOrganisation) {
        this.organisation = uniteOrganisation;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public Boolean getValide() {
        return valide;
    }

    public void setValide(Boolean valide) {
        this.valide = valide;
    }

    public String getErreur() {
        return erreur;
    }

    public void setErreur(String erreur) {
        this.erreur = erreur;
    }

    public Double getSoldeCompteOpApres() {
        return soldeCompteOpApres;
    }

    public void setSoldeCompteOpApres(Double soldeCompteOpApres) {
        this.soldeCompteOpApres = soldeCompteOpApres;
    }

    public Boolean getEstDetacher() {
        return estDetacher;
    }

    public void setEstDetacher(Boolean estDetacher) {
        this.estDetacher = estDetacher;
    }

    public Double getSoldeCompteComm() {
        return soldeCompteComm;
    }

    public void setSoldeCompteComm(Double soldeCompteComm) {
        this.soldeCompteComm = soldeCompteComm;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LigneReglement ligneReglement = (LigneReglement) o;
        if (ligneReglement.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), ligneReglement.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "LigneReglement{" +
            "id=" + getId() +
            ", montantCommission='" + getMontantCommission() + "'" +
            ", soldeCompteOp='" + getSoldeCompteOp() + "'" +
                ",valide='"+ getValide()+"'"+
            "}";
    }
}
