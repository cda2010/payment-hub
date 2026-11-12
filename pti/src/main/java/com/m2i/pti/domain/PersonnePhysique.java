package com.m2i.pti.domain;

import com.m2i.pti.config.audit.EntityAuditEventListener;
import com.m2i.pti.domain.enumeration.Sexe;
import com.m2i.pti.service.dto.PersonnePhysiqueDTO;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A PersonnePhysique.
 */

@NamedNativeQueries({
        @NamedNativeQuery(name = "PersonnePhysique.findByIdReport",
                query = "select pp.id, pp.nom_client as  nomClient, pp.prenom_client as prenomClient, pp.lieu_naissance as lieuNaissance, pp.date_naissance as dateNaissance, pp.pays_residence_id as paysResidenceId," +
                        "pp.sexe, n.libelle as nationaliteLibelle, pp.nationalite_id as nationaliteId" +
                        " from personne_physique pp " +
                        "left join nationalite n on pp.nationalite_id=n.id" +
                        " where pp.id= CAST (CAST (:ppId as text) as BIGINT)",
                resultSetMapping = "Mapping.PersonnePhysiqueByIdReportDTO",hints={@QueryHint(name="org.hibernate.cacheable",value="false")}),

        @NamedNativeQuery(name = "PersonnePhysique.findByIdClient",
                query = "select pp.id, pp.nom_client as  nomClient, pp.prenom_client as prenomClient, pp.lieu_naissance as lieuNaissance, pp.date_naissance as dateNaissance,pp.nom_mere_client as nomMereClient,pp.personne_prevenir as personnePrevenir , pp.pays_residence_id as paysResidenceId," +
                        "pp.sexe, n.libelle as nationaliteLibelle, pp.nationalite_id as nationaliteId" +
                        " from personne_physique pp " +
                        "left join nationalite n on pp.nationalite_id=n.id" +
                        " where pp.id= CAST (CAST (:ppId as text) as BIGINT)",
                resultSetMapping = "Mapping.PersonnePhysiqueClientDTO",hints={@QueryHint(name="org.hibernate.cacheable",value="false")})
})

@SqlResultSetMappings(value = {
        @SqlResultSetMapping(name = "Mapping.PersonnePhysiqueByIdReportDTO",
                classes = @ConstructorResult(targetClass = PersonnePhysiqueDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = Long.class),
                                @ColumnResult(name = "nomClient", type = String.class),
                                @ColumnResult(name = "prenomClient", type = String.class),
                                @ColumnResult(name = "lieuNaissance", type = String.class),
                                @ColumnResult(name = "dateNaissance", type = LocalDate.class),
                                @ColumnResult(name = "paysResidenceId", type = Long.class),
                                @ColumnResult(name = "sexe", type = String.class),
                                @ColumnResult(name = "nationaliteLibelle", type = String.class),
                                @ColumnResult(name = "nationaliteId", type = Long.class)
                        })),

        @SqlResultSetMapping(name = "Mapping.PersonnePhysiqueClientDTO",
                classes = @ConstructorResult(targetClass = PersonnePhysiqueDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = Long.class),
                                @ColumnResult(name = "nomClient", type = String.class),
                                @ColumnResult(name = "prenomClient", type = String.class),
                                @ColumnResult(name = "lieuNaissance", type = String.class),
                                @ColumnResult(name = "dateNaissance", type = LocalDate.class),
                                @ColumnResult(name = "nomMereClient", type = String.class),
                                @ColumnResult(name = "personnePrevenir", type = String.class),
                                @ColumnResult(name = "paysResidenceId", type = Long.class),
                                @ColumnResult(name = "sexe", type = String.class),
                                @ColumnResult(name = "nationaliteLibelle", type = String.class),
                                @ColumnResult(name = "nationaliteId", type = Long.class)
                        }))
})


