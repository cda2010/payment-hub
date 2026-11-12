package com.m2i.pti.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.m2i.pti.config.audit.EntityAuditEventListener;
import com.m2i.pti.service.dto.CompteAutreDTO;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

/**
 * A CompteAutre.
 */
@SqlResultSetMappings(value = {
    @SqlResultSetMapping(name = "Mapping.CompteAutreIdDTO",
            classes = @ConstructorResult(targetClass = CompteAutreDTO.class,
                    columns = {
                        @ColumnResult(name = "id", type = Long.class)
                        ,
                                @ColumnResult(name = "solde", type = BigDecimal.class)
                        ,
                                @ColumnResult(name = "codeFse", type = String.class)

                    }))
    ,


        @SqlResultSetMapping(name = "Mapping.CompteAutreByValeurCompteDTO",
            classes = @ConstructorResult(targetClass = CompteAutreDTO.class,
                    columns = {
                        @ColumnResult(name = "id", type = Long.class)
                        ,
                        @ColumnResult(name = "solde", type = BigDecimal.class)
                        ,
                        @ColumnResult(name = "codeFse", type = String.class)
                        ,
                        @ColumnResult(name = "nomClient", type = String.class)
                        ,
                        @ColumnResult(name = "prenomClient", type = String.class)
                        ,
                        @ColumnResult(name = "raisonSociale", type = String.class)
                        ,
                        @ColumnResult(name = "telephone", type = String.class)
                        ,
                        @ColumnResult(name = "codeTel", type = String.class)
                        ,
                        @ColumnResult(name = "libelle", type = String.class)
                        ,
                        @ColumnResult(name = "profile", type = String.class)
                        ,
                        @ColumnResult(name = "telephoneOrganisation", type = String.class)
                        ,
                        @ColumnResult(name = "indicatifPaysOrganisation", type = String.class),}))
})

@NamedNativeQueries({
    @NamedNativeQuery(name = "CompteAutre.findByCompteAutreId",
            query = "select id, solde, code_fse codeFse "
            + "from compte_autre where id= CAST (CAST (:id as text) as BIGINT)",
            resultSetMapping = "Mapping.CompteAutreIdDTO", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})
    ,

        @NamedNativeQuery(name = "CompteAutre.findCompteAutreDTO",
            query = "select ca.id, ca.solde, ca.code_fse codeFse, ph.nom_client nomClient, ph.prenom_client prenomClient, pm.raison_sociale raisonSociale, "
            + "p.telephone, p.code_telephone codeTel, uo.libelle_unite_organisation libelle, pa.jhi_type profile, uo.telephone telephoneOrganisation, uo.indicatif_pays indicatifPaysOrganisation from compte_autre ca left Join compte c on ca.id=c.compte_autre_id "
            + "left Join client cl on c.client_id=cl.id "
            + "left Join personne p on cl.personne_id=p.id "
            + "left Join personne_physique ph on ph.id=p.personne_physique_id "
            + "left Join unite_organisation uo on uo.id=c.unite_organisation_id "
            + "left Join profil_acteur pa on pa.id=cl.profil_acteur_id "
            + "left Join personne_morale pm on pm.id=p.personne_morale_id where c.valeur_compte=:valeurCompte",
            resultSetMapping = "Mapping.CompteAutreByValeurCompteDTO", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})})

@Entity
@Table(name = "compte_autre")
public class CompteAutre extends AbstractAuditingEntity implements Serializable {

    @Transient
    @JsonIgnore
    private final Object lock = new Object();
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Version
    private Integer version;
    @Column(name = "solde", precision = 20, scale = 2)
    private volatile BigDecimal solde = BigDecimal.ZERO;

    @Column(name = "codeFse")
    private String codeFse;

    @OneToMany(mappedBy = "compteAutre", fetch = FetchType.LAZY)
    @JsonIgnore
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<TransactionComptePti> transactionComptePtis = new HashSet<>();
    @Column(name = "date_derniere_transaction")
    private ZonedDateTime dateDerniereTransaction;

    
 
    
    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public synchronized BigDecimal getSolde() {
        synchronized (lock) {
            return solde;
        }

    }

    public synchronized CompteAutre withdrawPTI(BigDecimal montant) {
        synchronized (lock) {
            System.out.println("Solde avant" + this.solde);
            this.solde = solde.subtract(montant.setScale(0, RoundingMode.HALF_UP));
            this.dateDerniereTransaction = ZonedDateTime.now();
            System.out.println("Solde apres" + this.solde);
        }

        return this;
    }

    public synchronized CompteAutre withdraw(BigDecimal montant) {
        synchronized (lock) {
            System.out.println("Solde avant" + this.solde);
            this.solde = solde.subtract(montant.abs().setScale(0, RoundingMode.HALF_UP));
            this.dateDerniereTransaction = ZonedDateTime.now();
            System.out.println("Solde apres" + this.solde);
        }

        return this;
    }

    public synchronized CompteAutre deposit(BigDecimal montant) {
        synchronized (lock) {
            this.solde = solde.add(montant.abs().setScale(0, RoundingMode.HALF_UP));
            this.dateDerniereTransaction = ZonedDateTime.now();
        }
        return this;
    }

    public synchronized CompteAutre depositPTI(BigDecimal montant) {
        synchronized (lock) {
            this.solde = solde.add(montant.setScale(0, RoundingMode.HALF_UP));
            this.dateDerniereTransaction = ZonedDateTime.now();
        }
        return this;
    }

    public CompteAutre solde(BigDecimal solde) {
        this.solde = solde;
        return this;
    }

    public String getCodeFse() {
        return codeFse;
    }

    public void setCodeFse(String codeFse) {
        this.codeFse = codeFse;
    }

    public void setSolde(BigDecimal solde) {
        synchronized (lock) {
            this.solde = solde;
        }

    }

    public Set<TransactionComptePti> getTransactionComptePtis() {
        return transactionComptePtis;
    }

    public CompteAutre transactionComptePtis(Set<TransactionComptePti> transactionComptePtis) {
        this.transactionComptePtis = transactionComptePtis;
        return this;
    }

    public CompteAutre addTransactionComptePti(TransactionComptePti transactionComptePti) {
        this.transactionComptePtis.add(transactionComptePti);
        transactionComptePti.setCompteAutre(this);
        return this;
    }

    public CompteAutre removeTransactionComptePti(TransactionComptePti transactionComptePti) {
        this.transactionComptePtis.remove(transactionComptePti);
        transactionComptePti.setCompteAutre(null);
        return this;
    }

    public void setTransactionComptePtis(Set<TransactionComptePti> transactionComptePtis) {
        this.transactionComptePtis = transactionComptePtis;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public ZonedDateTime getDateDerniereTransaction() {
        return dateDerniereTransaction;
    }

    public void setDateDerniereTransaction(ZonedDateTime dateDerniereTransaction) {
        this.dateDerniereTransaction = dateDerniereTransaction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CompteAutre compteAutre = (CompteAutre) o;
        if (compteAutre.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), compteAutre.getId());
    }

    @Override
    public Object clone() {
        CompteAutre cpteAutre = new CompteAutre();
        try {
            cpteAutre = (CompteAutre) super.clone();
        } catch (CloneNotSupportedException e) {
            cpteAutre.setCodeFse(codeFse);
            cpteAutre.setSolde(solde);
            cpteAutre.setTransactionComptePtis(transactionComptePtis);
            cpteAutre.setVersion(version);
            cpteAutre.setId(id);
        }

        return cpteAutre;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "CompteAutre{"
                + "id=" + getId()
                + ", solde=" + getSolde()
                + "}";
    }
}
