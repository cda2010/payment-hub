package com.m2i.pti.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;
import  com.m2i.pti.service.dto.ProduitDTO;
/**
 * A Produit.
 */

@NamedNativeQueries({
    @NamedNativeQuery(name = "Produit.findByProduitByFacturier",
            query = "select prod.id, nom_produit nomProduit, numero_ordre numeroOrdre, actif actif ,transactionnel  transactionnel,"
                    + " a_un_montant aUnMontant, montant montant from produit prod where "
            + "  prod.facturier_id=:idFacturier order by numero_ordre asc",
            resultSetMapping = "Mapping.ProduitDTO", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})
})
@SqlResultSetMapping(name = "Mapping.ProduitDTO", 
        classes = @ConstructorResult(targetClass = ProduitDTO.class,
                columns = {
                    @ColumnResult(name = "id", type = Long.class)
                    ,
                    @ColumnResult(name = "nomProduit", type = String.class)
                    ,
                    @ColumnResult(name = "numeroOrdre", type = Integer.class)
                    ,
                    @ColumnResult(name = "actif", type = boolean.class)
                    ,
                    @ColumnResult(name = "transactionnel", type = boolean.class)
                        ,
                    @ColumnResult(name = "aUnMontant", type = boolean.class)
                           ,
                    @ColumnResult(name = "montant", type = double.class)
                  
                }))
@Entity
@Table(name = "produit")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Cacheable(value = true)
public class Produit extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom_produit")
    private String nomProduit;

    @Column(name = "numero_ordre")
    private Integer numeroOrdre;

    @Column(name = "actif")
    private boolean actif;
    @Column(name = "transactionnel", columnDefinition = "boolean default true")
    private boolean transactionnel = true;

    
     @Column(name = "noScreen", columnDefinition = "boolean default false")
    private boolean noScreen = false;
     
    @Column(name = "a_un_montant")
    private boolean aUnMontant;

    @Column(name = "montant", nullable = true)
    private double montant;

    @ManyToOne
    // @JsonIgnore
    private Facturier facturier;

    @OneToMany(mappedBy = "produit")
    @JsonIgnore
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<Ecran> ecrans = new HashSet<>();
    
    @ManyToOne
    private TypeService serviceAssocie;

    
    
    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNomProduit() {
        return nomProduit;
    }

    public Produit nomProduit(String nomProduit) {
        this.nomProduit = nomProduit;
        return this;
    }

    public void setNomProduit(String nomProduit) {
        this.nomProduit = nomProduit;
    }

    public Integer getNumeroOrdre() {
        return numeroOrdre;
    }

    public Produit numeroOrdre(Integer numeroOrdre) {
        this.numeroOrdre = numeroOrdre;
        return this;
    }

    public void setNumeroOrdre(Integer numeroOrdre) {
        this.numeroOrdre = numeroOrdre;
    }

    public boolean getActif() {
        return actif;
    }

    public Produit actif(boolean actif) {
        this.actif = actif;
        return this;
    }

    public Facturier getFacturier() {
        return facturier;
    }

    public void setFacturier(Facturier facturier) {
        this.facturier = facturier;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    public boolean getUnMontant() {
        return aUnMontant;
    }

    public void setaUnMontant(boolean aUnMontant) {
        this.aUnMontant = aUnMontant;
    }

    public double getMontant() {
        return montant;
    }

    public void setMontant(double montant) {
        this.montant = montant;
    }

    public Set<Ecran> getEcrans() {
        return ecrans;
    }

    public Produit ecrans(Set<Ecran> ecrans) {
        this.ecrans = ecrans;
        return this;
    }

    public Produit addEcran(Ecran ecran) {
        this.ecrans.add(ecran);
        ecran.setProduit(this);
        return this;
    }

    public Produit removeEcran(Ecran ecran) {
        this.ecrans.remove(ecran);
        ecran.setProduit(null);
        return this;
    }

    public boolean isTransactionnel() {
        return transactionnel;
    }

    public void setTransactionnel(boolean transactionnel) {
        this.transactionnel = transactionnel;
    }

    public void setEcrans(Set<Ecran> ecrans) {
        this.ecrans = ecrans;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    public boolean isNoScreen() {
        return noScreen;
    }

    public void setNoScreen(boolean noScreen) {
        this.noScreen = noScreen;
    }

    public TypeService getServiceAssocie() {
        return serviceAssocie;
    }

    public void setServiceAssocie(TypeService serviceAssocie) {
        this.serviceAssocie = serviceAssocie;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Produit produit = (Produit) o;
        if (produit.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), produit.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "Produit{"
                + "id=" + id
                + ", nomProduit='" + nomProduit + '\''
                + ", numeroOrdre=" + numeroOrdre
                + ", actif=" + actif
                + ", aUnMontant=" + aUnMontant
                + ", montant=" + montant
                + ", facturier=" + facturier
                + '}';
    }
}
