package com.m2i.pti.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.m2i.pti.config.audit.EntityAuditEventListener;
import com.m2i.pti.service.dto.ClientDTO;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@SqlResultSetMapping(name = "Mapping.ClientDTO1",
        classes = @ConstructorResult(targetClass = ClientDTO.class,
                columns = {
                    @ColumnResult(name = "id", type = Long.class)
                    ,
                    @ColumnResult(name = "codeClient", type = String.class)
                    ,
                    @ColumnResult(name = "personneId", type = Long.class)
                    ,
                    @ColumnResult(name = "profilId", type = Long.class)
                    ,
                    @ColumnResult(name = "profilActeurId", type = Long.class)
                    ,
                    @ColumnResult(name = "groupeProfilActeurId", type = Long.class)
                    ,
                    @ColumnResult(name = "clientValide", type = Boolean.class)
                    ,
                     @ColumnResult(name = "reseauId", type = Long.class)
                }))

/**
 * A Client.
 */
@SqlResultSetMappings(value = {
    @SqlResultSetMapping(name = "Mapping.ClientDTO",
            classes = @ConstructorResult(targetClass = ClientDTO.class,
                    columns = {
                        @ColumnResult(name = "id", type = Long.class)
                        ,
                                @ColumnResult(name = "codeClient", type = String.class)
                        ,
                                @ColumnResult(name = "personneId", type = Long.class)
                        ,
                                @ColumnResult(name = "clientValide", type = Boolean.class)
                        ,
                                @ColumnResult(name = "clientIdentifie", type = Boolean.class)
                        ,
                                @ColumnResult(name = "profilId", type = Long.class)
                        ,
                                @ColumnResult(name = "profilActeurId", type = Long.class)
                        ,
                                @ColumnResult(name = "groupeProfilActeurId", type = Long.class)
                        ,
                                @ColumnResult(name = "createdDate", type = Instant.class)
                        ,
                                @ColumnResult(name = "createdBy", type = String.class)
                        ,
                                @ColumnResult(name = "lastModifiedBy", type = String.class)
                        ,
                                @ColumnResult(name = "lastModifiedDate", type = Instant.class)
                        ,
                                @ColumnResult(name = "uniteOrganisationCreateurId", type = Long.class)
                        ,        @ColumnResult(name = "reseauId", type = Long.class)
                    }))
    ,
        @SqlResultSetMapping(name = "Mapping.ClientDTO2",
            classes = @ConstructorResult(targetClass = ClientDTO.class,
                    columns = {
                        @ColumnResult(name = "id", type = Long.class)
                        ,
                                @ColumnResult(name = "codeClient", type = String.class)
                        ,
                                @ColumnResult(name = "personneId", type = Long.class)
                        ,
                                @ColumnResult(name = "profilId", type = Long.class)
                        ,
                                @ColumnResult(name = "profilActeurId", type = Long.class)
                        ,
                                @ColumnResult(name = "groupeProfilActeurId", type = Long.class)
                        ,
                                @ColumnResult(name = "clientValide", type = Boolean.class)
                        ,
                                @ColumnResult(name = "reseauId", type = Long.class)
                        ,
                                @ColumnResult(name = "dateValidation", type = LocalDate.class)
                        ,
                                @ColumnResult(name = "userValide", type = String.class),}))
})

