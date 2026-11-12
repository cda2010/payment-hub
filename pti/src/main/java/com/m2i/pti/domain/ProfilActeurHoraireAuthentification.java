package com.m2i.pti.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "profil_acteur_horaire_authentification")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class ProfilActeurHoraireAuthentification extends AbstractAuditingEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long profil_acteurs_id;

    private Long horaire_authentifications_id;

    public Long getProfil_acteurs_id() {
        return profil_acteurs_id;
    }

    public void setProfil_acteurs_id(Long profil_acteurs_id) {
        this.profil_acteurs_id = profil_acteurs_id;
    }

    public Long getHoraire_authentifications_id() {
        return horaire_authentifications_id;
    }

    public void setHoraire_authentifications_id(Long horaire_authentifications_id) {
        this.horaire_authentifications_id = horaire_authentifications_id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
