package com.m2i.pti.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.m2i.pti.config.audit.EntityAuditEventListener;
import com.m2i.pti.domain.enumeration.*;
import com.m2i.pti.service.dto.CompteDTO;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

@NamedNativeQueries({
    @NamedNativeQuery(name = "Compte.findTopByValeurCompteAndEtiquette",
            query = "select id , etiquette,valeur_compte valeurCompte,  libelle_compte libelleCompte ,categorie_compte categorieCompte,  classe_compte classeCompte,compte_pincipale_id comptePincipaleId, "
            + "first_categorie_compte firstCategorieCompte ,compte_banque_id compteBanqueId,compte_autre_id compteAutreId,  client_id clientId,  "
            + "  type_compte_id typeCompteId, "
            + "  unite_organisation_id uniteOrganisationId,etat_compte etatCompte,statut_compte statutCompte,numero_court numeroCourt    from compte where valeur_compte=:valeur and etiquette= :etiquette and etat_compte='OUVERT' and statut_compte='ACTIVE' and client_id is not null ",
            resultSetMapping = "Mapping.CompteDTO", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})
    ,
@NamedNativeQuery(name = "Compte.findAllCompteClientOptimise",
            query = "select cpt.id , etiquette,valeur_compte valeurCompte,  libelle_compte libelleCompte ,categorie_compte categorieCompte,  classe_compte classeCompte,compte_pincipale_id comptePincipaleId, "
            + "first_categorie_compte firstCategorieCompte ,compte_banque_id compteBanqueId,compte_autre_id compteAutreId,  client_id clientId,  "
            + "  type_compte_id typeCompteId, "
            + "  unite_organisation_id uniteOrganisationId,etat_compte etatCompte,statut_compte statutCompte,numero_court numeroCourt  from compte cpt inner join client c on cpt.client_id=c.id"
            + " inner join compte_autre ca on cpt.compte_autre_id=ca.id where cpt.valeur_compte is not null and ca.code_fse=:codeFse ",
            resultSetMapping = "Mapping.CompteDTO", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})
    ,


 @NamedNativeQuery(name = "Compte.findTopByUniteOrganisationAndEtiquette",
            query = "select id , etiquette,valeur_compte valeurCompte,  libelle_compte libelleCompte ,categorie_compte categorieCompte,  classe_compte classeCompte,compte_pincipale_id comptePincipaleId, "
            + " first_categorie_compte firstCategorieCompte ,compte_banque_id compteBanqueId,compte_autre_id compteAutreId,  client_id clientId,  "
            + "  type_compte_id typeCompteId, "
            + "  unite_organisation_id uniteOrganisationId,etat_compte etatCompte,statut_compte statutCompte,numero_court numeroCourt    from compte where unite_organisation_id=:uoId and etiquette= :etiquette ",
            resultSetMapping = "Mapping.CompteDTO", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})
    ,
 @NamedNativeQuery(name = "Compte.findByValeurCompteAndCodeFse",
            query = "  select c.id , etiquette,valeur_compte valeurCompte,  libelle_compte libelleCompte ,categorie_compte categorieCompte, "
            + " classe_compte classeCompte,compte_pincipale_id comptePincipaleId, "
            + "            first_categorie_compte firstCategorieCompte ,compte_banque_id compteBanqueId,compte_autre_id compteAutreId,  client_id clientId,  "
            + "              type_compte_id typeCompteId, "
            + "             unite_organisation_id uniteOrganisationId,etat_compte etatCompte,statut_compte statutCompte,numero_court numeroCourt    "
            + "             from compte c, compte_autre ca  where "
            + "            valeur_compte=:valeur and ca.code_fse=:codeFse and c.compte_autre_id=ca.id ",
            resultSetMapping = "Mapping.CompteDTO", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})
    ,
    @NamedNativeQuery(name = "Compte.findTopByValeurCompteBanqueAndEtiquette",
            query = "select id , etiquette,valeur_compte valeurCompte,  libelle_compte libelleCompte ,categorie_compte categorieCompte,  classe_compte classeCompte,compte_pincipale_id comptePincipaleId, "
            + " first_categorie_compte firstCategorieCompte ,compte_banque_id compteBanqueId,compte_autre_id compteAutreId,  client_id clientId,  "
            + "  type_compte_id typeCompteId, "
            + "  unite_organisation_id uniteOrganisationId,etat_compte etatCompte,statut_compte statutCompte,numero_court numeroCourt    "
            + " from compte c, compte_banque cb where cb.id= ca.compte_banque_id and code_banque =:codeBanque "
            + " and code_fse=:teneur and code_agence=:codeAgence and valeur_compte=:valeur and etiquette= :etiquette ",
            resultSetMapping = "Mapping.CompteDTO", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})
    ,
    
    @NamedNativeQuery(name = "Compte.findByClasseAndClientId",
            query = "select c.id , etiquette,valeur_compte valeurCompte,  libelle_compte libelleCompte ,categorie_compte categorieCompte,  classe_compte classeCompte,compte_pincipale_id comptePincipaleId, "
            + " first_categorie_compte firstCategorieCompte ,compte_banque_id compteBanqueId,compte_autre_id compteAutreId,  client_id clientId,  "
            + "  type_compte_id typeCompteId, "
            + "  unite_organisation_id uniteOrganisationId,etat_compte etatCompte,statut_compte statutCompte,numero_court numeroCourt, solde soldeCompte, code_fse codeFse    from compte c , compte_autre ca  where c.compte_autre_id=ca.id and  client_id=:clientId and classe_compte= :classe  and statut_compte='ACTIVE' ",
            resultSetMapping = "Mapping.CompteDTOSOLDE", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})
    ,

@NamedNativeQuery(name = "Compte.findTopByValeurCompte",
            query = "select id , etiquette,valeur_compte valeurCompte,  libelle_compte libelleCompte ,categorie_compte categorieCompte,  classe_compte classeCompte,compte_pincipale_id comptePincipaleId, "
            + " first_categorie_compte firstCategorieCompte ,compte_banque_id compteBanqueId,compte_autre_id compteAutreId,  client_id clientId,  "
            + "  type_compte_id typeCompteId, "
            + "  unite_organisation_id uniteOrganisationId,etat_compte etatCompte,statut_compte statutCompte,numero_court numeroCourt  from compte where valeur_compte=:valeur ",
            resultSetMapping = "Mapping.CompteDTO", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})

    , 