@NamedNativeQueries({
    @NamedNativeQuery(name = "Client.findPersonneById",
            query = "select id, code_client codeClient, personne_id personneId, profil_id profilId, profil_acteur_id profilActeurId, groupe_profil_acteur_id groupeProfilActeurId \n"
            + " ,client_valide clientValide,reseau_id as reseauId from client where supprime= false and personne_id = :personneId",
            resultSetMapping = "Mapping.ClientDTO1", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})
    ,
@NamedNativeQuery(name = "Client.findByClient",
            query = "select id, code_client codeClient, personne_id personneId, profil_id profilId, profil_acteur_id profilActeurId, groupe_profil_acteur_id groupeProfilActeurId \n"
            + " ,client_valide clientValide from client where supprime= false and id = :id",
            resultSetMapping = "Mapping.ClientDTO1", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})
    ,
        @NamedNativeQuery(name = "Client.findClientPhysiqueByCriteria",
            query = "select c.id, c.code_client as codeClient, c.client_valide as clientValide, c.client_identifie as clientIdentifie,"
            + "c.personne_id as personneId, c.profil_id as profilId, c.profil_acteur_id as profilActeurId,"
            + "c.groupe_profil_acteur_id as groupeProfilActeurId,"
            + "c.created_date as createdDate, c.created_by as createdBy, c.last_modified_by as lastModifiedBy, c.last_modified_date as lastModifiedDate, c.unite_organisation_createur_id as uniteOrganisationCreateurId,c.reseau_id as reseauId"
            + " FROM client c"
            + " INNER JOIN personne p on c.personne_id=p.id"
            + " INNER JOIN personne_physique pp on p.personne_physique_id=pp.id"
            + " WHERE ("
            + "(:code is null or p.code_telephone= CAST (:code AS TEXT))"
            + "and (:telephone is null or UPPER(p.telephone) like  UPPER('%'||CAST (:telephone AS TEXT)||'%'))"
            + "and (:adresse is null or UPPER(p.adresse) like   UPPER('%'||CAST (:adresse AS TEXT)||'%'))"
            + "and (:nom is null or UPPER(pp.nom_client) like  UPPER('%'||CAST (:nom AS TEXT)||'%'))"
            + "and (:prenom is null or UPPER(pp.prenom_client) like  UPPER('%'||CAST (:prenom AS TEXT)||'%'))"
            + " and (c.supprime is null or c.supprime=false)) order by c.id desc limit :taille offset :page",
            resultSetMapping = "Mapping.ClientDTO", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})
    ,

        @NamedNativeQuery(name = "Client.findClientMoralByCriteria",
            query = "select c.id, c.code_client as codeClient, c.client_valide as clientValide, c.client_identifie as clientIdentifie,"
            + "c.personne_id as personneId, c.profil_id as profilId, c.profil_acteur_id as profilActeurId,"
            + "c.groupe_profil_acteur_id as groupeProfilActeurId,"
            + "c.created_date as createdDate, c.created_by as createdBy, c.last_modified_by as lastModifiedBy, c.last_modified_date as lastModifiedDate,c.unite_organisation_createur_id as uniteOrganisationCreateurId,c.reseau_id as reseauId"
            + " FROM client c "
            + "INNER JOIN personne p on c.personne_id=p.id"
            + " INNER JOIN personne_morale pm on p.personne_morale_id=pm.id"
            + " WHERE ("
            + "(:code is null or p.code_telephone= CAST (:code AS TEXT))"
            + "and (:telephone is null or UPPER(p.telephone) like  UPPER('%'||CAST (:telephone AS TEXT)||'%'))"
            + "and (:adresse is null or UPPER(p.adresse) like   UPPER('%'||CAST (:adresse AS TEXT)||'%'))"
            + "and (:raisonSociale is null or UPPER(pm.raison_sociale) like UPPER('%'||CAST (:raisonSociale AS TEXT)||'%'))"
            + "and (c.supprime is null or c.supprime=false)) order by c.id limit :taille offset :page",
            resultSetMapping = "Mapping.ClientDTO", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})
    ,

            @NamedNativeQuery(name = "Client.findClientMoralPersonne",
            query = "select c.id, c.code_client as codeClient, c.client_valide as clientValide, c.client_identifie as clientIdentifie,"
            + "c.personne_id as personneId, c.profil_id as profilId, c.profil_acteur_id as profilActeurId,"
            + "c.groupe_profil_acteur_id as groupeProfilActeurId,"
            + "c.created_date as createdDate, c.created_by as createdBy, c.last_modified_by as lastModifiedBy, c.last_modified_date as lastModifiedDate,c.unite_organisation_createur_id as uniteOrganisationCreateurId,c.reseau_id as reseauId"
            + " FROM client c "
            + "INNER JOIN personne p on c.personne_id=p.id"
            + " INNER JOIN personne_morale pm on p.personne_morale_id=pm.id"
            + " WHERE ("
            + " (c.supprime is null or c.supprime=false)) order by c.id ",
            resultSetMapping = "Mapping.ClientDTO", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})
    ,
        

        
        @NamedNativeQuery(name = "Client.findClientByPersonneId",
            query = "select c.id, c.code_client as codeClient, c.client_valide as clientValide, c.client_identifie as clientIdentifie,"
            + "c.personne_id as personneId, c.profil_id as profilId, c.profil_acteur_id as profilActeurId,"
            + "c.groupe_profil_acteur_id as groupeProfilActeurId,"
            + "c.created_date as createdDate, c.created_by as createdBy, c.last_modified_by as lastModifiedBy, c.last_modified_date as lastModifiedDate, c.unite_organisation_createur_id as uniteOrganisationCreateurId"
            + " FROM client c where c.personne_id= CAST (CAST (:personneId as text) as BIGINT) and c.supprime=false",
            resultSetMapping = "Mapping.ClientDTO", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})
    ,

        @NamedNativeQuery(name = "Client.findClientMoralReportByCriteriaDate",
            query = "select c.id, c.code_client as codeClient, c.client_valide as clientValide, c.client_identifie as clientIdentifie,"
            + "c.personne_id as personneId, c.profil_id as profilId, c.profil_acteur_id as profilActeurId,"
            + "c.groupe_profil_acteur_id as groupeProfilActeurId,"
            + "c.created_date as createdDate, c.created_by as createdBy, c.last_modified_by as lastModifiedBy, c.last_modified_date as lastModifiedDate,c.unite_organisation_createur_id as uniteOrganisationCreateurId,c.reseau_id as reseauId"
            + " FROM client c "
            + "INNER JOIN personne p on c.personne_id=p.id"
            + " INNER JOIN personne_morale pm on p.personne_morale_id=pm.id"
            + " WHERE ("
            + "(:code is null or p.code_telephone= CAST (:code AS TEXT))"
            + "and (:telephone is null or UPPER(p.telephone) like  UPPER('%'||CAST (:telephone AS TEXT)||'%'))"
            + "and (:adresse is null or UPPER(p.adresse) like   UPPER('%'||CAST (:adresse AS TEXT)||'%'))"
            + "and (:raisonSociale is null or UPPER(pm.raison_sociale) like UPPER('%'||CAST (:raisonSociale AS TEXT)||'%'))"
            + "and (CAST(CAST(:dateDebut as text) as timestamp) is null or c.created_date between CAST(CAST(:dateDebut as text) as timestamp) and CAST(CAST(:dateFin as text) as timestamp))"
            + "and (c.supprime is null or c.supprime=false)) order by c.id desc limit :taille offset :page",
            resultSetMapping = "Mapping.ClientDTO", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})
    ,

        @NamedNativeQuery(name = "Client.findClientPhysiqueReportByCriteriaDate",
            query = "select c.id, c.code_client as codeClient, c.client_valide as clientValide, c.client_identifie as clientIdentifie,"
            + "c.personne_id as personneId, c.profil_id as profilId, c.profil_acteur_id as profilActeurId,"
            + "c.groupe_profil_acteur_id as groupeProfilActeurId,"
            + "c.created_date as createdDate, c.created_by as createdBy, c.last_modified_by as lastModifiedBy, c.last_modified_date as lastModifiedDate, c.unite_organisation_createur_id as uniteOrganisationCreateurId,c.reseau_id as reseauId"
            + " FROM client c"
            + " INNER JOIN personne p on c.personne_id=p.id"
            + " INNER JOIN personne_physique pp on p.personne_physique_id=pp.id"
            + " WHERE ("
            + "(:code is null or p.code_telephone= CAST (:code AS TEXT))"
            + "and (:telephone is null or UPPER(p.telephone) like  UPPER('%'||CAST (:telephone AS TEXT)||'%'))"
            + "and (:adresse is null or UPPER(p.adresse) like   UPPER('%'||CAST (:adresse AS TEXT)||'%'))"
            + "and (:nom is null or UPPER(pp.nom_client) like  UPPER('%'||CAST (:nom AS TEXT)||'%'))"
            + "and (:prenom is null or UPPER(pp.prenom_client) like  UPPER('%'||CAST (:prenom AS TEXT)||'%'))"
            + " and ( CAST(CAST(:dateDebut as text) as timestamp) is null or c.created_date between CAST (CAST(:dateDebut as text) as timestamp) and CAST(CAST(:dateFin as text) as timestamp)) "
            + "and (c.supprime is null or c.supprime=false)) order by c.id desc limit :taille offset :page",
            resultSetMapping = "Mapping.ClientDTO", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})
    ,

        @NamedNativeQuery(name = "Client.findAllClientByEtat",
            query = "select c.id, c.code_client as codeClient, c.client_valide as clientValide, c.client_identifie as clientIdentifie,"
            + "c.personne_id as personneId, c.profil_id as profilId, c.profil_acteur_id as profilActeurId,"
            + "c.groupe_profil_acteur_id as groupeProfilActeurId,"
            + "c.created_date as createdDate, c.created_by as createdBy, c.last_modified_by as lastModifiedBy, c.last_modified_date as lastModifiedDate, c.unite_organisation_createur_id as uniteOrganisationCreateurId"
            + " FROM client c"
            + " WHERE ("
            + "(c.client_valide is null or c.client_valide=:valide) and (c.supprime is null or c.supprime=false)) order by c.id desc ",
            resultSetMapping = "Mapping.ClientDTO")
    ,
        @NamedNativeQuery(name = "Client.findAllClientRegulation",
            query = "select distinct c.id, c.code_client as codeClient, c.client_valide as clientValide, c.client_identifie as clientIdentifie,"
            + "c.personne_id as personneId, c.profil_id as profilId, c.profil_acteur_id as profilActeurId,"
            + "c.groupe_profil_acteur_id as groupeProfilActeurId,"
            + "c.created_date as createdDate, c.created_by as createdBy, c.last_modified_by as lastModifiedBy, c.last_modified_date as lastModifiedDate, c.unite_organisation_createur_id as uniteOrganisationCreateurId,c.reseau_id as reseauId"
            + " From client c inner join personne per on c.personne_id=per.id inner join personne_physique p "
            + "on per.personne_physique_id=p.id  inner join compte cpt on cpt.client_id=c.id where ("
            + "(:nom is null or p.nom_client like ('%'||CAST (:nom AS TEXT)||'%') ) and (:telephone is null  or per.telephone like ('%'||CAST (:telephone AS TEXT)||'%'))"
            + "and (:numeroCpt is null or cpt.valeur_compte like ('%'||CAST (:numeroCpt AS TEXT)||'%') ) )", resultSetMapping = "Mapping.ClientDTO", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})
    ,


        @NamedNativeQuery(name = "Client.findClientByClientId",
            query = "select c.id, c.code_client as codeClient, c.client_valide as clientValide, c.client_identifie as clientIdentifie,"
            + "c.personne_id as personneId, c.profil_id as profilId, c.profil_acteur_id as profilActeurId,"
            + "c.groupe_profil_acteur_id as groupeProfilActeurId,"
            + "c.created_date as createdDate, c.created_by as createdBy, c.last_modified_by as lastModifiedBy, c.last_modified_date as lastModifiedDate, c.unite_organisation_createur_id as uniteOrganisationCreateurId,c.reseau_id as reseauId"
            + " FROM client c where c.id= CAST (CAST (:clientId as text) as BIGINT)",
            resultSetMapping = "Mapping.ClientDTO", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})
    ,

        @NamedNativeQuery(name = "Client.findAllClient1",
            query = "select c.id, c.code_client as codeClient, c.client_valide as clientValide, c.client_identifie as clientIdentifie,"
            + "c.personne_id as personneId, c.profil_id as profilId, c.profil_acteur_id as profilActeurId,"
            + "c.groupe_profil_acteur_id as groupeProfilActeurId,"
            + "c.created_date as createdDate, c.created_by as createdBy, c.last_modified_by as lastModifiedBy, c.last_modified_date as lastModifiedDate, c.unite_organisation_createur_id as uniteOrganisationCreateurId"
            + " FROM client c"
            + " INNER JOIN personne p on c.personne_id=p.id"
            + " INNER JOIN personne_physique pp on p.personne_physique_id=pp.id"
            + " WHERE ("
            + "  c.client_valide=:clientValide and  c.supprime=false) order by c.id asc",
            resultSetMapping = "Mapping.ClientDTO", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})
    ,

        @NamedNativeQuery(name = "Client.findAllClient2",
            query = "select c.id, c.code_client as codeClient, c.client_valide as clientValide, c.client_identifie as clientIdentifie,"
            + "c.personne_id as personneId, c.profil_id as profilId, c.profil_acteur_id as profilActeurId,"
            + "c.groupe_profil_acteur_id as groupeProfilActeurId,"
            + "c.created_date as createdDate, c.created_by as createdBy, c.last_modified_by as lastModifiedBy, c.last_modified_date as lastModifiedDate, c.unite_organisation_createur_id as uniteOrganisationCreateurId,c.reseau_id as reseauId"
            + " FROM client c"
            + " INNER JOIN personne p on c.personne_id=p.id"
            + " INNER JOIN personne_physique pp on p.personne_physique_id=pp.id"
            + " WHERE ("
            + "  (c.client_valide=:clientValide or  c.client_valide is null) and  c.supprime=false) order by c.id asc",
            resultSetMapping = "Mapping.ClientDTO", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})
    ,

        @NamedNativeQuery(name = "Client.clientIdentifier",
            query = "select id, code_client codeClient, personne_id personneId, profil_id profilId, profil_acteur_id profilActeurId, groupe_profil_acteur_id groupeProfilActeurId \n"
            + " ,client_valide clientValide,reseau_id as reseauId,c.date_validation as dateValidation,c.user_valide as userValide from client c where c.supprime= false and  (CAST(CAST(:dateDebut as text) as timestamp) is null or c.date_validation between CAST(CAST(:dateDebut as text) as timestamp) and CAST(CAST(:dateFin as text) as timestamp))",
            resultSetMapping = "Mapping.ClientDTO2", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")}),
        
        @NamedNativeQuery(name = "Client.findClientPhysiqueByCarteCriteria",
                query = "select c.id, c.code_client as codeClient, c.client_valide as clientValide, c.client_identifie as clientIdentifie,"
                        + "c.personne_id as personneId, c.profil_id as profilId, c.profil_acteur_id as profilActeurId,"
                        + "c.groupe_profil_acteur_id as groupeProfilActeurId,"
                        + "c.created_date as createdDate, c.created_by as createdBy, c.last_modified_by as lastModifiedBy, c.last_modified_date as lastModifiedDate, c.unite_organisation_createur_id as uniteOrganisationCreateurId,c.reseau_id as reseauId"
                        + " FROM client c"
                        + " INNER JOIN personne p on c.personne_id=p.id"
                        + " INNER JOIN personne_physique pp on p.personne_physique_id=pp.id"
                        + " INNER JOIN compte cpt on cpt.client_id=c.id"
                        + " INNER JOIN carte ca on cpt.carte_id=ca.id "
                        + " WHERE ("
                        + "(:code is null or p.code_telephone= CAST (:code AS TEXT))"
                        + "and (:telephone is null or UPPER(p.telephone) like  UPPER('%'||CAST (:telephone AS TEXT)||'%'))"
                        + "and (:adresse is null or UPPER(p.adresse) like   UPPER('%'||CAST (:adresse AS TEXT)||'%'))"
                        + "and (:nom is null or UPPER(pp.nom_client) like  UPPER('%'||CAST (:nom AS TEXT)||'%'))"
                        + "and (:prenom is null or UPPER(pp.prenom_client) like  UPPER('%'||CAST (:prenom AS TEXT)||'%'))"
                        + "and (:cardNumber is null or CAST(:cardNumber AS TEXT) = '' or UPPER(cpt.valeur_compte) like  UPPER('%'||CAST (:cardNumber AS TEXT)||'%'))"
                        + "and (:last4Digit is null or CAST(:last4Digit AS TEXT) = '' or ca.last_digit = CAST (:last4Digit AS TEXT))"
                        + " and (c.supprime is null or c.supprime=false)) order by c.id desc limit :taille offset :page",
                resultSetMapping = "Mapping.ClientDTO", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})
    ,

        @NamedNativeQuery(name = "Client.findClientPhysiqueByWalletCriteria",
                query = "select distinct c.id, c.code_client as codeClient, c.client_valide as clientValide, c.client_identifie as clientIdentifie,"
                        + "c.personne_id as personneId, c.profil_id as profilId, c.profil_acteur_id as profilActeurId,"
                        + "c.groupe_profil_acteur_id as groupeProfilActeurId,"
                        + "c.created_date as createdDate, c.created_by as createdBy, c.last_modified_by as lastModifiedBy, c.last_modified_date as lastModifiedDate, c.unite_organisation_createur_id as uniteOrganisationCreateurId,c.reseau_id as reseauId"
                        + " FROM client c"
                        + " INNER JOIN personne p on c.personne_id=p.id"
                        + " INNER JOIN personne_physique pp on p.personne_physique_id=pp.id"
                        + " INNER JOIN compte cpt on cpt.client_id=c.id"
                        + " WHERE ("
                        + "(:code is null or p.code_telephone= CAST (:code AS TEXT))"
                        + "and (:telephone is null or UPPER(p.telephone) like  UPPER('%'||CAST (:telephone AS TEXT)||'%'))"
                        + "and (:adresse is null or UPPER(p.adresse) like   UPPER('%'||CAST (:adresse AS TEXT)||'%'))"
                        + "and (:nom is null or UPPER(pp.nom_client) like  UPPER('%'||CAST (:nom AS TEXT)||'%'))"
                        + "and (:prenom is null or UPPER(pp.prenom_client) like  UPPER('%'||CAST (:prenom AS TEXT)||'%'))"
                        + "and (:walletNumber is null or UPPER(cpt.valeur_compte) like  UPPER('%'||CAST (:walletNumber AS TEXT)||'%'))"
                        + " and (c.supprime is null or c.supprime=false)) order by c.id desc limit :taille offset :page",
                resultSetMapping = "Mapping.ClientDTO", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})

})

