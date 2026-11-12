package com.m2i.pti.domain;

import com.m2i.pti.domain.enumeration.TypeIntervalle;
import com.m2i.pti.domain.enumeration.TypeTarif;
import com.m2i.pti.service.dto.LigneRepartitionCommissionDTO;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * A LigneRepartitionCommission.
 */
@SqlResultSetMappings(value = {
        @SqlResultSetMapping(name = "Mapping.LigneRepartitionCommissionDTO",
                classes = @ConstructorResult(targetClass = LigneRepartitionCommissionDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = Long.class),
                                @ColumnResult(name = "niveau", type = Integer.class),
                                @ColumnResult(name = "valeur", type = Double.class),
                                @ColumnResult(name = "somme", type = BigDecimal.class),
                                @ColumnResult(name = "typeTarif", type = Integer.class),
                                @ColumnResult(name = "min", type = BigDecimal.class),
                                @ColumnResult(name = "max", type = BigDecimal.class),
                                @ColumnResult(name = "surFrais", type = Boolean.class),
                                @ColumnResult(name = "tableauRepartitionCommissionId", type = Long.class)
                        }))
})

@NamedNativeQueries({
        @NamedNativeQuery(name = "LigneRepartitionCommission.findTableauRepartitionCommissionId",
                query = "SELECT id, niveau, valeur, somme, type_tarif as typeTarif, min, max, sur_frais as surFrais, tableau_repartition_commission_id as tableauRepartitionCommissionId" +
                        " FROM ligne_repartition_commission where tableau_repartition_commission_id= CAST (CAST (:tableauId as TEXT) as BIGINT)",
                resultSetMapping = "Mapping.LigneRepartitionCommissionDTO",hints={@QueryHint(name="org.hibernate.cacheable",value="false")})
})



@Entity
@Table(name = "ligne_repartition_commission")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class LigneRepartitionCommission extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "niveau")
    private Integer niveau;

    @Column(name = "valeur")
    private Double valeur;

    @Column(name = "somme")
    private BigDecimal somme;

    @Column(name = "type_tarif")
    private TypeTarif typeTarif;

    
    @Column(name = "type_intervalle")
    private String typeIntervalle;
     
    
    @Column(name = "min")
    private BigDecimal min;

    @Column(name = "max")
    private BigDecimal max;

    @Column(name = "sur_frais")
    private Boolean surFrais;

    @ManyToOne
    private TableauRepartitionCommission tableauRepartitionCommission;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getNiveau() {
        return niveau;
    }

    public LigneRepartitionCommission niveau(Integer niveau) {
        this.niveau = niveau;
        return this;
    }

    public Double getValeur() {
        return valeur;
    }

    public void setValeur(Double valeur) {
        this.valeur = valeur;
    }

    public void setNiveau(Integer niveau) {
        this.niveau = niveau;
    }

    public BigDecimal getSomme() {
        return somme;
    }

    public void setSomme(BigDecimal somme) {
        this.somme = somme;
    }

    public TypeTarif getTypeTarif() {
        return typeTarif;
    }

    public void setTypeTarif(TypeTarif typeTarif) {
        this.typeTarif = typeTarif;
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

    public void setMax(BigDecimal max) {
        this.max = max;
    }

    public Boolean getSurFrais() {
        return surFrais;
    }

    public void setSurFrais(Boolean surFrais) {
        this.surFrais = surFrais;
    }

    public TableauRepartitionCommission getTableauRepartitionCommission() {
        return tableauRepartitionCommission;
    }

    public LigneRepartitionCommission tableauRepartitionCommission(TableauRepartitionCommission tableauRepartitionCommission) {
        this.tableauRepartitionCommission = tableauRepartitionCommission;
        return this;
    }

    public void setTableauRepartitionCommission(TableauRepartitionCommission tableauRepartitionCommission) {
        this.tableauRepartitionCommission = tableauRepartitionCommission;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    public String getTypeIntervalle() {
        return typeIntervalle;
    }

    public void setTypeIntervalle(String typeIntervalle) {
        this.typeIntervalle = typeIntervalle;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LigneRepartitionCommission ligneRepartitionCommission = (LigneRepartitionCommission) o;
        if (ligneRepartitionCommission.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), ligneRepartitionCommission.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "LigneRepartitionCommission{" +
            "id=" + getId() +
            ", niveau=" + getNiveau() +
            ", valeur=" + getValeur() +
            "}";
    }
}