@NamedNativeQuery(name = "Compte.findByValeurCompteAndClientId",
            query = "select id , etiquette,valeur_compte valeurCompte,  libelle_compte libelleCompte ,categorie_compte categorieCompte,  classe_compte classeCompte,compte_pincipale_id comptePincipaleId, "
            + " first_categorie_compte firstCategorieCompte ,compte_banque_id compteBanqueId,compte_autre_id compteAutreId,  client_id clientId,  "
            + "  type_compte_id typeCompteId, "
            + "  unite_organisation_id uniteOrganisationId,etat_compte etatCompte,statut_compte statutCompte,numero_court numeroCourt  from compte where valeur_compte=:valeur  and client_id=:clientId  and coalesce(statut_compte,'ACTIVE')='ACTIVE'",
            resultSetMapping = "Mapping.CompteDTO", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})
    , 

@NamedNativeQuery(name = "Compte.findAccountClient",
            query = "select id , etiquette,valeur_compte valeurCompte,  libelle_compte libelleCompte ,categorie_compte categorieCompte,  classe_compte classeCompte,compte_pincipale_id comptePincipaleId, "
            + " first_categorie_compte firstCategorieCompte ,compte_banque_id compteBanqueId,compte_autre_id compteAutreId,  client_id clientId,  "
            + "  type_compte_id typeCompteId, "
            + "  unite_organisation_id uniteOrganisationId,etat_compte etatCompte,statut_compte statutCompte,numero_court numeroCourt  from compte where client_id=:clientId and statut_compte='ACTIVE' ",
            resultSetMapping = "Mapping.CompteDTO", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})
    ,
@NamedNativeQuery(name = "Compte.findCompteWithValeurAndCodeFse",
            query = "select c.id , etiquette,valeur_compte valeurCompte,libelle_compte libelleCompte ,"
            + "categorie_compte categorieCompte,  classe_compte classeCompte,compte_pincipale_id comptePincipaleId, "
            + " first_categorie_compte firstCategorieCompte ,compte_banque_id compteBanqueId,compte_autre_id compteAutreId,  client_id clientId,  "
            + "  type_compte_id typeCompteId, "
            + "  unite_organisation_id uniteOrganisationId,etat_compte etatCompte,statut_compte statutCompte,"
            + "numero_court numeroCourt, solde soldeCompte, code_fse codeFse  from compte c, compte_autre ca where ca.id= c.compte_autre_id and coalesce(ca.code_fse,:codeFse)=:codeFse and valeur_compte=:valeurCompte   and coalesce(statut_compte,'ACTIVE')='ACTIVE'  ",
            resultSetMapping = "Mapping.CompteDTOSOLDE", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})
    ,