@Entity
@Table(name = "personne_physique")
@EntityListeners(value = {EntityAuditEventListener.class})
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class PersonnePhysique extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "nom_client", nullable = false)
    private String nomClient;

    @NotNull
    @Column(name = "prenom_client", nullable = false)
    private String prenomClient;

    @Column(name = "nom_mere_client")
    private String nomMereClient;
    @Column(name = "date_naissance")
    private LocalDate dateNaissance;
    @Column(name = "lieu_naissance")
    private String lieuNaissance;

    @Column(name = "personne_prevenir")
    private String personnePrevenir;

    @Column(name = "profession")
    private String profession;

    @Column(name = "nom_jeune_fille")
    private String nomJeuneFille;


    @Lob
    @Column(name = "photo")
    private byte[] photo;

    @Column(name = "photo_content_type")
    private String photoContentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "sexe")
    private Sexe sexe;

    @ManyToOne
    private Nationalite nationalite;


    @ManyToOne
    private Nationalite paysResidence;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNomClient() {
        return nomClient;
    }

    public PersonnePhysique nomClient(String nomClient) {
        this.nomClient = nomClient;
        return this;
    }

    public void setNomClient(String nomClient) {
        this.nomClient = nomClient;
    }

    public String getPrenomClient() {
        return prenomClient;
    }

    public PersonnePhysique prenomClient(String prenomClient) {
        this.prenomClient = prenomClient;
        return this;
    }

    public void setPrenomClient(String prenomClient) {
        this.prenomClient = prenomClient;
    }

    public LocalDate getDateNaissance() {
        return dateNaissance;
    }

    public PersonnePhysique dateNaissance(LocalDate dateNaissance) {
        this.dateNaissance = dateNaissance;
        return this;
    }

    public void setDateNaissance(LocalDate dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    public byte[] getPhoto() {
        return photo;
    }

    public void setPhoto(byte[] photo) {
        this.photo = photo;
    }

    public String getPhotoContentType() {
        return photoContentType;
    }

    public void setPhotoContentType(String photoContentType) {
        this.photoContentType = photoContentType;
    }

    public String getPersonnePrevenir() {
        return personnePrevenir;
    }

    public PersonnePhysique personnePrevenir(String personnePrevenir) {
        this.personnePrevenir = personnePrevenir;
        return this;
    }

    public void setPersonnePrevenir(String personnePrevenir) {
        this.personnePrevenir = personnePrevenir;
    }

    public String getLieuNaissance() {
        return lieuNaissance;
    }

    public PersonnePhysique lieuNaissance(String lieuNaissance) {
        this.lieuNaissance = lieuNaissance;
        return this;
    }

    public void setLieuNaissance(String lieuNaissance) {
        this.lieuNaissance = lieuNaissance;
    }

    public Sexe getSexe() {
        return sexe;
    }

    public PersonnePhysique sexe(Sexe sexe) {
        this.sexe = sexe;
        return this;
    }

    public void setSexe(Sexe sexe) {
        this.sexe = sexe;
    }

    public Nationalite getNationalite() {
        return nationalite;
    }

    public PersonnePhysique nationalite(Nationalite nationalite) {
        this.nationalite = nationalite;
        return this;
    }

    public Nationalite getPaysResidence() {
        return paysResidence;
    }

    public void setPaysResidence(Nationalite paysResidence) {
        this.paysResidence = paysResidence;
    }

    public void setNationalite(Nationalite nationalite) {
        this.nationalite = nationalite;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    public String getNomMereClient() {
        return nomMereClient;
    }

    public void setNomMereClient(String nomMereClient) {
        this.nomMereClient = nomMereClient;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public String getNomJeuneFille() {
        return nomJeuneFille;
    }

    public void setNomJeuneFille(String nomJeuneFille) {
        this.nomJeuneFille = nomJeuneFille;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PersonnePhysique personnePhysique = (PersonnePhysique) o;
        if (personnePhysique.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), personnePhysique.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "PersonnePhysique{" +
            "id=" + getId() +
            ", nomClient='" + getNomClient() + "'" +
            ", prenomClient='" + getPrenomClient() + "'" +
            ", dateNaissance='" + getDateNaissance() + "'" +
            ", personnePrevenir='" + getPersonnePrevenir() + "'" +
            ", lieuNaissance='" + getLieuNaissance() + "'" +
            ", sexe='" + getSexe() + "'" +
            "}";
    }
}
