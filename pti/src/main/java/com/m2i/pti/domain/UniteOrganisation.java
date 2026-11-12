package com.m2i.pti.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.m2i.pti.config.audit.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import com.m2i.pti.service.dto.UniteOrganisationDTO;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A UniteOrganisation.
 */
@SqlResultSetMappings(value = {
    @SqlResultSetMapping(name = "Mapping.UniteOrganisationDTO",
            classes = @ConstructorResult(targetClass = UniteOrganisationDTO.class,
                    columns = {
                        @ColumnResult(name = "id", type = Long.class)
                        ,
                        @ColumnResult(name = "libelleUniteOrganisation", type = String.class)
                        ,
                        @ColumnResult(name = "estSiege", type = Boolean.class)
                        ,
                        @ColumnResult(name = "codeUo", type = String.class)
                        ,
                        @ColumnResult(name = "numeroOrdre", type = Integer.class)
                        ,
                        @ColumnResult(name = "codeFse", type = String.class)
                        ,
                        @ColumnResult(name = "estFiliale", type = Boolean.class)
                        ,
                        @ColumnResult(name = "codePaysTelephone", type = String.class)
                        ,
                        @ColumnResult(name = "codePays", type = String.class)
                        ,
                        @ColumnResult(name = "adresse", type = String.class)
                        ,
                        @ColumnResult(name = "email", type = String.class)
                        ,
                        @ColumnResult(name = "niveau", type = Integer.class)
                        ,
                        @ColumnResult(name = "estActif", type = Boolean.class)
                        ,
                        @ColumnResult(name = "pereUOId", type = Long.class)
                        ,
                        @ColumnResult(name = "telephone", type = String.class)
                        ,
                        @ColumnResult(name = "typeUOId", type = Long.class)
                        ,
                        @ColumnResult(name = "profilActeurId", type = Long.class)
                        ,
                        @ColumnResult(name = "serviceProfilId", type = Long.class)
                        ,
                        @ColumnResult(name = "codeBanque", type = String.class)
                        ,
                        @ColumnResult(name = "uoPaysId", type = Long.class)
                        ,
                        @ColumnResult(name = "typeUOCode", type = String.class)
                        ,
                        @ColumnResult(name = "typeUOLibelleTypeUO", type = String.class)
                        ,
                        @ColumnResult(name = "niveauTypeUo", type = Integer.class)
                    }))
    ,
    @SqlResultSetMapping(name = "Mapping.UniteOrganisationDTOFindOne",
            classes = @ConstructorResult(targetClass = UniteOrganisationDTO.class,
                    columns = {
                        @ColumnResult(name = "id", type = Long.class)
                        ,
                        @ColumnResult(name = "libelleUniteOrganisation", type = String.class)
                        ,
                        @ColumnResult(name = "estSiege", type = Boolean.class)
                        ,
                        @ColumnResult(name = "codeUo", type = String.class)
                        ,
                        @ColumnResult(name = "numeroOrdre", type = Integer.class)
                        ,
                        @ColumnResult(name = "codeFse", type = String.class)
                        ,
                        @ColumnResult(name = "estFiliale", type = Boolean.class)
                        ,
                        @ColumnResult(name = "codePaysTelephone", type = String.class)
                        ,
                        @ColumnResult(name = "codePays", type = String.class)
                        ,
                        @ColumnResult(name = "adresse", type = String.class)
                        ,
                        @ColumnResult(name = "email", type = String.class)
                        ,
                        @ColumnResult(name = "niveau", type = Integer.class)
                        ,
                        @ColumnResult(name = "estActif", type = Boolean.class)
                        ,
                        @ColumnResult(name = "pereUOId", type = Long.class)
                        ,
                        @ColumnResult(name = "telephone", type = String.class)
                        ,
                        @ColumnResult(name = "typeUOId", type = Long.class)
                        ,
                        @ColumnResult(name = "profilActeurId", type = Long.class)
                        ,
                        @ColumnResult(name = "serviceProfilId", type = Long.class)
                        ,
                        @ColumnResult(name = "codeBanque", type = String.class)
                        ,
                        @ColumnResult(name = "uoPaysId", type = Long.class)
                        ,
                        @ColumnResult(name = "typeUOCode", type = String.class)
                        ,
                        @ColumnResult(name = "typeUOLibelleTypeUO", type = String.class)
                        ,
                        @ColumnResult(name = "niveauTypeUo", type = Integer.class)

                    }))
    ,

        @SqlResultSetMapping(name = "Mapping.UniteOrganisationDTOTypeUO",
            classes = @ConstructorResult(targetClass = UniteOrganisationDTO.class,
                    columns = {
                        @ColumnResult(name = "id", type = Long.class)
                        ,
                                @ColumnResult(name = "libelleUniteOrganisation", type = String.class)
                        ,
                                @ColumnResult(name = "estSiege", type = Boolean.class)
                        ,
                                @ColumnResult(name = "codeUO", type = String.class)
                        ,
                                @ColumnResult(name = "numeroOrdre", type = Integer.class)
                        ,
                                @ColumnResult(name = "codeFse", type = String.class)
                        ,
                                @ColumnResult(name = "estFiliale", type = Boolean.class)
                        ,
                                @ColumnResult(name = "telephone", type = String.class)
                        ,
                                @ColumnResult(name = "codePaysTelephone", type = String.class)
                        ,
                                @ColumnResult(name = "adresse", type = String.class)
                        ,
                                @ColumnResult(name = "email", type = String.class)
                        ,
                                @ColumnResult(name = "niveau", type = Integer.class)
                        ,
                                @ColumnResult(name = "pereUOId", type = Long.class)
                        ,
                                @ColumnResult(name = "typeUOId", type = Long.class)
                        ,
                                @ColumnResult(name = "codeBanque", type = String.class)
                        ,
                                @ColumnResult(name = "codePays", type = String.class)
                        ,
                                @ColumnResult(name = "estActif", type = Boolean.class)
                        ,
                                @ColumnResult(name = "serviceProfilId", type = Long.class)
                        ,
                                @ColumnResult(name = "profilActeurId", type = Long.class)
                        ,
                                @ColumnResult(name = "uoPaysId", type = Long.class)

                    }))
    ,

        @SqlResultSetMapping(name = "Mapping.UniteOrganisationfindForViewPaginatedDTO",
            classes = @ConstructorResult(targetClass = UniteOrganisationDTO.class,
                    columns = {
                        @ColumnResult(name = "id", type = Long.class)
                        ,
                                @ColumnResult(name = "libelleUniteOrganisation", type = String.class)
                        ,
                                @ColumnResult(name = "estSiege", type = Boolean.class)
                        ,
                                @ColumnResult(name = "codeUO", type = String.class)
                        ,
                                @ColumnResult(name = "numeroOrdre", type = Integer.class)
                        ,
                                @ColumnResult(name = "codeFse", type = String.class)
                        ,
                                @ColumnResult(name = "feuille", type = Boolean.class)
                        ,
                                @ColumnResult(name = "estFiliale", type = Boolean.class)
                        ,
                                @ColumnResult(name = "telephone", type = String.class)
                        ,
                                @ColumnResult(name = "codePaysTelephone", type = String.class)
                        ,
                                @ColumnResult(name = "codePays", type = String.class)
                        ,
                                @ColumnResult(name = "adresse", type = String.class)
                        ,
                                @ColumnResult(name = "email", type = String.class)
                        ,
                                @ColumnResult(name = "niveau", type = Integer.class)
                        ,
                                @ColumnResult(name = "niveauTypeUo", type = Integer.class)
                        ,
                                @ColumnResult(name = "estActif", type = Boolean.class)
                        ,
                                @ColumnResult(name = "pereUOId", type = Long.class)
                        ,
                                @ColumnResult(name = "typeUOId", type = Long.class)
                        ,
                                @ColumnResult(name = "profilActeurId", type = Long.class)
                        ,
                                @ColumnResult(name = "serviceProfilId", type = Long.class)
                        ,
                                @ColumnResult(name = "codeBanque", type = String.class)
                        ,
                                @ColumnResult(name = "uoPaysId", type = Long.class)
                        ,
                                @ColumnResult(name = "typeUOCode", type = String.class)
                        ,
                                @ColumnResult(name = "typeUOLibelleTypeUO", type = String.class)
                        ,
                                @ColumnResult(name = "representantId", type = Long.class)
                        ,
                                @ColumnResult(name = "dateCreation", type = LocalDate.class)
                        ,
                                @ColumnResult(name = "serviceProfilNom", type = String.class)
                        ,
                                @ColumnResult(name = "profilActeurNom", type = String.class)
                        ,
                                @ColumnResult(name = "groupeProfilActeurId", type = Long.class)
                        ,
                                @ColumnResult(name = "groupeProfilActeurNom", type = String.class)
                        ,
                                @ColumnResult(name = "vlilleId", type = Long.class),
                                @ColumnResult(name = "agentId", type = Integer.class),
                                @ColumnResult(name = "tagIdentityRef", type = Integer.class),
                                @ColumnResult(name = "marchandId", type = Integer.class),
                                @ColumnResult(name = "cosId", type = Integer.class),
                                @ColumnResult(name = "uoZoneId", type = Long.class),
                                @ColumnResult(name = "uoFseId", type = Long.class),
                                @ColumnResult(name = "createdDate", type = Instant.class),
                    }
            ))

})