@NamedNativeQuery(name = "Compte.findOneById",
            query = "select c.id , etiquette,valeur_compte valeurCompte,libelle_compte libelleCompte ,"
            + "categorie_compte categorieCompte,  classe_compte classeCompte,compte_pincipale_id comptePincipaleId, "
            + " first_categorie_compte firstCategorieCompte ,compte_banque_id compteBanqueId,compte_autre_id compteAutreId,  client_id clientId,  "
            + "  type_compte_id typeCompteId, "
            + "  unite_organisation_id uniteOrganisationId,etat_compte etatCompte,statut_compte statutCompte,"
            + "numero_court numeroCourt, solde soldeCompte, code_fse codeFse  from compte c, compte_autre ca where ca.id= c.compte_autre_id and c.id=:id  and coalesce(statut_compte,'ACTIVE')='ACTIVE'  ",
            resultSetMapping = "Mapping.CompteDTOSOLDE", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})
    ,
        @NamedNativeQuery(name = "Compte.findBanqueOneById",
            query = "select c.id , etiquette,valeur_compte valeurCompte,libelle_compte libelleCompte ,"
            + "categorie_compte categorieCompte,  classe_compte classeCompte,compte_pincipale_id comptePincipaleId, "
            + " first_categorie_compte firstCategorieCompte ,compte_banque_id compteBanqueId,compte_autre_id compteAutreId,  client_id clientId,  "
            + "  type_compte_id typeCompteId, "
            + "  unite_organisation_id uniteOrganisationId,etat_compte etatCompte,statut_compte statutCompte,"
            + "numero_court numeroCourt, code_fse codeFse  from compte c, compte_banque cb where cb.id= c.compte_banque_id and c.id=:id  and coalesce(statut_compte,'ACTIVE')='ACTIVE'  ",
            resultSetMapping = "Mapping.CompteDTOBANQUE", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})
    ,
         @NamedNativeQuery(name = "Compte.findCarteOneById",
            query = "select c.id , etiquette,valeur_compte valeurCompte,libelle_compte libelleCompte ,"
            + " categorie_compte categorieCompte,  "
            + "  carte_id carteId, client_id clientId,  "
            + "  type_compte_id typeCompteId, "
            + "  unite_organisation_id uniteOrganisationId,etat_compte etatCompte,statut_compte statutCompte,"
            + " code_fse codeFse ,last_digit last4Digit from compte c, carte cb where cb.id= c.carte_id and c.id=:id  and coalesce(statut_compte,'ACTIVE')='ACTIVE'  ",
            resultSetMapping = "Mapping.CompteDTOCARTE", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})
    ,
        @NamedNativeQuery(name = "Compte.findByClientIdOptimise",
            query = "select c.id , etiquette, c.valeur_compte as valeurCompte, c.libelle_compte as libelleCompte, c.categorie_compte as categorieCompte, c.classe_compte as classeCompte, c.compte_pincipale_id as comptePincipaleId, "
            + "c.first_categorie_compte as firstCategorieCompte ,c.compte_banque_id as compteBanqueId, c.compte_autre_id as compteAutreId, c.client_id as clientId, "
            + "c.type_compte_id as typeCompteId, tc.libelle_type_compte as typeCompteLibelleTypeCompte,"
            + "c.unite_organisation_id as uniteOrganisationId, uo.libelle_unite_organisation as uniteOrganisationLibelleUniteOrganisation, c.etat_compte as etatCompte, c.statut_compte as statutCompte, c.numero_court as numeroCourt"
            + " from compte c "
            + "LEFT JOIN type_compte tc on c.type_compte_id=tc.id "
            + "LEFT JOIN unite_organisation uo on c.unite_organisation_id=uo.id "
            + "where c.client_id= CAST (CAST (:clientId AS TEXT ) AS BIGINT) ",
            resultSetMapping = "Mapping.findByClientIdDTO", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})
    ,
        
        
        @NamedNativeQuery(name = "Compte.findCompteByClientIdAndClasseCompte",
            query = "select id ,valeur_compte as valeurCompte, compte_banque_id as compteBanqueId, compte_autre_id as compteAutreId,client_id as clientId,unite_organisation_id as uniteOrganisationId"
            + " from compte where client_id= CAST (CAST (:clientId AS TEXT) AS BIGINT) and classe_compte= CAST (:classeCompte AS TEXT)",
            resultSetMapping = "Mapping.findCompteByIdReportDTO", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})
    ,

  @NamedNativeQuery(name = "Compte.findCompteByIdReport",
            query = "select id ,valeur_compte as valeurCompte, compte_banque_id as compteBanqueId, compte_autre_id as compteAutreId,client_id as clientId,unite_organisation_id as uniteOrganisationId"
            + " from compte where id= CAST (CAST (:compteId AS TEXT) AS BIGINT)",
            resultSetMapping = "Mapping.findCompteByIdReportDTO", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})
    ,

  @NamedNativeQuery(name = "Compte.findByCompteAutreId",
            query = "select id, etiquette,valeur_compte valeurCompte,compte_pincipale_id as comptePincipaleId, compte_autre_id as compteAutreId,client_id as clientId,unite_organisation_id as uniteOrganisationId from compte where compte_autre_id = CAST (CAST (:compteAutreId AS TEXT) AS BIGINT)",
            resultSetMapping = "Mapping.findByCompteAutreIdDTO", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})
    ,

  @NamedNativeQuery(name = "Compte.findByUniteOrganisationOptimise",
            query = "select cpt.id , etiquette,valeur_compte valeurCompte,  libelle_compte libelleCompte ,categorie_compte categorieCompte,  classe_compte classeCompte,compte_pincipale_id comptePincipaleId, "
            + "first_categorie_compte firstCategorieCompte ,compte_banque_id compteBanqueId,compte_autre_id compteAutreId, "
            + "  type_compte_id typeCompteId, "
            + "  unite_organisation_id uniteOrganisationId,etat_compte etatCompte,statut_compte statutCompte,numero_court numeroCourt  from compte cpt "
            + " where cpt.unite_organisation_id=CAST (CAST (:uoId AS TEXT) AS BIGINT)  ",
            resultSetMapping = "Mapping.CompteUoDTO", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})
    ,

  @NamedNativeQuery(name = "Compte.findByUniteOrganisationWithoutPtiOptimise",
            query = "select cpt.id , etiquette,valeur_compte valeurCompte,  libelle_compte libelleCompte ,categorie_compte categorieCompte,  classe_compte classeCompte,compte_pincipale_id comptePincipaleId, "
            + "first_categorie_compte firstCategorieCompte ,compte_banque_id compteBanqueId,compte_autre_id compteAutreId, "
            + "  type_compte_id typeCompteId, "
            + "  unite_organisation_id uniteOrganisationId,etat_compte etatCompte,statut_compte statutCompte,numero_court numeroCourt  from compte cpt "
            + " where cpt.unite_organisation_id=CAST (CAST (:uoId AS TEXT) AS BIGINT) and cpt.etiquette not like '%PTI%' ",
            resultSetMapping = "Mapping.CompteUoDTO", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})
    ,

  @NamedNativeQuery(name = "Compte.findValeurCompteAndClientIdPeriode",
            query = "Select c.valeur_compte as valeurCompte, c.categorie_compte as categorieCompte, c.etat_compte as etatCompte, c.statut_compte as statutCompte, cli.id as clientId from compte as c inner join client as cli on c.client_id = cli.id"
            + " where CAST(CAST(:dateDebut as text) as timestamp) is null or c.created_date between CAST(CAST(:dateDebut AS text) AS timestamp) and CAST(CAST(:dateFin AS text) AS timestamp ) order by cli.id desc",
            resultSetMapping = "Mapping.findNumeroCompteAndClientIdDTO")
    ,

  @NamedNativeQuery(name = "Compte.findValeurCompteAndClientIdActifIn3months",
            query = "Select c.valeur_compte as valeurCompte, c.categorie_compte as categorieCompte, cli.id as clientId,ca.last_modified_date as lastModifieddate from compte as c inner join client as cli on c.client_id=cli.id inner join compte_autre ca on c.compte_autre_id=ca.id where c.categorie_compte='AUTRE'  and  coalesce(ca.last_modified_date,ca.date_derniere_transaction) between :dateDebut and :dateFin order by c.id desc limit :taille offset :page",
            resultSetMapping = "Mapping.findValeurCompteAndCategorieCompteAndClientIdDTODate")
    ,
  @NamedNativeQuery(name = "Compte.findAllCompteActif",
            query = "select c.id , etiquette,valeur_compte valeurCompte,libelle_compte libelleCompte ,"
            + "categorie_compte categorieCompte,  classe_compte classeCompte,compte_pincipale_id comptePincipaleId, "
            + " first_categorie_compte firstCategorieCompte ,compte_banque_id compteBanqueId,compte_autre_id compteAutreId,  client_id clientId,  "
            + "  type_compte_id typeCompteId, "
            + "  unite_organisation_id uniteOrganisationId,etat_compte etatCompte,statut_compte statutCompte,"
            + "numero_court numeroCourt, solde soldeCompte, code_fse codeFse  from compte c, compte_autre ca where ca.id= c.compte_autre_id   and statut_compte='ACTIVE' and client_id is not null order by c.id desc limit CAST (CAST (:limit  AS TEXT) AS BIGINT) OFFSET CAST (CAST (:offset  AS TEXT) AS BIGINT)  ",
            resultSetMapping = "Mapping.CompteDTOSOLDE", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})
    ,