@Entity
@Table(name = "client")
@Cacheable(value = true)
@EntityListeners(value = {EntityAuditEventListener.class})
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Client extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code_client")
    private String codeClient;

    @Column(name = "client_valide")
    private Boolean clientValide;

    @Column(name = "supprime")
    private Boolean supprime = false;

    @Column(name = "client_identifie")
    private Boolean clientIdentifie = false;

    @OneToOne
    @JoinColumn(unique = false)
    private Personne personne;

    @ManyToOne
    private ServiceProfil profil;

    @ManyToOne
    private UniteOrganisation uniteOrganisationCreateur;

    @Column(name = "idt_id")
    private Integer tagIdentityRef;

    @Column(name = "cli_id")
    private Integer cliRef;

    @Column(name = "dalais_regularisation")
    private LocalDate dalaisRegularisation;

    @Column(name = "date_validation")
    private LocalDate dateValidation;

    @Column(name = "user_valide")
    private String userValide;
    @Column(name = "client_externe")
    private Integer clientExterne;
    @ManyToOne
    private ProfilActeur profilActeur;

    @ManyToOne
    private GroupeProfilActeur groupeProfilActeur;

    @OneToOne
    private UniteOrganisation reseau;
    
    @ManyToOne
    @JoinColumn(name = "info_supp")
    private InfoSupp infoSupp;

    @OneToMany(mappedBy = "titulaire")
    @JsonIgnore
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<Document> documents = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodeClient() {
        return codeClient;
    }

    public Client codeClient(String codeClient) {
        this.codeClient = codeClient;
        return this;
    }

    public Integer getTagIdentityRef() {
        return tagIdentityRef;
    }

    public void setTagIdentityRef(Integer tagIdentityRef) {
        this.tagIdentityRef = tagIdentityRef;
    }

    public Integer getCliRef() {
        return cliRef;
    }

    public void setCliRef(Integer cliRef) {
        this.cliRef = cliRef;
    }

    public void setCodeClient(String codeClient) {
        this.codeClient = codeClient;
    }

    public Boolean getClientValide() {
        return clientValide;
    }

    public void setClientValide(Boolean clientValide) {
        this.clientValide = clientValide;
    }

    public Client clientValide(Boolean clientValide) {
        this.clientValide = clientValide;
        return this;
    }

    public ServiceProfil getProfil() {
        return profil;
    }

    public void setProfil(ServiceProfil profil) {
        this.profil = profil;
    }

    public Client profil(ServiceProfil profil) {
        this.profil = profil;
        return this;
    }

    public Personne getPersonne() {
        return personne;
    }

    public Client personne(Personne personne) {
        this.personne = personne;
        return this;
    }

    public void setPersonne(Personne personne) {
        this.personne = personne;
    }

    public LocalDate getDateValidation() {
        return dateValidation;
    }

    public void setDateValidation(LocalDate dateValidation) {
        this.dateValidation = dateValidation;
    }

    public Set<Document> getDocuments() {
        return documents;
    }

    public void setDocuments(Set<Document> documents) {
        this.documents = documents;
    }

    public ProfilActeur getProfilActeur() {
        return profilActeur;
    }

    public void setProfilActeur(ProfilActeur profilActeur) {
        this.profilActeur = profilActeur;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove
    public Boolean getClientIdentifie() {
        return clientIdentifie;
    }

    public void setClientIdentifie(Boolean clientIdentifie) {
        this.clientIdentifie = clientIdentifie;
    }

    public GroupeProfilActeur getGroupeProfilActeur() {
        return groupeProfilActeur;
    }

    public void setGroupeProfilActeur(GroupeProfilActeur groupeProfilActeur) {
        this.groupeProfilActeur = groupeProfilActeur;
    }

    public Boolean getSupprime() {
        return supprime;
    }

    public void setSupprime(Boolean supprime) {
        this.supprime = supprime;
    }

    public UniteOrganisation getUniteOrganisationCreateur() {
        return uniteOrganisationCreateur;
    }

    public void setUniteOrganisationCreateur(UniteOrganisation uniteOrganisationCreateur) {
        this.uniteOrganisationCreateur = uniteOrganisationCreateur;
    }

    public LocalDate getDalaisRegularisation() {
        return dalaisRegularisation;
    }

    public void setDalaisRegularisation(LocalDate dalaisRegularisation) {
        this.dalaisRegularisation = dalaisRegularisation;
    }

    public String getUserValide() {
        return userValide;
    }

    public void setUserValide(String userValide) {
        this.userValide = userValide;
    }

    public UniteOrganisation getReseau() {
        return reseau;
    }

    public void setReseau(UniteOrganisation reseau) {
        this.reseau = reseau;
    }

    public Integer getClientExterne() {
        return clientExterne;
    }

    public void setClientExterne(Integer clientExterne) {
        this.clientExterne = clientExterne;
    }

    public InfoSupp getInfoSupp() {
        return infoSupp;
    }

    public void setInfoSupp(InfoSupp infoSupp) {
        this.infoSupp = infoSupp;
    }
    

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Client client = (Client) o;
        if (client.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), client.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "Client{"
                + "id=" + getId()
                + ", codeClient='" + getCodeClient() + "'"
                + "}";
    }
}
