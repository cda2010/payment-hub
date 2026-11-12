package com.m2i.pti.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.m2i.pti.config.audit.EntityAuditEventListener;
import com.m2i.pti.domain.enumeration.DestinationProfilActeur;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

import com.m2i.pti.domain.enumeration.TypeProfilActeur;

/**
 * A ProfilActeur.
 */
@Entity
@Table(name = "profil_acteur")
@EntityListeners(value = {EntityAuditEventListener.class})
@Cacheable(value = true)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class ProfilActeur extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "nom", nullable = false)
    private String nom;

    @NotNull
    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "code_fse")
    private String codeFse;
    
    @Column(name = "active")
    private Boolean active;

    @Enumerated(EnumType.STRING)
    @Column(name = "jhi_type")
    private TypeProfilActeur type;

    @Column(name = "destine_a")
    private DestinationProfilActeur destineA;

    @ManyToOne
    GroupeProfilActeur groupe;

    @ManyToMany
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @JoinTable(name = "profil_acteur_horaire_authentification",
            joinColumns = @JoinColumn(name="profil_acteurs_id", referencedColumnName="id"),
            inverseJoinColumns = @JoinColumn(name="horaire_authentifications_id", referencedColumnName="id"))
    private Set<HoraireAuthentification> horaireAuthentifications = new HashSet<>();


    @ManyToMany
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @JoinTable(name = "profil_acteur_tableau_frais",
            joinColumns = @JoinColumn(name="profil_acteurs_id", referencedColumnName="id"),
            inverseJoinColumns = @JoinColumn(name="tableau_frais_id", referencedColumnName="id"))
    private Set<TableauFrais> tableauFrais = new HashSet<>();

    @OneToMany(mappedBy = "profilActeur", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<UniteOrganisation> organisations = new HashSet<>();

    @OneToMany(mappedBy = "profilActeur", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Client> clients = new HashSet<>();

    public GroupeProfilActeur getGroupe() {
        return groupe;
    }

    public void setGroupe(GroupeProfilActeur groupe) {
        this.groupe = groupe;
    }

    @ManyToMany
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @JoinTable(name = "profil_acteur_tableau_cumul",
            joinColumns = @JoinColumn(name="profil_acteurs_id", referencedColumnName="id"),
            inverseJoinColumns = @JoinColumn(name="tableau_cumuls_id", referencedColumnName="id"))
    private Set<TableauCumul> tableauCumuls = new HashSet<>();

    @ManyToMany
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @JoinTable(name = "profil_acteur_group_repartition",
            joinColumns = @JoinColumn(name="profil_acteurs_id", referencedColumnName="id"),
            inverseJoinColumns = @JoinColumn(name="group_repartition_id", referencedColumnName="id"))
    private Set<GroupRepartition> groupRepartitions = new HashSet<>();

    @ManyToMany
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @JoinTable(name = "profil_acteur_tableau_repartition_commission",
            joinColumns = @JoinColumn(name="profil_acteurs_id", referencedColumnName="id"),
            inverseJoinColumns = @JoinColumn(name="tableau_repartition_commissions_id", referencedColumnName="id"))
    private Set<TableauRepartitionCommission> tableauRepartitionCommissions = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public ProfilActeur nom(String nom) {
        this.nom = nom;
        return this;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public ProfilActeur description(String description) {
        this.description = description;
        return this;
    }

    public DestinationProfilActeur getDestineA() {
        return destineA;
    }

    public void setDestineA(DestinationProfilActeur destineA) {
        this.destineA = destineA;
    }

    public Set<GroupRepartition> getGroupRepartitions() {
        return groupRepartitions;
    }

    public void setGroupRepartitions(Set<GroupRepartition> groupRepartitions) {
        this.groupRepartitions = groupRepartitions;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean isActive() {
        return active;
    }

    public ProfilActeur active(Boolean active) {
        this.active = active;
        return this;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public TypeProfilActeur getType() {
        return type;
    }

    public ProfilActeur type(TypeProfilActeur type) {
        this.type = type;
        return this;
    }

    public void setType(TypeProfilActeur type) {
        this.type = type;
    }

    public Set<TableauFrais> getTableauFrais() {
        return tableauFrais;
    }

    public ProfilActeur tableauFrais(Set<TableauFrais> tableauFrais) {
        this.tableauFrais = tableauFrais;
        return this;
    }



    public void setTableauFrais(Set<TableauFrais> tableauFrais) {
        this.tableauFrais = tableauFrais;
    }

    public Set<UniteOrganisation> getOrganisations() {
        return organisations;
    }

    public ProfilActeur organisations(Set<UniteOrganisation> uniteOrganisations) {
        this.organisations = uniteOrganisations;
        return this;
    }

    public ProfilActeur addOrganisation(UniteOrganisation uniteOrganisation) {
        this.organisations.add(uniteOrganisation);
        uniteOrganisation.setProfilActeur(this);
        return this;
    }

    public ProfilActeur removeOrganisation(UniteOrganisation uniteOrganisation) {
        this.organisations.remove(uniteOrganisation);
        uniteOrganisation.setProfilActeur(null);
        return this;
    }

    public void setOrganisations(Set<UniteOrganisation> uniteOrganisations) {
        this.organisations = uniteOrganisations;
    }

    public Set<Client> getClients() {
        return clients;
    }

   

    

    public void setClients(Set<Client> clients) {
        this.clients = clients;
    }

    public Set<TableauCumul> getTableauCumuls() {
        return tableauCumuls;
    }

    public ProfilActeur tableauCumuls(Set<TableauCumul> tableauCumuls) {
        this.tableauCumuls = tableauCumuls;
        return this;
    }


    public void setTableauCumuls(Set<TableauCumul> tableauCumuls) {
        this.tableauCumuls = tableauCumuls;
    }

    public Set<TableauRepartitionCommission> getTableauRepartitionCommissions() {
        return tableauRepartitionCommissions;
    }

    public ProfilActeur tableauRepartitionCommissions(Set<TableauRepartitionCommission> tableauRepartitionCommissions) {
        this.tableauRepartitionCommissions = tableauRepartitionCommissions;
        return this;
    }


    public void setTableauRepartitionCommissions(Set<TableauRepartitionCommission> tableauRepartitionCommissions) {
        this.tableauRepartitionCommissions = tableauRepartitionCommissions;
    }

    public Set<HoraireAuthentification> getHoraireAuthentifications() {
        return horaireAuthentifications;
    }

    public void setHoraireAuthentifications(Set<HoraireAuthentification> horaireAuthentifications) {
        this.horaireAuthentifications = horaireAuthentifications;
    }

    public String getCodeFse() {
        return codeFse;
    }

    public void setCodeFse(String codeFse) {
        this.codeFse = codeFse;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProfilActeur profilActeur = (ProfilActeur) o;
        if (profilActeur.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), profilActeur.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "ProfilActeur{" +
                "id=" + getId() +
                ", nom='" + getNom() + "'" +
                ", description='" + getDescription() + "'" +
                ", active='" + isActive() + "'" +
                ", type='" + getType() + "'" +
                "}";
    }
}
