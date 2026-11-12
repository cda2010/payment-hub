package com.m2i.pti.domain;

import com.m2i.pti.config.Constants;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.m2i.pti.config.audit.EntityAuditEventListener;
import com.m2i.pti.service.dto.UserDTO;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.constraints.Email;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.time.Instant;

/**
 * A user.
 */
@SqlResultSetMappings(value = {
        @SqlResultSetMapping(name = "Mapping.UserDTO",
                classes = @ConstructorResult(targetClass = UserDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = Long.class),
                                @ColumnResult(name = "login", type = String.class),
                                @ColumnResult(name = "firstName", type = String.class),
                                @ColumnResult(name = "lastName", type = String.class),
                                @ColumnResult(name = "email", type = String.class),
                                @ColumnResult(name = "phoneNumber", type = String.class),
                                @ColumnResult(name = "activated", type = Boolean.class),
                                @ColumnResult(name = "langKey", type = String.class),
                                @ColumnResult(name = "createdBy", type = String.class),
                                @ColumnResult(name = "createdDate", type = Instant.class),
                                @ColumnResult(name = "lastModifiedBy", type = String.class),
                                @ColumnResult(name = "lastModifiedDate", type = Instant.class),
                                @ColumnResult(name = "profilId", type = Long.class),
                                @ColumnResult(name = "profilNom", type = String.class),
                                @ColumnResult(name = "uniteOrganisationId", type = Long.class),
                                @ColumnResult(name = "pointVenteId", type = Long.class),
                                @ColumnResult(name = "uniteOrganisationNom", type = String.class),
                                @ColumnResult(name = "codeIndicatif", type = String.class),
                                @ColumnResult(name = "nbrFailure", type = Integer.class),
                                @ColumnResult(name = "dateDerniereConnexion", type = ZonedDateTime.class),
                                @ColumnResult(name = "isBackOffice", type = Integer.class)
                        }))
})

