package com.m2i.pti.domain;

import com.m2i.pti.config.audit.EntityAuditEventListener;
import com.m2i.pti.domain.enumeration.Etape;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

import com.m2i.pti.service.dto.FactureDTO;

/**
 * A Facture.
 */



@SqlResultSetMappings(value = {
    @SqlResultSetMapping(name = "Mapping.FactureDTO",
        classes = @ConstructorResult(targetClass = FactureDTO.class,
            columns = {
                @ColumnResult(name = "id", type = Long.class),
                @ColumnResult(name = "datePaiement", type = String.class),
                @ColumnResult(name = "montantPaiement", type = String.class),
                @ColumnResult(name = "referenceFacature", type = String.class),
                @ColumnResult(name = "referencePaiement", type = String.class),
                @ColumnResult(name = "produit", type = String.class),
                @ColumnResult(name = "strExtraData", type = String.class)
              
            }
        )
    ),
    @SqlResultSetMapping(name = "Mapping.FactureOpDTO",
                classes = @ConstructorResult(targetClass = FactureDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = Long.class),
                                @ColumnResult(name = "datePaiement", type = String.class),
                                @ColumnResult(name = "montantPaiement", type = String.class),
                                @ColumnResult(name = "referenceFacature", type = String.class),
                                @ColumnResult(name = "referencePaiement", type = String.class),
                                @ColumnResult(name = "referenceExt", type = String.class),
                                @ColumnResult(name = "transactionIdExt", type = String.class),
                                @ColumnResult(name = "produit", type = String.class),
                                @ColumnResult(name = "operationId", type = Long.class),
                                @ColumnResult(name = "strExtraData", type = String.class),
                                @ColumnResult(name = "extraData", type = String.class),
                        }
                )
        ),
})

@NamedNativeQueries({
    @NamedNativeQuery(name = "Facture.findFactureOptimise",
        query = "SELECT f.id, f.date_paiement datePaiement, f.montant_facture montantPaiement, f.reference_facature referenceFacature,"+
        "f.reference_paiement referencePaiement,f.reference_ext as referenceExt,f.transact_id_ext as transactionIdExt, p.nom_produit produit,f.operation_id as operationId , str_extra_data as strExtraData,extra_data as extraData FROM facture f , produit p "+
        "where ("+
        "f.produit_id = p.id "+
        "and (:produit is null or f.produit_id= CAST(CAST(:produit as text) as BIGINT))"+
        "and (:refFacture is null or :refFacture='' or f.reference_facature=CAST(:refFacture as text))"+
        "and (:datePaiement is null or :datePaiement='' or f.date_paiement like CAST(:datePaiement as text)||'%')"+
        "and (:refPaiement is null or :refPaiement='' or f.reference_paiement=CAST(:refPaiement as text))"+
        ")  order by f.id desc",
        resultSetMapping = "Mapping.FactureOpDTO"),

    @NamedNativeQuery(name = "Facture.findFactureOfProduitOptimise",
                query = "SELECT f.id, f.date_paiement datePaiement, f.montant_facture montantPaiement, f.reference_facature referenceFacature,"+
                        "f.reference_paiement referencePaiement,f.reference_ext as referenceExt,f.transact_id_ext as transactionIdExt, p.nom_produit produit,f.operation_id as operationId,str_extra_data as strExtraData,extra_data as extraData  FROM facture f , produit p "+
                        "where ("+
                        "f.produit_id = p.id "+
                        "and (:produit is null or p.facturier_id= CAST(CAST(:produit as text) as BIGINT))"+
                        "  )order by f.id desc",
                resultSetMapping = "Mapping.FactureOpDTO"),

    @NamedNativeQuery(name = "Facture.findFactureByProduitIdOptimise",
                query = "SELECT f.id, f.date_paiement datePaiement, f.montant_facture montantPaiement, f.reference_facature referenceFacature,"+
                        "f.reference_paiement referencePaiement,f.reference_ext as referenceExt,f.transact_id_ext as transactionIdExt, p.nom_produit produit,f.operation_id as operationId,str_extra_data as strExtraData,extra_data as extraData  FROM facture f , produit p "+
                        "where ("+
                        "f.produit_id = p.id "+
                        "and (:produit is null or f.produit_id= CAST(CAST(:produit as text) as BIGINT))"+
                        "  )order by f.id desc",
                resultSetMapping = "Mapping.FactureOpDTO"),
    
     @NamedNativeQuery(name = "Facture.findFactureByFacturierIdAndPersonne",
                query = "SELECT f.id, f.date_paiement datePaiement, f.montant_facture montantPaiement, f.reference_facature referenceFacature,"+
                        "f.reference_paiement referencePaiement,f.reference_ext as referenceExt,f.transact_id_ext as transactionIdExt, p.nom_produit produit,f.operation_id as operationId,str_extra_data as strExtraData,f.extra_data as extraData  FROM facture f , produit p,  operation op, personne pers,emetteur em  "+
                        "where (op.id=f.operation_id and pers.id=em.personne_id and " +
                        " op.emetteur_id=em.id  and pers.id=:persId  and p.facturier_id=:facturierId and "+
                        " f.produit_id = p.id "+
                        "  )order by f.id desc limit :taille offset :page",
                resultSetMapping = "Mapping.FactureOpDTO")
})

