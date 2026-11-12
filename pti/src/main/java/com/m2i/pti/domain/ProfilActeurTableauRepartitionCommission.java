package com.m2i.pti.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;

@Entity
@Table(name = "profil_acteur_tableau_repartition_commission")
@Cacheable(value = true)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)

public class ProfilActeurTableauRepartitionCommission  extends AbstractAuditingEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long profil_acteurs_id;

    private Long tableau_repartition_commissions_id;

    public Long getProfil_acteurs_id() {
        return profil_acteurs_id;
    }

    public void setProfil_acteurs_id(Long profil_acteurs_id) {
        this.profil_acteurs_id = profil_acteurs_id;
    }

    public Long getTableau_repartition_commissions_id() {
        return tableau_repartition_commissions_id;
    }

    public void setTableau_repartition_commissions_id(Long tableau_repartition_commissions_id) {
        this.tableau_repartition_commissions_id = tableau_repartition_commissions_id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