@NamedNativeQueries({
    @NamedNativeQuery(name = "UniteOrganisation.findByCodeUOAndSupprimeIsFalse",
            query = "SELECT u.id, libelle_unite_organisation libelleUniteOrganisation,  est_siege estSiege, code_uo codeUo ,numero_ordre numeroOrdre, "
            + "code_fse codeFse,  est_filiale estFiliale ,  code_telephone codePaysTelephone ,"
            + "indicatif_pays codePays,adresse, email, u.niveau, est_actif estActif,  pereuo_id as pereUOId,  telephone,  "
            + "typeuo_id as typeUOId,profil_acteur_id profilActeurId ,service_profil_id serviceProfilId, code_banque codeBanque, uo_pays_id uoPaysId   , tu.code_type_uo typeUOCode, tu.libelle_type_uo typeUOLibelleTypeUO, tu.niveau niveauTypeUo "
            + "FROM unite_organisation u , type_unite_organisation tu WHERE u.typeuo_id=tu.id and code_uo = :codeUo and supprime = :issupp",
            resultSetMapping = "Mapping.UniteOrganisationDTO",hints={@QueryHint(name="org.hibernate.cacheable",value="false")})
    ,
    @NamedNativeQuery(name = "UniteOrganisation.findByCodeFseAndSupprimeIsFalse",
            query = "SELECT u.id, libelle_unite_organisation libelleUniteOrganisation,  est_siege estSiege, code_uo codeUo ,numero_ordre numeroOrdre, "
            + "code_fse codeFse,  est_filiale estFiliale ,  code_telephone codePaysTelephone ,"
            + "indicatif_pays codePays,adresse, email, u.niveau, est_actif estActif,  pereuo_id as pereUOId,  telephone,  "
            + "typeuo_id as typeUOId,profil_acteur_id profilActeurId ,service_profil_id serviceProfilId , code_banque codeBanque , uo_pays_id uoPaysId   , tu.code_type_uo typeUOCode, tu.libelle_type_uo typeUOLibelleTypeUO, tu.niveau niveauTypeUo "
            + "FROM unite_organisation u , type_unite_organisation tu WHERE u.typeuo_id=tu.id and  code_fse = :codefse and supprime=false",
            resultSetMapping = "Mapping.UniteOrganisationDTO",hints={@QueryHint(name="org.hibernate.cacheable",value="true")})
    ,
    @NamedNativeQuery(name = "UniteOrganisation.findOne",
            query = "SELECT u.id, libelle_unite_organisation libelleUniteOrganisation,  est_siege estSiege, code_uo codeUo ,numero_ordre numeroOrdre, "
            + "code_fse codeFse,  est_filiale estFiliale ,  code_telephone codePaysTelephone ,"
            + " indicatif_pays codePays,adresse, email, u.niveau, est_actif estActif,  pereuo_id as pereUOId,  telephone,  "
            + " typeuo_id as typeUOId,profil_acteur_id profilActeurId ,service_profil_id serviceProfilId , code_banque codeBanque,  uo_pays_id uoPaysId  , tu.code_type_uo typeUOCode, tu.libelle_type_uo typeUOLibelleTypeUO, tu.niveau niveauTypeUo "
            + " FROM unite_organisation u , type_unite_organisation tu WHERE u.typeuo_id=tu.id and u.id =CAST(CAST (:id AS TEXT) AS BIGINT ) and supprime=false",
            resultSetMapping = "Mapping.UniteOrganisationDTOFindOne",hints={@QueryHint(name="org.hibernate.cacheable",value="false")})
    ,
 @NamedNativeQuery(name = "UniteOrganisation.findByUoFse",
            query = "SELECT u.id, libelle_unite_organisation libelleUniteOrganisation,  est_siege estSiege, code_uo codeUo ,numero_ordre numeroOrdre, "
            + "code_fse codeFse,  est_filiale estFiliale ,  code_telephone codePaysTelephone ,"
            + " indicatif_pays codePays,adresse, email, u.niveau, est_actif estActif,  pereuo_id as pereUOId,  telephone,  "
            + " typeuo_id as typeUOId,profil_acteur_id profilActeurId ,service_profil_id serviceProfilId , code_banque codeBanque,  uo_pays_id uoPaysId  , tu.code_type_uo typeUOCode, tu.libelle_type_uo typeUOLibelleTypeUO, tu.niveau niveauTypeUo "
            + " FROM unite_organisation u , type_unite_organisation tu WHERE u.uo_fse_id=:uofseId and supprime=false",
            resultSetMapping = "Mapping.UniteOrganisationDTOFindOne",hints={@QueryHint(name="org.hibernate.cacheable",value="false")})
    ,
        @NamedNativeQuery(name = "UniteOrganisation.findAgenceDigitaleOptimise",
                query = "SELECT u.id, libelle_unite_organisation libelleUniteOrganisation,  est_siege estSiege, code_uo codeUo ,numero_ordre numeroOrdre, "
                        + "code_fse codeFse,  est_filiale estFiliale ,  code_telephone codePaysTelephone ,"
                        + "indicatif_pays codePays,adresse, email, u.niveau, est_actif estActif,  pereuo_id as pereUOId,  telephone,  "
                        + "typeuo_id as typeUOId,profil_acteur_id profilActeurId ,service_profil_id serviceProfilId , code_banque codeBanque , uo_pays_id uoPaysId   , tu.code_type_uo typeUOCode, tu.libelle_type_uo typeUOLibelleTypeUO, tu.niveau niveauTypeUo "
                        + "FROM unite_organisation u , type_unite_organisation tu WHERE u.typeuo_id=tu.id and  typeuo_id = CAST(CAST (:typeUoId AS TEXT) AS BIGINT ) and supprime=false and est_siege=true order by libelle_unite_organisation asc",
                resultSetMapping = "Mapping.UniteOrganisationDTO",hints={@QueryHint(name="org.hibernate.cacheable",value="false")})
        ,
        @NamedNativeQuery(name = "UniteOrganisation.findAgentOptimise",
                query = "SELECT u.id, libelle_unite_organisation libelleUniteOrganisation,  est_siege estSiege, code_uo codeUo ,numero_ordre numeroOrdre, "
                        + "code_fse codeFse,  est_filiale estFiliale ,  code_telephone codePaysTelephone ,"
                        + "indicatif_pays codePays,adresse, email, u.niveau, est_actif estActif,  pereuo_id as pereUOId,  telephone,  "
                        + "typeuo_id as typeUOId,profil_acteur_id profilActeurId ,service_profil_id serviceProfilId , code_banque codeBanque , uo_pays_id uoPaysId   , tu.code_type_uo typeUOCode, tu.libelle_type_uo typeUOLibelleTypeUO, tu.niveau niveauTypeUo "
                        + "FROM unite_organisation u , type_unite_organisation tu WHERE u.typeuo_id=tu.id and  typeuo_id in (:typeUoId  ) and supprime=false  order by libelle_unite_organisation asc",
                resultSetMapping = "Mapping.UniteOrganisationDTO",hints={@QueryHint(name="org.hibernate.cacheable",value="false")})
        ,

        @NamedNativeQuery(name = "UniteOrganisation.findByTypeUOAndSupprimeIsFalseOptimise",
            query = "SELECT u.id, libelle_unite_organisation libelleUniteOrganisation,  est_siege estSiege, code_uo codeUo ,numero_ordre numeroOrdre, "
            + "code_fse codeFse,  est_filiale estFiliale ,  code_telephone codePaysTelephone ,"
            + "indicatif_pays codePays,adresse, email, u.niveau, est_actif estActif,  pereuo_id as pereUOId,  telephone,  "
            + "typeuo_id as typeUOId,profil_acteur_id profilActeurId ,service_profil_id serviceProfilId , code_banque codeBanque , uo_pays_id uoPaysId   , tu.code_type_uo typeUOCode, tu.libelle_type_uo typeUOLibelleTypeUO, tu.niveau niveauTypeUo "
            + "FROM unite_organisation u , type_unite_organisation tu WHERE u.typeuo_id=tu.id and  typeuo_id = CAST(CAST (:typeUoId AS TEXT) AS BIGINT ) and supprime=false order by libelle_unite_organisation asc",
            resultSetMapping = "Mapping.UniteOrganisationDTO",hints={@QueryHint(name="org.hibernate.cacheable",value="false")})
    ,


        @NamedNativeQuery(name = "UniteOrganisation.findAllOptimise",
            query = "SELECT u.id,libelle_unite_organisation libelleUniteOrganisation,  est_siege estSiege, code_uo codeUo ,numero_ordre numeroOrdre,"
            + "code_fse codeFse,  est_filiale estFiliale ,  code_telephone codePaysTelephone,"
            + "indicatif_pays codePays,adresse, email, u.niveau, est_actif estActif,  pereuo_id as pereUOId,  telephone,"
            + "typeuo_id as typeUOId,profil_acteur_id profilActeurId ,service_profil_id serviceProfilId, code_banque codeBanque, uo_pays_id uoPaysId, tu.code_type_uo typeUOCode, tu.libelle_type_uo typeUOLibelleTypeUO, tu.niveau niveauTypeUo"
            + " FROM unite_organisation u , type_unite_organisation tu WHERE u.typeuo_id=tu.id and u.supprime=false",
            resultSetMapping = "Mapping.UniteOrganisationDTO",hints={@QueryHint(name="org.hibernate.cacheable",value="false")})
    ,

    @NamedNativeQuery(name = "UniteOrganisation.findAllOptimiseByTypeUo",
            query = "SELECT u.id,libelle_unite_organisation libelleUniteOrganisation,  est_siege estSiege, code_uo codeUo ,numero_ordre numeroOrdre,"
            + "code_fse codeFse,  est_filiale estFiliale ,  code_telephone codePaysTelephone,"
            + "indicatif_pays codePays,adresse, email, u.niveau, est_actif estActif,  pereuo_id as pereUOId,  telephone,"
            + "typeuo_id as typeUOId,profil_acteur_id profilActeurId ,service_profil_id serviceProfilId, code_banque codeBanque, uo_pays_id uoPaysId   , tu.code_type_uo typeUOCode, tu.libelle_type_uo typeUOLibelleTypeUO, tu.niveau niveauTypeUo"
            + " FROM unite_organisation u , type_unite_organisation tu WHERE u.typeuo_id=tu.id and u.typeuo_id = :typeUoId and u.supprime=false",
            resultSetMapping = "Mapping.UniteOrganisationDTO",hints={@QueryHint(name="org.hibernate.cacheable",value="false")})
    ,

   @NamedNativeQuery(name = "UniteOrganisation.findByPereUOAndSupprimeIsFalseOptimiser",
            query = "SELECT u.id,libelle_unite_organisation libelleUniteOrganisation,  est_siege estSiege, code_uo codeUo ,numero_ordre numeroOrdre,"
            + "code_fse codeFse,  est_filiale estFiliale ,  code_telephone codePaysTelephone,"
            + "indicatif_pays codePays,adresse, email, u.niveau, est_actif estActif,  pereuo_id as pereUOId,  telephone,"
            + "typeuo_id as typeUOId,profil_acteur_id profilActeurId ,service_profil_id serviceProfilId, code_banque codeBanque, uo_pays_id uoPaysId   , tu.code_type_uo typeUOCode, tu.libelle_type_uo typeUOLibelleTypeUO, tu.niveau niveauTypeUo"
            + " FROM unite_organisation u , type_unite_organisation tu WHERE u.typeuo_id=tu.id and u.pereuo_id = :pereUOID and u.supprime=false",
            resultSetMapping = "Mapping.UniteOrganisationDTO",hints={@QueryHint(name="org.hibernate.cacheable",value="false")})
    ,
   @NamedNativeQuery(name = "UniteOrganisation.findChildrenPage",
            query = "SELECT u.id,libelle_unite_organisation libelleUniteOrganisation,  est_siege estSiege, code_uo codeUo ,numero_ordre numeroOrdre,"
            + "code_fse codeFse,  est_filiale estFiliale ,  code_telephone codePaysTelephone,"
            + "indicatif_pays codePays,adresse, email, u.niveau, est_actif estActif,  pereuo_id as pereUOId,  telephone,"
            + "typeuo_id as typeUOId,profil_acteur_id profilActeurId ,service_profil_id serviceProfilId, code_banque codeBanque, uo_pays_id uoPaysId   , tu.code_type_uo typeUOCode, tu.libelle_type_uo typeUOLibelleTypeUO, tu.niveau niveauTypeUo"
            + " FROM unite_organisation u , type_unite_organisation tu WHERE u.typeuo_id=tu.id and u.pereuo_id = CAST(CAST(:pereUOID AS TEXT) AS BIGINT)"
            + " and (u.supprime is null or u.supprime=false) order by u.libelle_unite_organisation limit :taille offset :page",
            resultSetMapping = "Mapping.UniteOrganisationDTO",hints={@QueryHint(name="org.hibernate.cacheable",value="false")})
    ,
   @NamedNativeQuery(name = "UniteOrganisation.findByTypeUo",
            query = "SELECT u.id, libelle_unite_organisation libelleUniteOrganisation,  est_siege estSiege, code_uo codeUo ,numero_ordre numeroOrdre, "
            + "code_fse codeFse,  est_filiale estFiliale ,  code_telephone codePaysTelephone ,"
            + " indicatif_pays codePays,adresse, email, u.niveau, est_actif estActif,  pereuo_id as pereUOId,  telephone,  "
            + " typeuo_id as typeUOId,profil_acteur_id profilActeurId ,service_profil_id serviceProfilId , code_banque codeBanque,  uo_pays_id uoPaysId"
            + " FROM unite_organisation u WHERE u.typeuo_id = :id and supprime=false",
            resultSetMapping = "Mapping.UniteOrganisationDTOTypeUO",hints={@QueryHint(name="org.hibernate.cacheable",value="false")})
    ,

        @NamedNativeQuery(name = "UniteOrganisation.findForViewPaginatedOptimise",
            query = "SELECT u.id, u.libelle_unite_organisation as libelleUniteOrganisation, u.est_siege as estSiege, u.code_uo as codeUo ,u.numero_ordre as numeroOrdre,u.feuille,"
            + "u.code_fse as codeFse,  u.est_filiale as estFiliale , u.telephone, u.code_telephone as codePaysTelephone,"
            + "u.indicatif_pays as codePays, u.adresse, u.email, u.niveau, tu.niveau as niveauTypeUo, u.est_actif as estActif,  u.pereuo_id as pereUOId,"
            + "u.typeuo_id as typeUOId, u.profil_acteur_id as profilActeurId ,u.service_profil_id as serviceProfilId, u.code_banque as codeBanque, u.uo_pays_id as uoPaysId, "
            + "tu.code_type_uo as typeUOCode, tu.libelle_type_uo as typeUOLibelleTypeUO, u.representant_id as representantId,"
            + "u.date_creation as dateCreation, sp.nom as serviceProfilNom,"
            + "pa.nom as profilActeurNom, u.groupe_profil_acteur_id as groupeProfilActeurId, gp.nom as groupeProfilActeurNom,u.ville_id as vlilleId,u.agt_id as agentId,u.idt_id as tagIdentityRef,u.mer_id marchandId,u.cos_id as cosId,u.uo_zone_id as uoZoneId,u.uo_fse_id as uoFseId,u.created_date as createdDate"
            + " FROM unite_organisation u"
            + " LEFT JOIN type_unite_organisation tu on u.typeuo_id=tu.id"
            + " LEFT JOIN service_profil sp on u.service_profil_id=sp.id"
            + " LEFT JOIN profil_acteur pa on u.profil_acteur_id=pa.id"
            + " LEFT JOIN groupe_profil_acteur gp on u.groupe_profil_acteur_id=gp.id"
            + " WHERE("
            + "(tu.code_type_uo!='FSE')"
            + "and (tu.code_type_uo!='PAYS')"
            + "and (tu.code_type_uo!='ZONE')"
            + "and (:libelleUniteOrganisation is null or :libelleUniteOrganisation='' or UPPER(u.libelle_unite_organisation)  like UPPER('%'||CAST (:libelleUniteOrganisation AS TEXT)||'%'))"
            + "and (:codeUO is null or :codeUO='' or UPPER(u.code_uo) like UPPER('%'||CAST (:codeUO AS TEXT)||'%'))"
            + "and (:typeUO is null or  tu.code_type_uo= CAST(:typeUO AS TEXT))"
            + "and (:numeroOrdre is null or u.numero_ordre= CAST(CAST (:numeroOrdre AS TEXT) AS integer))"
            + "and (:groupeId is null or u.groupe_profil_acteur_id= CAST (CAST (:groupeId AS TEXT) AS BIGINT))"
            + "and (:villeId is null or u.ville_id= CAST(CAST (:villeId AS TEXT) AS BIGINT))"
            + "and (:adresse is null or :adresse='' or UPPER(u.adresse)  like UPPER('%'||CAST (:adresse AS TEXT)||'%'))"
            + "and (u.supprime is null or u.supprime=false)) order by u.id desc limit :taille offset :page",
            resultSetMapping = "Mapping.UniteOrganisationfindForViewPaginatedDTO",hints={@QueryHint(name="org.hibernate.cacheable",value="false")})
    ,

        @NamedNativeQuery(name = "UniteOrganisation.findByPereUo",
            query = "SELECT u.id, libelle_unite_organisation libelleUniteOrganisation,  est_siege estSiege, code_uo codeUo ,numero_ordre numeroOrdre, "
            + "code_fse codeFse,  est_filiale estFiliale ,  code_telephone codePaysTelephone ,"
            + " indicatif_pays codePays,adresse, email, u.niveau, est_actif estActif,  pereuo_id as pereUOId,  telephone,  "
            + " typeuo_id as typeUOId,profil_acteur_id profilActeurId ,service_profil_id serviceProfilId , code_banque codeBanque,  uo_pays_id uoPaysId,"
            + "tu.code_type_uo as typeUOCode,tu.libelle_type_uo as typeUOLibelleTypeUO, tu.niveau as niveauTypeUo"
            + " FROM unite_organisation u,type_unite_organisation tu WHERE u.typeuo_id=tu.id and u.pereuo_id = CAST(CAST (:id AS TEXT) AS BIGINT ) and supprime=false",
            resultSetMapping = "Mapping.UniteOrganisationDTOFindOne",hints={@QueryHint(name="org.hibernate.cacheable",value="false")})
    ,

        
         @NamedNativeQuery(name = "UniteOrganisation.findByPereUoAndType",
            query = "SELECT u.id, libelle_unite_organisation libelleUniteOrganisation,  est_siege estSiege, code_uo codeUo ,numero_ordre numeroOrdre, "
            + "code_fse codeFse,  est_filiale estFiliale ,  code_telephone codePaysTelephone ,"
            + " indicatif_pays codePays,adresse, email, u.niveau, est_actif estActif,  pereuo_id as pereUOId,  telephone,  "
            + " typeuo_id as typeUOId,profil_acteur_id profilActeurId ,service_profil_id serviceProfilId , code_banque codeBanque,  uo_pays_id uoPaysId,"
            + "tu.code_type_uo as typeUOCode,tu.libelle_type_uo as typeUOLibelleTypeUO, tu.niveau as niveauTypeUo"
            + " FROM unite_organisation u,type_unite_organisation tu WHERE u.typeuo_id=tu.id and u.pereuo_id = CAST(CAST (:id AS TEXT) AS BIGINT ) and  u.typeuo_id= CAST(CAST (:typeId AS TEXT) AS BIGINT ) and supprime=false",
            resultSetMapping = "Mapping.UniteOrganisationDTOFindOne",hints={@QueryHint(name="org.hibernate.cacheable",value="false")})
    ,

        
        @NamedNativeQuery(name = "UniteOrganisation.findForViewListOptimise",
            query = "SELECT u.id,libelle_unite_organisation libelleUniteOrganisation,  est_siege estSiege, code_uo codeUo ,numero_ordre numeroOrdre,"
            + "code_fse codeFse,  est_filiale estFiliale ,  code_telephone codePaysTelephone,"
            + "indicatif_pays codePays,adresse, email, u.niveau, est_actif estActif,  pereuo_id as pereUOId,  telephone,"
            + "typeuo_id as typeUOId,profil_acteur_id profilActeurId ,service_profil_id serviceProfilId, code_banque codeBanque, uo_pays_id uoPaysId   , tu.code_type_uo typeUOCode, tu.libelle_type_uo typeUOLibelleTypeUO, tu.niveau niveauTypeUo"
            + " FROM unite_organisation u , type_unite_organisation tu WHERE u.typeuo_id=tu.id and tu.code_type_uo!='FSE' and tu.code_type_uo!='PAYS' and tu.code_type_uo!='ZONE' and (u.supprime is null or u.supprime=false)",
            resultSetMapping = "Mapping.UniteOrganisationDTO",hints={@QueryHint(name="org.hibernate.cacheable",value="false")}),

        @NamedNativeQuery(name = "UniteOrganisation.findAllOptimiseNew",
                query = "SELECT u.id, u.libelle_unite_organisation as libelleUniteOrganisation, u.est_siege as estSiege, u.code_uo as codeUo ,u.numero_ordre as numeroOrdre,u.feuille,"
                        + "u.code_fse as codeFse,  u.est_filiale as estFiliale , u.telephone, u.code_telephone as codePaysTelephone,"
                        + "u.indicatif_pays as codePays, u.adresse, u.email, u.niveau, tu.niveau as niveauTypeUo, u.est_actif as estActif,  u.pereuo_id as pereUOId,"
                        + "u.typeuo_id as typeUOId, u.profil_acteur_id as profilActeurId ,u.service_profil_id as serviceProfilId, u.code_banque as codeBanque, u.uo_pays_id as uoPaysId, "
                        + "tu.code_type_uo as typeUOCode, tu.libelle_type_uo as typeUOLibelleTypeUO, u.representant_id as representantId,"
                        + "u.date_creation as dateCreation, sp.nom as serviceProfilNom,"
                        + "pa.nom as profilActeurNom, u.groupe_profil_acteur_id as groupeProfilActeurId, gp.nom as groupeProfilActeurNom,u.ville_id as vlilleId,u.agt_id as agentId,u.idt_id as tagIdentityRef,u.mer_id marchandId,u.cos_id as cosId,u.uo_zone_id as uoZoneId,u.uo_fse_id as uoFseId,u.created_date as createdDate"
                        + " FROM unite_organisation u"
                        + " LEFT JOIN type_unite_organisation tu on u.typeuo_id=tu.id"
                        + " LEFT JOIN service_profil sp on u.service_profil_id=sp.id"
                        + " LEFT JOIN profil_acteur pa on u.profil_acteur_id=pa.id"
                        + " LEFT JOIN groupe_profil_acteur gp on u.groupe_profil_acteur_id=gp.id"
                        + " WHERE("
                        + "u.supprime=false) order by u.id desc",
                resultSetMapping = "Mapping.UniteOrganisationfindForViewPaginatedDTO",hints={@QueryHint(name="org.hibernate.cacheable",value="false")}),


        @NamedNativeQuery(name = "UniteOrganisation.findAllByDate",
                query = "SELECT u.id, libelle_unite_organisation libelleUniteOrganisation,  est_siege estSiege, code_uo codeUo ,numero_ordre numeroOrdre, "
                        + "code_fse codeFse,  est_filiale estFiliale ,  code_telephone codePaysTelephone ,"
                        + " indicatif_pays codePays,adresse, email, u.niveau, u.est_actif estActif,  pereuo_id as pereUOId,  telephone,  "
                        + " typeuo_id as typeUOId,profil_acteur_id profilActeurId ,service_profil_id serviceProfilId , code_banque codeBanque,  uo_pays_id uoPaysId,"
                        + "tu.code_type_uo as typeUOCode,tu.libelle_type_uo as typeUOLibelleTypeUO, tu.niveau as niveauTypeUo"
                        + " FROM unite_organisation u,type_unite_organisation tu,point_vente pv WHERE u.typeuo_id=tu.id and pv.unite_organisation_id=u.id and pv.id in (:pvIds) and supprime=false",
                resultSetMapping = "Mapping.UniteOrganisationDTOFindOne",hints={@QueryHint(name="org.hibernate.cacheable",value="false")})
        ,
        @NamedNativeQuery(name = "UniteOrganisation.findAllOptimiseForReport",
                query = "SELECT u.id, u.libelle_unite_organisation as libelleUniteOrganisation, u.est_siege as estSiege, u.code_uo as codeUo ,u.numero_ordre as numeroOrdre,u.feuille,"
                        + "u.code_fse as codeFse,  u.est_filiale as estFiliale , u.telephone, u.code_telephone as codePaysTelephone,"
                        + "u.indicatif_pays as codePays, u.adresse, u.email, u.niveau, tu.niveau as niveauTypeUo, u.est_actif as estActif,  u.pereuo_id as pereUOId,"
                        + "u.typeuo_id as typeUOId, u.profil_acteur_id as profilActeurId ,u.service_profil_id as serviceProfilId, u.code_banque as codeBanque, u.uo_pays_id as uoPaysId, "
                        + "tu.code_type_uo as typeUOCode, tu.libelle_type_uo as typeUOLibelleTypeUO, u.representant_id as representantId,"
                        + "u.date_creation as dateCreation, sp.nom as serviceProfilNom,"
                        + "pa.nom as profilActeurNom, u.groupe_profil_acteur_id as groupeProfilActeurId, gp.nom as groupeProfilActeurNom,u.ville_id as vlilleId,u.agt_id as agentId,u.idt_id as tagIdentityRef,u.mer_id marchandId,u.cos_id as cosId,u.uo_zone_id as uoZoneId,u.uo_fse_id as uoFseId"
                        + " FROM unite_organisation u"
                        + " LEFT JOIN type_unite_organisation tu on u.typeuo_id=tu.id"
                        + " LEFT JOIN service_profil sp on u.service_profil_id=sp.id"
                        + " LEFT JOIN profil_acteur pa on u.profil_acteur_id=pa.id"
                        + " LEFT JOIN groupe_profil_acteur gp on u.groupe_profil_acteur_id=gp.id"
                        + " WHERE("
                        + "(tu.code_type_uo!='FSE')"
                        + "and (tu.code_type_uo!='PAYS')"
                        + "and (tu.code_type_uo!='ZONE')"
                        + "and (:typeUO is null or  tu.code_type_uo= CAST(:typeUO AS TEXT))"
                        + "and(:debut is null or u.created_date <='2023-07-14 14:16:49.187' )"
                        + "and (u.supprime is null or u.supprime=false)) order by u.id desc limit :taille offset :page",
                resultSetMapping = "Mapping.UniteOrganisationfindForViewPaginatedDTO",hints={@QueryHint(name="org.hibernate.cacheable",value="false")})



})