@NamedNativeQuery(name = "Compte.findValeurCompteAndCategorieCompteAndClientIdPeriode",
            query = "Select c.valeur_compte as valeurCompte, c.categorie_compte as categorieCompte, cli.id as clientId from compte as c inner join client as cli on c.client_id = cli.id"
            + " where cast(:dateDebut as timestamp) is null or c.created_date between :dateDebut and :dateFin",
            resultSetMapping = "Mapping.findValeurCompteAndCategorieCompteAndClientIdDTO")
    ,

@NamedNativeQuery(name = "Compte.findValeurCompteAndCategorieCompteAndClientIdPeriode1",
            query = "Select c.valeur_compte as valeurCompte, c.categorie_compte as categorieCompte, cli.id as clientId from compte as c inner join client as cli on c.client_id = cli.id"
            + " where cast(:dateDebut as timestamp) is null or c.created_date >= :dateDebut",
            resultSetMapping = "Mapping.findValeurCompteAndCategorieCompteAndClientIdDTO")
    ,

@NamedNativeQuery(name = "Compte.findValeurCompteAndCategorieCompteAndClientIdPeriode2",
            query = "Select c.valeur_compte as valeurCompte, c.categorie_compte as categorieCompte, cli.id as clientId from compte as c inner join client as cli on c.client_id = cli.id"
            + " where cast(:dateFin as timestamp) is null or c.created_date <= :dateFin",
            resultSetMapping = "Mapping.findValeurCompteAndCategorieCompteAndClientIdDTO")
    ,

 @NamedNativeQuery(name = "Compte.findCptOpAndCptComByUoIdOpt",
            query = "select c.id , etiquette,valeur_compte valeurCompte,libelle_compte libelleCompte ,"
            + "categorie_compte categorieCompte,  classe_compte classeCompte,compte_pincipale_id comptePincipaleId, "
            + " first_categorie_compte firstCategorieCompte ,compte_banque_id compteBanqueId,compte_autre_id compteAutreId,  client_id clientId,  "
            + "  type_compte_id typeCompteId, "
            + "  unite_organisation_id uniteOrganisationId,etat_compte etatCompte,statut_compte statutCompte,"
            + "numero_court numeroCourt, solde soldeCompte, code_fse codeFse  from compte c, compte_autre ca where ca.id= c.compte_autre_id and c.unite_organisation_id=:uoId and c.etiquette in (:etiquettes)  ",
            resultSetMapping = "Mapping.CompteDTOSOLDE", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})
    ,

 @NamedNativeQuery(name = "Compte.findAllCptCliOptimise",
            query = "select cpt.id , etiquette,valeur_compte valeurCompte,  libelle_compte libelleCompte ,categorie_compte categorieCompte,  classe_compte classeCompte,compte_pincipale_id comptePincipaleId, "
            + "first_categorie_compte firstCategorieCompte ,compte_banque_id compteBanqueId,compte_autre_id compteAutreId,  client_id clientId,  "
            + "  type_compte_id typeCompteId, "
            + "  unite_organisation_id uniteOrganisationId,etat_compte etatCompte,statut_compte statutCompte,numero_court numeroCourt  from compte cpt inner join client c on cpt.client_id=c.id"
            + " inner join compte_autre ca on cpt.compte_autre_id=ca.id where cpt.valeur_compte is not null  ",
            resultSetMapping = "Mapping.CompteDTO", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})
    ,


 @NamedNativeQuery(name = "Compte.findAllCompteBanqueActif",
            query = "select cpt.id , etiquette,valeur_compte valeurCompte,  libelle_compte libelleCompte ,categorie_compte categorieCompte,  classe_compte classeCompte,compte_pincipale_id comptePincipaleId, "
            + "first_categorie_compte firstCategorieCompte ,compte_banque_id compteBanqueId,compte_autre_id compteAutreId,  client_id clientId,  "
            + "  type_compte_id typeCompteId, "
            + "  unite_organisation_id uniteOrganisationId,etat_compte etatCompte,statut_compte statutCompte,numero_court numeroCourt  from compte cpt inner join client c on cpt.client_id=c.id"
            + " inner join compte_banque cb on cpt.compte_banque_id=cb.id where cpt.statut_compte=CAST(:statut AS text)   and (cast(:dateDebut as timestamp) is null or cb.created_date between :dateDebut and :dateFin)  ",
            resultSetMapping = "Mapping.CompteDTO", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})
    ,


  @NamedNativeQuery(name = "Compte.findAllCompteBanqueDesactive",
            query = "select cpt.id , etiquette,valeur_compte valeurCompte,  libelle_compte libelleCompte ,categorie_compte categorieCompte,  classe_compte classeCompte,compte_pincipale_id comptePincipaleId, "
            + "first_categorie_compte firstCategorieCompte ,compte_banque_id compteBanqueId,compte_autre_id compteAutreId,  client_id clientId,  "
            + "  type_compte_id typeCompteId, "
            + "  unite_organisation_id uniteOrganisationId,etat_compte etatCompte,statut_compte statutCompte,numero_court numeroCourt  from compte cpt inner join client c on cpt.client_id=c.id"
            + " inner join compte_banque cb on cpt.compte_banque_id=cb.id where (cpt.statut_compte=:statut or cpt.statut_compte is null) and (cast(:dateDebut as timestamp) is null or cb.created_date between :dateDebut and :dateFin)  ",
            resultSetMapping = "Mapping.CompteDTO", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})
    ,

 @NamedNativeQuery(name = "Compte.findAllCompteBanque",
            query = "select cpt.id , etiquette,valeur_compte valeurCompte,  libelle_compte libelleCompte ,categorie_compte categorieCompte,  classe_compte classeCompte,compte_pincipale_id comptePincipaleId, "
            + "first_categorie_compte firstCategorieCompte ,compte_banque_id compteBanqueId,compte_autre_id compteAutreId,  client_id clientId,  "
            + "  type_compte_id typeCompteId, "
            + "  unite_organisation_id uniteOrganisationId,etat_compte etatCompte,statut_compte statutCompte,numero_court numeroCourt  from compte cpt inner join client c on cpt.client_id=c.id"
            + " inner join compte_banque cb on cpt.compte_banque_id=cb.id where (cast(:dateDebut as timestamp) is null or cb.created_date between :dateDebut and :dateFin)   ",
            resultSetMapping = "Mapping.CompteDTO", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})
    ,

 @NamedNativeQuery(name = "Compte.findValeurCompteAndAgentIdActifIn3months",
            query = "Select c.id as id,c.valeur_compte as valeurCompte, c.categorie_compte as categorieCompte, u.id as uniteOrganisationId,ca.last_modified_date as lastModifieddate from compte as c inner join unite_organisation as u on c.unite_organisation_id = u.id inner join compte_autre ca on c.compte_autre_id=ca.id where  u.typeuo_id=:typeUoId and c.etiquette='CPTEOPERATIONACTEUR'  and  coalesce(ca.last_modified_date,ca.date_derniere_transaction) between :dateDebut and :dateFin order by c.id desc limit :taille offset :page",
            resultSetMapping = "Mapping.findValeurCompteAndCategorieCompteAndAgentIdDTO"),})

