package com.m2i.pti.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;

@Entity
@Table(name = "profil_acteur_tableau_cumul")
@Cacheable(value = true)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)

public class ProfilActeurTableauCumul extends AbstractAuditingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long profil_acteurs_id;

    private Long tableau_cumuls_id;

    public Long getProfil_acteurs_id() {
        return profil_acteurs_id;
    }

    public void setProfil_acteurs_id(Long profil_acteurs_id) {
        this.profil_acteurs_id = profil_acteurs_id;
    }

    public Long getTableau_cumuls_id() {
        return tableau_cumuls_id;
    }

    public void setTableau_cumuls_id(Long tableau_cumuls_id) {
        this.tableau_cumuls_id = tableau_cumuls_id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