@NamedNativeQueries({
        @NamedNativeQuery(name = "User.findAllUser",
                query = "SELECT jh.id,jh.login,jh.first_name as firstName,jh.last_name as lastName,jh.email,jh.phone_number as phoneNumber," +
                        "jh.activated,jh.lang_key as langKey,jh.created_by as createdBy,jh.created_date as createdDate,jh.last_modified_by as lastModifiedBy," +
                        "jh.last_modified_date as lastModifiedDate,jh.profil_id as profilId,p.nom as profilNom,jh.unite_organisation_id as uniteOrganisationId," +
                        "jh.point_vente_id as pointVenteId,uo.libelle_unite_organisation as uniteOrganisationNom,jh.code_indicatif as codeIndicatif," +
                        "jh.nbr_failure as nbrFailure,jh.date_derniere_connexion as dateDerniereConnexion,jh.is_back_office as isBackOffice" +
                        " FROM jhi_user jh " +
                        "LEFT JOIN profil p on jh.profil_id=p.id" +
                        " INNER JOIN unite_organisation uo on jh.unite_organisation_id=uo.id" +
                        " where (" +
                        "((:uoId) is null or jh.unite_organisation_id in (:uoId))" +
                        "and (:profilId is null or jh.profil_id= CAST(CAST (:profilId AS TEXT) AS BIGINT ))" +
                        "and (:login is null or :login='' or jh.login like ('%'||CAST (:login AS TEXT)||'%'))" +
                        "and (:telephone is null or :telephone='' or jh.phone_number like('%'||CAST (:telephone AS TEXT)||'%'))) order by jh.id desc limit :taille offset :page",
                resultSetMapping = "Mapping.UserDTO",hints={@QueryHint(name="org.hibernate.cacheable",value="false")}),
       @NamedNativeQuery(name = "User.findUser",
                query = "SELECT jh.id,jh.login,jh.first_name as firstName,jh.last_name as lastName,jh.email,jh.phone_number as phoneNumber," +
                        "jh.activated,jh.lang_key as langKey,jh.created_by as createdBy,jh.created_date as createdDate,jh.last_modified_by as lastModifiedBy," +
                        "jh.last_modified_date as lastModifiedDate,jh.profil_id as profilId,p.nom as profilNom,jh.unite_organisation_id as uniteOrganisationId," +
                        "jh.point_vente_id as pointVenteId,uo.libelle_unite_organisation as uniteOrganisationNom,jh.code_indicatif as codeIndicatif," +
                        "jh.nbr_failure as nbrFailure,jh.date_derniere_connexion as dateDerniereConnexion,jh.is_back_office as isBackOffice" +
                        " FROM jhi_user jh " +
                        "LEFT JOIN profil p on jh.profil_id=p.id" +
                        " INNER JOIN unite_organisation uo on jh.unite_organisation_id=uo.id" +
                        " where (" +
                        " (login=CAST(:login AS TEXT)))" 

                        ,
                resultSetMapping = "Mapping.UserDTO",hints={@QueryHint(name="org.hibernate.cacheable",value="false")}),

        @NamedNativeQuery(name = "User.findAllUserIfAdmin",
                query = "SELECT jh.id,jh.login,jh.first_name as firstName,jh.last_name as lastName,jh.email,jh.phone_number as phoneNumber," +
                        "jh.activated,jh.lang_key as langKey,jh.created_by as createdBy,jh.created_date as createdDate,jh.last_modified_by as lastModifiedBy," +
                        "jh.last_modified_date as lastModifiedDate,jh.profil_id as profilId,p.nom as profilNom,jh.unite_organisation_id as uniteOrganisationId," +
                        "jh.point_vente_id as pointVenteId,uo.libelle_unite_organisation as uniteOrganisationNom,jh.code_indicatif as codeIndicatif," +
                        "jh.nbr_failure as nbrFailure,jh.date_derniere_connexion as dateDerniereConnexion,jh.is_back_office as isBackOffice" +
                        " FROM jhi_user jh " +
                        "LEFT JOIN profil p on jh.profil_id=p.id" +
                        " INNER JOIN unite_organisation uo on jh.unite_organisation_id=uo.id" +
                        " where (" +
                        "((:uoId) is null or jh.unite_organisation_id in (CAST(CAST (:uoId AS TEXT) AS BIGINT )))" +
                        "and (:profilId is null or jh.profil_id= CAST(CAST (:profilId AS TEXT) AS BIGINT ))" +
                        "and (:login is null or :login='' or jh.login like ('%'||CAST (:login AS TEXT)||'%'))" +
                        "and (:telephone is null or :telephone='' or jh.phone_number like('%'||CAST (:telephone AS TEXT)||'%'))) order by jh.id desc limit :taille offset :page",
                resultSetMapping = "Mapping.UserDTO",hints={@QueryHint(name="org.hibernate.cacheable",value="false")}),
        @NamedNativeQuery(name = "User.findManagedOpen",
                query = "SELECT jh.id,jh.login,jh.first_name as firstName,jh.last_name as lastName,jh.email,jh.phone_number as phoneNumber," +
                        "jh.activated,jh.lang_key as langKey,jh.created_by as createdBy,jh.created_date as createdDate,jh.last_modified_by as lastModifiedBy," +
                        "jh.last_modified_date as lastModifiedDate,jh.profil_id as profilId,p.nom as profilNom,jh.unite_organisation_id as uniteOrganisationId," +
                        "jh.point_vente_id as pointVenteId,uo.libelle_unite_organisation as uniteOrganisationNom,jh.code_indicatif as codeIndicatif," +
                        "jh.nbr_failure as nbrFailure,jh.date_derniere_connexion as dateDerniereConnexion,jh.is_back_office as isBackOffice" +
                        " FROM jhi_user jh " +
                        "LEFT JOIN profil p on jh.profil_id=p.id" +
                        " INNER JOIN unite_organisation uo on jh.unite_organisation_id=uo.id" +
                        " where (" +
                        "(:profilId is null or jh.profil_id= CAST(CAST (:profilId AS TEXT) AS BIGINT ))" +
                        "and (:login is null or :login='' or jh.login like ('%'||CAST (:login AS TEXT)||'%'))" +
                        "and (:telephone is null or :telephone='' or jh.phone_number like('%'||CAST (:telephone AS TEXT)||'%'))) order by jh.id desc limit :taille offset :page",
                resultSetMapping = "Mapping.UserDTO",hints={@QueryHint(name="org.hibernate.cacheable",value="false")})

})