@Entity
@Table(name = "facture")
@EntityListeners(value = {EntityAuditEventListener.class})
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Facture extends AbstractAuditingEntity  implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reference_facature")
    private String referenceFacature;

    @Column(name = "montant_facture")
    private String montantFacture;

    @Column(name = "reference_paiement")
    private String referencePaiement;

    @Column(name = "date_paiement")
    private String datePaiement;
 @Column(name = "extra_data")
    private String extraData;
 @Column(name = "transact_id_ext")
    private String transactionIdExt;
 @Column(name = "message_retour")
    private String messageRet;
 @Column(name = "reference_ext")
    private String referenceExt;
 @Column(name = "str_extra_data")
    private String strExtraData;
    @ManyToOne
    private Produit produit;

    @ManyToOne
    private Operation operation;

  @Enumerated(EnumType.STRING)
    @Column(name = "etape")
    private Etape etape;
    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReferenceFacature() {
        return referenceFacature;
    }

    public Facture referenceFacature(String referenceFacature) {
        this.referenceFacature = referenceFacature;
        return this;
    }

    public void setReferenceFacature(String referenceFacature) {
        this.referenceFacature = referenceFacature;
    }

    public String getMontantFacture() {
        return montantFacture;
    }

    public Facture montantFacture(String montantFacture) {
        this.montantFacture = montantFacture;
        return this;
    }

    public void setMontantFacture(String montantFacture) {
        this.montantFacture = montantFacture;
    }

    public String getReferencePaiement() {
        return referencePaiement;
    }

    public Facture referencePaiement(String referencePaiement) {
        this.referencePaiement = referencePaiement;
        return this;
    }

    public void setReferencePaiement(String referencePaiement) {
        this.referencePaiement = referencePaiement;
    }

    public String getDatePaiement() {
        return datePaiement;
    }

    public Facture datePaiement(String datePaiement) {
        this.datePaiement = datePaiement;
        return this;
    }

    public void setDatePaiement(String datePaiement) {
        this.datePaiement = datePaiement;
    }

    public Produit getProduit() {
        return produit;
    }
    public void setProduit(Produit produit) {
        this.produit = produit;
    }

    public Operation getOperation() {
        return operation;
    }
    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public String getExtraData() {
        return extraData;
    }

    public void setExtraData(String extraData) {
        this.extraData = extraData;
    }

    public String getTransactionIdExt() {
        return transactionIdExt;
    }

    public void setTransactionIdExt(String transactionIdExt) {
        this.transactionIdExt = transactionIdExt;
    }

    public String getMessageRet() {
        return messageRet;
    }

    public void setMessageRet(String messageRet) {
        this.messageRet = messageRet;
    }

    public String getReferenceExt() {
        return referenceExt;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove
    public void setReferenceExt(String referenceExt) {
        this.referenceExt = referenceExt;
    }

    public String getStrExtraData() {
        return strExtraData;
    }

    public void setStrExtraData(String strExtraData) {
        this.strExtraData = strExtraData;
    }

    public Etape getEtape() {
        return etape;
    }

    public void setEtape(Etape etape) {
        this.etape = etape;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Facture facture = (Facture) o;
        if (facture.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), facture.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "Facture{" +
            "id=" + getId() +
            ", referenceFacature='" + getReferenceFacature() + "'" +
            ", montantFacture='" + getMontantFacture() + "'" +
            ", referencePaiement='" + getReferencePaiement() + "'" +
            ", datePaiement='" + getDatePaiement() + "'" +
            "}";
    }
}