@SqlResultSetMappings(value = {
    @SqlResultSetMapping(name = "Mapping.findByClientIdDTO",
            classes = @ConstructorResult(targetClass = CompteDTO.class,
                    columns = {
                        @ColumnResult(name = "id", type = Long.class)
                        ,
                                @ColumnResult(name = "etiquette", type = String.class)
                        ,
                                @ColumnResult(name = "valeurCompte", type = String.class)
                        ,
                                @ColumnResult(name = "libelleCompte", type = String.class)
                        ,
                                @ColumnResult(name = "categorieCompte", type = String.class)
                        ,
                                @ColumnResult(name = "classeCompte", type = String.class)
                        ,
                                @ColumnResult(name = "comptePincipaleId", type = Long.class)
                        ,
                                @ColumnResult(name = "firstCategorieCompte", type = String.class)
                        ,
                                @ColumnResult(name = "compteBanqueId", type = Long.class)
                        ,
                                @ColumnResult(name = "compteAutreId", type = Long.class)
                        ,
                                @ColumnResult(name = "clientId", type = Long.class)
                        ,
                                @ColumnResult(name = "typeCompteId", type = Long.class)
                        ,
                                @ColumnResult(name = "typeCompteLibelleTypeCompte", type = String.class)
                        ,
                                @ColumnResult(name = "uniteOrganisationId", type = Long.class)
                        ,
                                @ColumnResult(name = "uniteOrganisationLibelleUniteOrganisation", type = String.class)
                        ,
                                @ColumnResult(name = "etatCompte", type = String.class)
                        ,
                                @ColumnResult(name = "statutCompte", type = String.class)
                        ,
                                @ColumnResult(name = "numeroCourt", type = String.class)
                    }))
    ,

        @SqlResultSetMapping(name = "Mapping.findByClientIAndClasseComptedDTO",
            classes = @ConstructorResult(targetClass = CompteDTO.class,
                    columns = {
                        @ColumnResult(name = "id", type = Long.class)
                        ,
                                @ColumnResult(name = "etiquette", type = String.class)
                        ,
                                @ColumnResult(name = "valeurCompte", type = String.class)
                        ,
                                @ColumnResult(name = "libelleCompte", type = String.class)
                        ,
                                @ColumnResult(name = "categorieCompte", type = String.class)
                        ,
                                @ColumnResult(name = "classeCompte", type = String.class)
                        ,
                                @ColumnResult(name = "comptePincipaleId", type = Long.class)
                        ,
                                @ColumnResult(name = "firstCategorieCompte", type = String.class)
                        ,
                                @ColumnResult(name = "compteBanqueId", type = Long.class)
                        ,
                                @ColumnResult(name = "compteAutreId", type = Long.class)
                        ,
                                @ColumnResult(name = "clientId", type = Long.class)
                        ,
                                @ColumnResult(name = "typeCompteId", type = Long.class)
                        ,
                                @ColumnResult(name = "uniteOrganisationId", type = Long.class)
                        ,
                                @ColumnResult(name = "etatCompte", type = String.class)
                        ,
                                @ColumnResult(name = "statutCompte", type = String.class)
                        ,
                                @ColumnResult(name = "numeroCourt", type = String.class)
                        ,
                                @ColumnResult(name = "soldeCompte", type = BigDecimal.class)
                    }))
    ,


        @SqlResultSetMapping(name = "Mapping.CompteDTO",
            classes = @ConstructorResult(targetClass = CompteDTO.class,
                    columns = {
                        @ColumnResult(name = "id", type = Long.class)
                        ,
                    @ColumnResult(name = "etiquette", type = String.class)
                        ,
                    @ColumnResult(name = "valeurCompte", type = String.class)
                        ,
                    @ColumnResult(name = "libelleCompte", type = String.class)
                        ,
                    @ColumnResult(name = "categorieCompte", type = String.class)
                        ,
                    @ColumnResult(name = "classeCompte", type = String.class)
                        ,
                    @ColumnResult(name = "comptePincipaleId", type = Long.class)
                        ,
                    @ColumnResult(name = "firstCategorieCompte", type = String.class)
                        ,
                    @ColumnResult(name = "compteBanqueId", type = Long.class)
                        ,
                    @ColumnResult(name = "compteAutreId", type = Long.class)
                        ,
                    @ColumnResult(name = "clientId", type = Long.class)
                        ,
                    @ColumnResult(name = "typeCompteId", type = Long.class)
                        ,
                    @ColumnResult(name = "uniteOrganisationId", type = Long.class)
                        ,
                    @ColumnResult(name = "etatCompte", type = String.class)
                        ,
                    @ColumnResult(name = "statutCompte", type = String.class)
                        ,
                    @ColumnResult(name = "numeroCourt", type = String.class)

                    }))
    ,

               
        @SqlResultSetMapping(name = "Mapping.CompteAllDTO",
            classes = @ConstructorResult(targetClass = CompteDTO.class,
                    columns = {
                        @ColumnResult(name = "id", type = Long.class)
                        ,
                                @ColumnResult(name = "etiquette", type = String.class)
                        ,
                                @ColumnResult(name = "valeurCompte", type = String.class)
                        ,
                                @ColumnResult(name = "libelleCompte", type = String.class)
                        ,
                                @ColumnResult(name = "categorieCompte", type = String.class)
                        ,
                                @ColumnResult(name = "classeCompte", type = String.class)
                        ,
                                @ColumnResult(name = "comptePincipaleId", type = Long.class)
                        ,
                                @ColumnResult(name = "firstCategorieCompte", type = String.class)
                        ,
                                @ColumnResult(name = "compteBanqueId", type = Long.class)
                        ,
                                @ColumnResult(name = "compteAutreId", type = Long.class)
                        ,
                                @ColumnResult(name = "clientId", type = Long.class)
                        ,
                                @ColumnResult(name = "clientCodeClient", type = String.class)
                        ,
                                @ColumnResult(name = "numeroCourt", type = String.class)
                        ,
                                @ColumnResult(name = "typeCompteId", type = Long.class)
                        ,
                                @ColumnResult(name = "typeCompteLibelleTypeCompte", type = String.class)
                        ,
                                @ColumnResult(name = "uniteOrganisationId", type = Long.class)
                        ,
                                @ColumnResult(name = "uniteOrganisationLibelleUniteOrganisation", type = String.class)
                        ,
                                @ColumnResult(name = "soldeCompte", type = BigDecimal.class)
                        ,
                                @ColumnResult(name = "etatCompte", type = String.class)
                        ,
                                @ColumnResult(name = "statutCompte", type = String.class)

                    }))
    ,

        @SqlResultSetMapping(name = "Mapping.findByCompteAutreIdDTO",
            classes = @ConstructorResult(targetClass = CompteDTO.class,
                    columns = {
                        @ColumnResult(name = "id", type = Long.class)
                        ,
                                @ColumnResult(name = "etiquette", type = String.class)
                        ,
                                @ColumnResult(name = "valeurCompte", type = String.class)
                        ,
                                @ColumnResult(name = "comptePincipaleId", type = Long.class)
                        ,
                                @ColumnResult(name = "compteAutreId", type = Long.class)
                        ,
                                @ColumnResult(name = "clientId", type = Long.class)
                        ,
                                @ColumnResult(name = "uniteOrganisationId", type = Long.class),}))
    ,

        @SqlResultSetMapping(name = "Mapping.findCompteByIdReportDTO",
            classes = @ConstructorResult(targetClass = CompteDTO.class,
                    columns = {
                        @ColumnResult(name = "id", type = Long.class)
                        ,
                                @ColumnResult(name = "valeurCompte", type = String.class)
                        ,
                                @ColumnResult(name = "compteBanqueId", type = Long.class)
                        ,
                                @ColumnResult(name = "compteAutreId", type = Long.class)
                        ,
                                @ColumnResult(name = "clientId", type = Long.class)
                        ,
                                @ColumnResult(name = "uniteOrganisationId", type = Long.class),}))
    ,

        
@SqlResultSetMapping(name = "Mapping.CompteDTOSOLDE",
            classes = @ConstructorResult(targetClass = CompteDTO.class,
                    columns = {
                        @ColumnResult(name = "id", type = Long.class)
                        ,
                    @ColumnResult(name = "etiquette", type = String.class)
                        ,
                    @ColumnResult(name = "valeurCompte", type = String.class)
                        ,
                    @ColumnResult(name = "libelleCompte", type = String.class)
                        ,
                    @ColumnResult(name = "categorieCompte", type = String.class)
                        ,
                    @ColumnResult(name = "classeCompte", type = String.class)
                        ,
                    @ColumnResult(name = "comptePincipaleId", type = Long.class)
                        ,
                    @ColumnResult(name = "firstCategorieCompte", type = String.class)
                        ,
                    @ColumnResult(name = "compteBanqueId", type = Long.class)
                        ,
                    @ColumnResult(name = "compteAutreId", type = Long.class)
                        ,
                    @ColumnResult(name = "clientId", type = Long.class)
                        ,
                    @ColumnResult(name = "typeCompteId", type = Long.class)
                        ,
                    @ColumnResult(name = "uniteOrganisationId", type = Long.class)
                        ,
                    @ColumnResult(name = "etatCompte", type = String.class)
                        ,
                    @ColumnResult(name = "statutCompte", type = String.class)
                        ,
                    @ColumnResult(name = "numeroCourt", type = String.class)
                        ,
                    @ColumnResult(name = "soldeCompte", type = BigDecimal.class)
                        ,
                    @ColumnResult(name = "codeFse", type = String.class)

                    }))
    ,
@SqlResultSetMapping(name = "Mapping.CompteDTOBANQUE",
            classes = @ConstructorResult(targetClass = CompteDTO.class,
                    columns = {
                        @ColumnResult(name = "id", type = Long.class)
                        ,
                    @ColumnResult(name = "etiquette", type = String.class)
                        ,
                    @ColumnResult(name = "valeurCompte", type = String.class)
                        ,
                    @ColumnResult(name = "libelleCompte", type = String.class)
                        ,
                    @ColumnResult(name = "categorieCompte", type = String.class)
                        ,
                    @ColumnResult(name = "classeCompte", type = String.class)
                        ,
                    @ColumnResult(name = "comptePincipaleId", type = Long.class)
                        ,
                    @ColumnResult(name = "firstCategorieCompte", type = String.class)
                        ,
                    @ColumnResult(name = "compteBanqueId", type = Long.class)
                        ,
                    @ColumnResult(name = "compteAutreId", type = Long.class)
                       
                        ,
                    @ColumnResult(name = "clientId", type = Long.class)
                        ,
                    @ColumnResult(name = "typeCompteId", type = Long.class)
                        ,
                    @ColumnResult(name = "uniteOrganisationId", type = Long.class)
                        ,
                    @ColumnResult(name = "etatCompte", type = String.class)
                        ,
                    @ColumnResult(name = "statutCompte", type = String.class)
                        ,
                    @ColumnResult(name = "numeroCourt", type = String.class)
                        ,
                    
                    @ColumnResult(name = "codeFse", type = String.class)

                    }))

    ,
@SqlResultSetMapping(name = "Mapping.CompteDTOCARTE",
            classes = @ConstructorResult(targetClass = CompteDTO.class,
                    columns = {
                        @ColumnResult(name = "id", type = Long.class)
                        ,
                    @ColumnResult(name = "etiquette", type = String.class)
                        ,
                    @ColumnResult(name = "valeurCompte", type = String.class)
                        ,
                    @ColumnResult(name = "libelleCompte", type = String.class)
                        ,
                    @ColumnResult(name = "categorieCompte", type = String.class)
                        ,
                                        @ColumnResult(name = "carteId", type = Long.class)
                        ,
        @ColumnResult(name = "clientId", type = Long.class)
                        ,
                    @ColumnResult(name = "typeCompteId", type = Long.class)
                        ,
                    @ColumnResult(name = "uniteOrganisationId", type = Long.class)
                        ,
                    @ColumnResult(name = "etatCompte", type = String.class)
                        ,
                    @ColumnResult(name = "statutCompte", type = String.class)
                        ,
                        
                     @ColumnResult(name = "codeFse", type = String.class)
                        ,
                         @ColumnResult(name = "last4Digit", type = String.class)

                    }))
    ,
        @SqlResultSetMapping(name = "Mapping.CompteUoDTO",
            classes = @ConstructorResult(targetClass = CompteDTO.class,
                    columns = {
                        @ColumnResult(name = "id", type = Long.class)
                        ,
                                @ColumnResult(name = "etiquette", type = String.class)
                        ,
                                @ColumnResult(name = "valeurCompte", type = String.class)
                        ,
                                @ColumnResult(name = "libelleCompte", type = String.class)
                        ,
                                @ColumnResult(name = "categorieCompte", type = String.class)
                        ,
                                @ColumnResult(name = "classeCompte", type = String.class)
                        ,
                                @ColumnResult(name = "comptePincipaleId", type = Long.class)
                        ,
                                @ColumnResult(name = "firstCategorieCompte", type = String.class)
                        ,
                                @ColumnResult(name = "compteBanqueId", type = Long.class)
                        ,
                                @ColumnResult(name = "compteAutreId", type = Long.class)
                        ,
                                @ColumnResult(name = "typeCompteId", type = Long.class)
                        ,
                                @ColumnResult(name = "uniteOrganisationId", type = Long.class)
                        ,
                                @ColumnResult(name = "etatCompte", type = String.class)
                        ,
                                @ColumnResult(name = "statutCompte", type = String.class)
                        ,
                                @ColumnResult(name = "numeroCourt", type = String.class)

                    }))
    ,
        @SqlResultSetMapping(name = "Mapping.findNumeroCompteAndClientIdDTO",
            classes = @ConstructorResult(targetClass = CompteDTO.class,
                    columns = {
                        @ColumnResult(name = "valeurCompte", type = String.class)
                        ,
                                @ColumnResult(name = "clientId", type = Long.class)
                        ,
                                @ColumnResult(name = "categorieCompte", type = String.class)
                        ,
                                @ColumnResult(name = "etatCompte", type = String.class)
                        ,
                                @ColumnResult(name = "statutCompte", type = String.class)
                    }))
    ,

        @SqlResultSetMapping(name = "Mapping.findValeurCompteAndCategorieCompteAndClientIdDTO",
            classes = @ConstructorResult(targetClass = CompteDTO.class,
                    columns = {
                        @ColumnResult(name = "valeurCompte", type = String.class)
                        ,
                                @ColumnResult(name = "clientId", type = Long.class)
                        ,
                                @ColumnResult(name = "categorieCompte", type = String.class)
                    }))
    ,
        @SqlResultSetMapping(name = "Mapping.findValeurCompteAndCategorieCompteAndAgentIdDTO",
            classes = @ConstructorResult(targetClass = CompteDTO.class,
                    columns = {
                        @ColumnResult(name = "id", type = Long.class)
                        ,
                                @ColumnResult(name = "valeurCompte", type = String.class)
                        ,
                                @ColumnResult(name = "uniteOrganisationId", type = Long.class)
                        ,
                                @ColumnResult(name = "categorieCompte", type = String.class)
                        ,
                                @ColumnResult(name = "lastModifiedDate", type = Instant.class)
                    }))
    ,

        @SqlResultSetMapping(name = "Mapping.XBanqueReportDTO",
            classes = @ConstructorResult(targetClass = CompteDTO.class,
                    columns = {
                        @ColumnResult(name = "nomClient", type = String.class)
                        ,
                                @ColumnResult(name = "prenomClient", type = String.class)
                        ,
                                @ColumnResult(name = "telephone", type = String.class)
                        ,
                                @ColumnResult(name = "numeroCompteBanquaire", type = String.class)
                        ,
                                @ColumnResult(name = "codeAgence", type = String.class)
                        ,
                                @ColumnResult(name = "codeBanque", type = String.class)
                        ,
                                @ColumnResult(name = "riv", type = String.class)
                        ,
                                @ColumnResult(name = "statutCompte", type = String.class),}))
    ,
        @SqlResultSetMapping(name = "Mapping.findValeurCompteAndCategorieCompteAndClientIdDTODate",
            classes = @ConstructorResult(targetClass = CompteDTO.class,
                    columns = {
                        @ColumnResult(name = "valeurCompte", type = String.class)
                        ,
                                @ColumnResult(name = "clientId", type = Long.class)
                        ,
                                @ColumnResult(name = "categorieCompte", type = String.class)
                        ,
                                @ColumnResult(name = "lastModifiedDate", type = Instant.class)
                    })),})