@Entity
@EntityListeners(value = {EntityAuditEventListener.class})
@Table(name = "unite_organisation")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Cacheable(value = false)
public class UniteOrganisation extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "libelle_unite_organisation", nullable = false)
    private String libelleUniteOrganisation;

    @Column(name = "est_siege")
    private Boolean estSiege;

    @Column(name = "code_uo")
    private String codeUO;

    @Column(name = "numero_ordre")
    private Integer numeroOrdre;

    @Column(name = "feuille")
    private Boolean feuille;

    @Column(name = "code_fse")
    private String codeFse;

    @Column(name = "code_banque")
    private String codeBanque;

    @Column(name = "est_filiale")
    private Boolean estFiliale;

    @Column(name = "telephone")
    private String telephone;

    @Column(name = "code_telephone")
    private String codePaysTelephone;

    @Column(name = "indicatifPays")
    private String codePays;

    @Column(name = "adresse")
    private String adresse;

    @Column(name = "email")
    private String email;

    @Column(name = "niveau")
    private Integer niveau;

    @Column(name = "est_actif")
    private Boolean estActif;

    @Column(name = "supprime")
    private Boolean supprime = false;

    @Column(name = "date_creation")
    private LocalDate dateCreation;

    @Column(name = "mer_id")
    private Integer marchandId;

    @Column(name = "idt_id")
    private Integer tagIdentityRef;

    @Column(name = "agt_id")
    private Integer agentId;

    @Column(name = "cli_id")
    private Integer clientId;

    @Column(name = "cos_id")
    private Integer cosId;


    @Column(name = "uo_pays_id")
    private Long uoPaysId;

    @Column(name = "uo_zone_id")
    private Long uoZoneId;

    @Column(name = "uo_fse_id")
    private Long uoFseId;

    @Column(name="est_migrer")
    private Boolean estMigrer;

    @ManyToOne
    private UniteOrganisation pereUO;

    @ManyToOne
    private TypeUniteOrganisation typeUO;

    @ManyToOne
    private Personne representant;

    @ManyToOne
    @JsonIgnore
    private Ville ville;

    @ManyToOne
    private Commerciale commerciale;
    @ManyToOne
    private GroupeProfilActeur groupeProfilActeur;
    @ManyToOne
    private ProfilActeur profilActeur;

    @ManyToOne
    private FournisseurService fournisseur;

    // @ManyToOne(fetch = FetchType.LAZY)
    @ManyToOne
    ServiceProfil serviceProfil;
  @ManyToOne
     private GroupeFse groupeFse;

    @OneToOne
    private Localite localite;

    public UniteOrganisation() {
    }

    public UniteOrganisation(Long id, String libelleUniteOrganisation, Boolean estSiege, String codeUO, Integer numeroOrdre, Boolean feuille, String codeFse, String codeBanque, Boolean estFiliale, String telephone, String codePaysTelephone, String codePays, String adresse, String email, Integer niveau, Boolean estActif, LocalDate dateCreation, Integer marchandId, Integer tagIdentityRef, Integer agentId, Integer clientId, UniteOrganisation pereUO, TypeUniteOrganisation typeUO, Personne representant, Ville ville, Commerciale commerciale, GroupeProfilActeur groupeProfilActeur, ProfilActeur profilActeur, FournisseurService fournisseur, ServiceProfil serviceProfil, Localite localite) {
        this.id = id;
        this.libelleUniteOrganisation = libelleUniteOrganisation;
        this.estSiege = estSiege;
        this.codeUO = codeUO;
        this.numeroOrdre = numeroOrdre;
        this.feuille = feuille;
        this.codeFse = codeFse;
        this.codeBanque = codeBanque;
        this.estFiliale = estFiliale;
        this.telephone = telephone;
        this.codePaysTelephone = codePaysTelephone;
        this.codePays = codePays;
        this.adresse = adresse;
        this.email = email;
        this.niveau = niveau;
        this.estActif = estActif;
        this.dateCreation = dateCreation;
        this.marchandId = marchandId;
        this.tagIdentityRef = tagIdentityRef;
        this.agentId = agentId;
        this.clientId = clientId;
        this.pereUO = pereUO;
        this.typeUO = typeUO;
        this.representant = representant;
        this.ville = ville;
        this.commerciale = commerciale;
        this.groupeProfilActeur = groupeProfilActeur;
        this.profilActeur = profilActeur;
        this.fournisseur = fournisseur;
        this.serviceProfil = serviceProfil;
        this.localite = localite;
    }

    public Long getUoPaysId() {
        return uoPaysId;
    }

    public void setUoPaysId(Long uoPaysId) {
        this.uoPaysId = uoPaysId;
    }

    public String getCodeBanque() {
        return codeBanque;
    }

    public void setCodeBanque(String codeBanque) {
        this.codeBanque = codeBanque;
    }

    public UniteOrganisation codeBanque(String codeBanque) {
        this.codeBanque = codeBanque;
        return this;
    }

    public Integer getMarchandId() {
        return marchandId;
    }

    public void setMarchandId(Integer marchandId) {
        this.marchandId = marchandId;
    }

    public Integer getTagIdentityRef() {
        return tagIdentityRef;
    }

    public void setTagIdentityRef(Integer tagIdentityRef) {
        this.tagIdentityRef = tagIdentityRef;
    }

    public Integer getAgentId() {
        return agentId;
    }

    public void setAgentId(Integer agentId) {
        this.agentId = agentId;
    }

    public Integer getClientId() {
        return clientId;
    }

    public void setClientId(Integer clientId) {
        this.clientId = clientId;
    }

    public Boolean getSupprime() {
        return supprime;
    }

    public void setSupprime(Boolean supprime) {
        this.supprime = supprime;
    }

    public Boolean getEstSiege() {
        return estSiege;
    }

    public Boolean getFeuille() {
        return feuille;
    }

    public Boolean getEstFiliale() {
        return estFiliale;
    }

    public Commerciale getCommerciale() {
        return commerciale;
    }

    public void setCommerciale(Commerciale commerciale) {
        this.commerciale = commerciale;
    }

    public ServiceProfil getServiceProfil() {
        return serviceProfil;
    }

    public void setServiceProfil(ServiceProfil serviceProfil) {
        this.serviceProfil = serviceProfil;
    }

    public String getCodePays() {
        return codePays;
    }

    public void setCodePays(String codePays) {
        this.codePays = codePays;
    }

    public Ville getVille() {
        return ville;
    }

    public void setVille(Ville ville) {
        this.ville = ville;
    }

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public ProfilActeur getProfilActeur() {
        return profilActeur;
    }

    public void setProfilActeur(ProfilActeur profilActeur) {
        this.profilActeur = profilActeur;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public UniteOrganisation adresse(String adresse) {
        this.adresse = adresse;
        return this;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLibelleUniteOrganisation() {
        return libelleUniteOrganisation;
    }

    public UniteOrganisation libelleUniteOrganisation(String libelleUniteOrganisation) {
        this.libelleUniteOrganisation = libelleUniteOrganisation;
        return this;
    }

    public void setLibelleUniteOrganisation(String libelleUniteOrganisation) {
        this.libelleUniteOrganisation = libelleUniteOrganisation;
    }

    public Boolean isEstSiege() {
        return estSiege;
    }

    public UniteOrganisation estSiege(Boolean estSiege) {
        this.estSiege = estSiege;
        return this;
    }

    public LocalDate getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDate dateCreation) {
        this.dateCreation = dateCreation;
    }

    public void setEstSiege(Boolean estSiege) {
        this.estSiege = estSiege;
    }

    public String getCodeUO() {
        return codeUO;
    }

    public UniteOrganisation codeUO(String codeUO) {
        this.codeUO = codeUO;
        return this;
    }

    public void setCodeUO(String codeUO) {
        this.codeUO = codeUO;
    }

    public Integer getNumeroOrdre() {
        return numeroOrdre;
    }

    public UniteOrganisation numeroOrdre(Integer numeroOrdre) {
        this.numeroOrdre = numeroOrdre;
        return this;
    }

    public void setNumeroOrdre(Integer numeroOrdre) {
        this.numeroOrdre = numeroOrdre;
    }

    public Boolean isFeuille() {
        return feuille;
    }

    public UniteOrganisation feuille(Boolean feuille) {
        this.feuille = feuille;
        return this;
    }

    public void setFeuille(Boolean feuille) {
        this.feuille = feuille;
    }

    public String getCodeFse() {
        return codeFse;
    }

    public Localite getLocalite() {
        return localite;
    }

    public void setLocalite(Localite localite) {
        this.localite = localite;
    }

    public UniteOrganisation codeFse(String codeFse) {
        this.codeFse = codeFse;
        return this;
    }

    public void setCodeFse(String codeFse) {
        this.codeFse = codeFse;
    }

    public Boolean isEstFiliale() {
        return estFiliale;
    }

    public UniteOrganisation estFiliale(Boolean estFiliale) {
        this.estFiliale = estFiliale;
        return this;
    }

    public void setEstFiliale(Boolean estFiliale) {
        this.estFiliale = estFiliale;
    }

    public String getTelephone() {
        return telephone;
    }

    public UniteOrganisation telephone(String telephone) {
        this.telephone = telephone;
        return this;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getEmail() {
        return email;
    }

    public UniteOrganisation email(String email) {
        this.email = email;
        return this;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getNiveau() {
        return niveau;
    }

    public UniteOrganisation niveau(Integer niveau) {
        this.niveau = niveau;
        return this;
    }

    public void setNiveau(Integer niveau) {
        this.niveau = niveau;
    }

    public Long getUoZoneId() {
        return uoZoneId;
    }

    public void setUoZoneId(Long uoZoneId) {
        this.uoZoneId = uoZoneId;
    }

    public Long getUoFseId() {
        return uoFseId;
    }

    public void setUoFseId(Long uoFseId) {
        this.uoFseId = uoFseId;
    }

    public UniteOrganisation getPereUO() {
        return pereUO;
    }

    public UniteOrganisation pereUO(UniteOrganisation uniteOrganisation) {
        this.pereUO = uniteOrganisation;
        return this;
    }

    public void setPereUO(UniteOrganisation uniteOrganisation) {
        this.pereUO = uniteOrganisation;
    }

    public TypeUniteOrganisation getTypeUO() {
        return typeUO;
    }

    public UniteOrganisation typeUO(TypeUniteOrganisation typeUniteOrganisation) {
        this.typeUO = typeUniteOrganisation;
        return this;
    }

    public void setTypeUO(TypeUniteOrganisation typeUniteOrganisation) {
        this.typeUO = typeUniteOrganisation;
    }

    public Personne getRepresentant() {
        return representant;
    }

    public UniteOrganisation representant(Personne personne) {
        this.representant = personne;
        return this;
    }

    public void setRepresentant(Personne personne) {
        this.representant = personne;
    }

    public FournisseurService getFournisseur() {
        return fournisseur;
    }

    public UniteOrganisation fournisseur(FournisseurService fournisseurService) {
        this.fournisseur = fournisseurService;
        return this;
    }

    public void setFournisseur(FournisseurService fournisseurService) {
        this.fournisseur = fournisseurService;
    }

    public Boolean getEstActif() {
        return estActif;
    }

    public void setEstActif(Boolean estActif) {
        this.estActif = estActif;
    }

    public GroupeProfilActeur getGroupeProfilActeur() {
        return groupeProfilActeur;
    }

    public void setGroupeProfilActeur(GroupeProfilActeur groupeProfilActeur) {
        this.groupeProfilActeur = groupeProfilActeur;
    }

    public String getCodePaysTelephone() {
        return codePaysTelephone;
    }

    public void setCodePaysTelephone(String codePaysTelephone) {
        this.codePaysTelephone = codePaysTelephone;
    }

    public Integer getCosId() {
        return cosId;
    }

    public void setCosId(Integer cosId) {
        this.cosId = cosId;
    }

    public Boolean getEstMigrer() {
        return estMigrer;
    }

    public void setEstMigrer(Boolean estMigrer) {
        this.estMigrer = estMigrer;
    }

    
      public GroupeFse getGroupeFse() {
        return groupeFse;
    }

    public void setGroupeFse(GroupeFse groupeFse) {
        this.groupeFse = groupeFse;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UniteOrganisation uniteOrganisation = (UniteOrganisation) o;
        if (uniteOrganisation.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), uniteOrganisation.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "UniteOrganisation{"
                + "id=" + getId()
                + ", libelleUniteOrganisation='" + getLibelleUniteOrganisation() + "'"
                + ", estSiege='" + isEstSiege() + "'"
                + ", codeUO='" + getCodeUO() + "'"
                + ", numeroOrdre=" + getNumeroOrdre()
                + ", feuille='" + isFeuille() + "'"
                + ", codeFse='" + getCodeFse() + "'"
                + ", estFiliale='" + isEstFiliale() + "'"
                + ", telephone='" + getTelephone() + "'"
                + ", email='" + getEmail() + "'"
                + ", niveau=" + getNiveau()
                + "}";
    }
}