@Entity
/*@NamedEntityGraph(name = "User.authorities",
        attributeNodes = @NamedAttributeNode("authorities")
)*/
@Table(name = "jhi_user")
@EntityListeners(value = {EntityAuditEventListener.class})
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class User extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Pattern(regexp = Constants.LOGIN_REGEX)
    @Size(min = 1, max = 50)
    @Column(length = 50, unique = true, nullable = false)
    private String login;

    @JsonIgnore
    @NotNull
    @Size(min = 60, max = 60)
    @Column(name = "password_hash", length = 60)
    private String password;

    @Size(max = 50)
    @Column(name = "first_name", length = 50)
    private String firstName;

    @Size(max = 50)
    @Column(name = "last_name", length = 50)
    private String lastName;

    @Email
    @Size(min = 5, max = 100)
    @Column(length = 100, unique = true)
    private String email;

    @Column(length = 100, unique = true)
    private String phoneNumber;

    @NotNull
    @Column(nullable = false)
    private boolean activated = false;

    @Size(min = 2, max = 5)
    @Column(name = "lang_key", length = 5)
    private String langKey;

    @Size(max = 256)
    @Column(name = "image_url", length = 256)
    private String imageUrl;

    @Size(max = 20)
    @Column(name = "activation_key", length = 20)
    @JsonIgnore
    private String activationKey;

    @Size(max = 20)
    @Column(name = "reset_key", length = 20)
    @JsonIgnore
    private String resetKey;

    @Column(name = "reset_date")
    private Instant resetDate = null;

    private Integer nbrFailure;
    private String codeIndicatif;
    private ZonedDateTime dateDerniereConnexion;

    @JsonIgnore
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "jhi_user_authority",
            joinColumns = {
                @JoinColumn(name = "user_id", referencedColumnName = "id")},
            inverseJoinColumns = {
                @JoinColumn(name = "authority_name", referencedColumnName = "name")})
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @BatchSize(size = 20)
    private Set<Authority> authorities = new HashSet<>();

    @ManyToOne
    private PointVente pointVente;

    @ManyToOne
    private UniteOrganisation organisation;

    @ManyToOne
    private UniteOrganisation uniteOrganisation;

    @OneToMany(mappedBy = "utilisateur")
    @JsonIgnore
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<Autorisation> autorisations = new HashSet<>();

  

    

    @ManyToOne
    private Profil profil;
    @ManyToOne
    private Client client;
    @ManyToOne
    private Profil profilPrecedent;
    private Integer isBackOffice;

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    //Lowercase the login before saving it in database
    public void setLogin(String login) {
        this.login = StringUtils.lowerCase(login, Locale.ENGLISH);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean getActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public String getActivationKey() {
        return activationKey;
    }

    public void setActivationKey(String activationKey) {
        this.activationKey = activationKey;
    }

    public String getResetKey() {
        return resetKey;
    }

    public void setResetKey(String resetKey) {
        this.resetKey = resetKey;
    }

    public Instant getResetDate() {
        return resetDate;
    }

    public void setResetDate(Instant resetDate) {
        this.resetDate = resetDate;
    }

    public String getLangKey() {
        return langKey;
    }

    public void setLangKey(String langKey) {
        this.langKey = langKey;
    }

    public Set<Authority> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(Set<Authority> authorities) {
        this.authorities = authorities;
    }

    public PointVente getPointVente() {
        return pointVente;
    }

    public void setPointVente(PointVente pointVente) {
        this.pointVente = pointVente;
    }

    public Set<Autorisation> getAutorisations() {
        return autorisations;
    }

    public User autorisations(Set<Autorisation> autorisations) {
        this.autorisations = autorisations;
        return this;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setAutorisations(Set<Autorisation> autorisations) {
        this.autorisations = autorisations;
    }

    public ZonedDateTime getDateDerniereConnexion() {
        return dateDerniereConnexion;
    }

    public void setDateDerniereConnexion(ZonedDateTime dateDerniereConnexion) {
        this.dateDerniereConnexion = dateDerniereConnexion;
    }

    public User addAutorisation(Autorisation autorisation) {
        this.autorisations.add(autorisation);
        autorisation.setUtilisateur(this);
        return this;
    }

    public User removeUtilisateur(Autorisation autorisation) {
        this.autorisations.remove(autorisation);
        autorisation.setUtilisateur(null);
        return this;
    }

    

    public Profil getProfilPrecedent() {
        return profilPrecedent;
    }

    public void setProfilPrecedent(Profil profilPrecedent) {
        this.profilPrecedent = profilPrecedent;
    }

   

    public UniteOrganisation getOrganisation() {
        return organisation;
    }

    public void setOrganisation(UniteOrganisation organisation) {
        this.organisation = organisation;
    }

   

    public Profil getProfil() {
        return profil;
    }

    public void setProfil(Profil profil) {
        this.profil = profil;
    }

    public UniteOrganisation getUniteOrganisation() {
        return uniteOrganisation;
    }

    public void setUniteOrganisation(UniteOrganisation uniteOrganisation) {
        this.uniteOrganisation = uniteOrganisation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        User user = (User) o;
        return !(user.getId() == null || getId() == null) && Objects.equals(getId(), user.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    public Integer getNbrFailure() {
        return nbrFailure;
    }

    public void setNbrFailure(Integer nbrFailure) {
        this.nbrFailure = nbrFailure;
    }

    public Integer getIsBackOffice() {
        return isBackOffice;
    }

    public void setIsBackOffice(Integer isBackOffice) {
        this.isBackOffice = isBackOffice;
    }

    public String getCodeIndicatif() {
        return codeIndicatif;
    }

    public void setCodeIndicatif(String codeIndicatif) {
        this.codeIndicatif = codeIndicatif;
    }

    @Override
    public String toString() {
        return "User{"
                + "login='" + login + '\''
                + ", firstName='" + firstName + '\''
                + ", lastName='" + lastName + '\''
                + ", email='" + email + '\''
                + ", imageUrl='" + imageUrl + '\''
                + ", activated='" + activated + '\''
                + ", langKey='" + langKey + '\''
                + ", activationKey='" + activationKey + '\''
                + "}";
    }
}