/**
 * A Compte.
 */
@Entity
@Table(name = "compte")
@EntityListeners(value = {EntityAuditEventListener.class})
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Cacheable(value = false)
public class Compte extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "etiquette", nullable = false)
    private String etiquette;

    @Column(name = "libelle_compte")
    private String libelleCompte;

    @Enumerated(EnumType.STRING)
    @Column(name = "first_categorie_compte")
    private FirstCategorieCompte firstCategorieCompte;

    @OneToOne
    @JoinColumn(unique = true)
    private CompteBanque compteBanque;
    @OneToOne
    @JoinColumn(unique = true)
    private Carte carte;
    @OneToOne
    @JoinColumn(unique = true)
    private CompteAutre compteAutre;

    @ManyToOne
    private Client client;

    @ManyToOne
    private TypeCompte typeCompte;

    @ManyToOne
    private UniteOrganisation uniteOrganisation;

    @Column(name = "valeur_compte")
    private String valeurCompte;
    @Enumerated(EnumType.STRING)
    @Column(name = "classe_compte")
    private ClasseCompte classeCompte;

    @Enumerated(EnumType.STRING)
    @Column(name = "categorie_compte")
    private CategorieCompte categorieCompte;

    @Enumerated(EnumType.STRING)
    @Column(name = "etat_compte")
    private EtatCompte etatCompte;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_compte")
    private StatutCompte statutCompte;

    @Column(name = "acc_id")
    private Integer tagAccountId;

    @ManyToOne
    private Compte comptePincipale;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    @Column(name = "numero_court")
    private String numeroCourt;

    @Column(name = "alias")
    private String alias;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEtiquette() {
        return etiquette;
    }

    public Compte etiquette(String etiquette) {
        this.etiquette = etiquette;
        return this;
    }

    public void setEtiquette(String etiquette) {
        this.etiquette = etiquette;
    }

    public String getValeurCompte() {
        return valeurCompte;
    }

    public String getLibelleCompte() {
        return libelleCompte;
    }

    public void setLibelleCompte(String libelleCompte) {
        this.libelleCompte = libelleCompte;
    }

    public Compte valeurCompte(String valeurCompte) {
        this.valeurCompte = valeurCompte;
        return this;
    }

    public Compte alias(String alias) {
        this.alias = alias;
        return this;
    }

    public void setValeurCompte(String valeurCompte) {
        this.valeurCompte = valeurCompte;
    }

    public CategorieCompte getCategorieCompte() {
        return categorieCompte;
    }

    public Compte categorieCompte(CategorieCompte categorieCompte) {
        this.categorieCompte = categorieCompte;
        return this;
    }

    public void setCategorieCompte(CategorieCompte categorieCompte) {
        this.categorieCompte = categorieCompte;
    }

    public FirstCategorieCompte getFirstCategorieCompte() {
        return firstCategorieCompte;
    }

    public void setFirstCategorieCompte(FirstCategorieCompte firstCategorieCompte) {
        this.firstCategorieCompte = firstCategorieCompte;
    }

    public Compte getComptePincipale() {
        return comptePincipale;
    }

    public Compte comptePincipale(Compte compte) {
        this.comptePincipale = compte;
        return this;
    }

    public void setComptePincipale(Compte compte) {
        this.comptePincipale = compte;
    }

    public CompteAutre getCompteAutre() {
        return compteAutre;
    }

    public Compte compteAutre(CompteAutre compteAutre) {
        this.compteAutre = compteAutre;
        return this;
    }

    public void setCompteAutre(CompteAutre compteAutre) {
        this.compteAutre = compteAutre;
    }

    public Client getClient() {
        return client;
    }

    public Compte client(Client client) {
        this.client = client;
        return this;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public TypeCompte getTypeCompte() {
        return typeCompte;
    }

    public Compte typeCompte(TypeCompte typeCompte) {
        this.typeCompte = typeCompte;
        return this;
    }

    public void setTypeCompte(TypeCompte typeCompte) {
        this.typeCompte = typeCompte;
    }

    public CompteBanque getCompteBanque() {
        return compteBanque;
    }

    public Compte compteBanque(CompteBanque compteBanque) {
        this.compteBanque = compteBanque;
        return this;
    }

    public Compte carte(Carte carte) {
        this.carte = carte;
        return this;
    }

    public void setCompteBanque(CompteBanque compteBanque) {
        this.compteBanque = compteBanque;
    }

    public UniteOrganisation getUniteOrganisation() {
        return uniteOrganisation;
    }

    public Compte uniteOrganisation(UniteOrganisation uniteOrganisation) {
        this.uniteOrganisation = uniteOrganisation;
        return this;
    }

    public void setUniteOrganisation(UniteOrganisation uniteOrganisation) {
        this.uniteOrganisation = uniteOrganisation;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    public ClasseCompte getClasseCompte() {
        return classeCompte;
    }

    public void setClasseCompte(ClasseCompte classeCompte) {
        this.classeCompte = classeCompte;
    }

    public EtatCompte getEtatCompte() {
        return etatCompte;
    }

    public void setEtatCompte(EtatCompte etatCompte) {
        this.etatCompte = etatCompte;
    }

    public StatutCompte getStatutCompte() {
        return statutCompte;
    }

    public void setStatutCompte(StatutCompte statutCompte) {
        this.statutCompte = statutCompte;
    }

    public String getNumeroCourt() {
        return numeroCourt;
    }

    public void setNumeroCourt(String numeroCourt) {
        this.numeroCourt = numeroCourt;
    }

    public Integer getTagAccountId() {
        return tagAccountId;
    }

    public void setTagAccountId(Integer tagAccountId) {
        this.tagAccountId = tagAccountId;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public Carte getCarte() {
        return carte;
    }

    public void setCarte(Carte carte) {
        this.carte = carte;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Compte compte = (Compte) o;
        if (compte.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), compte.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "Compte{"
                + "id=" + getId()
                + ", etiquette='" + getEtiquette() + "'"
                + ", valeurCompte='" + getValeurCompte() + "'"
                + ", categorieCompte='" + getCategorieCompte() + "'"
                + ",classeCompte='" + getClasseCompte() + "'"
                + "}";
    }
}
