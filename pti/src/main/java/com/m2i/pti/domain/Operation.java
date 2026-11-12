package com.m2i.pti.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.ColumnResult;
import jakarta.persistence.ConstructorResult;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedNativeQueries;
import jakarta.persistence.NamedNativeQuery;
import jakarta.persistence.OneToOne;
import jakarta.persistence.QueryHint;
import jakarta.persistence.SqlResultSetMapping;
import jakarta.persistence.SqlResultSetMappings;
import jakarta.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.m2i.pti.config.audit.EntityAuditEventListener;
import com.m2i.pti.domain.converter.ColumnCryptDecrypt;
import com.m2i.pti.domain.enumeration.Etape;
import com.m2i.pti.domain.enumeration.StatutOperation;
import com.m2i.pti.mobile.dto.MOperationNumbLastestDTO;
import com.m2i.pti.service.dto.OperationDTO;
import com.m2i.pti.service.dto.OperationOptimiseDTO;
import com.m2i.pti.service.dto.OperationStatutDTO;

/**
 * A Operation.
 */
@SqlResultSetMappings({
    @SqlResultSetMapping(name = "Mapping.OperationOptimiseDTO",
            classes = @ConstructorResult(targetClass = OperationOptimiseDTO.class,
                    columns = {
                        @ColumnResult(name = "id", type = Long.class)
                        ,
                                @ColumnResult(name = "montantOp", type = BigDecimal.class)
                        ,
                                @ColumnResult(name = "codeOperation", type = String.class)
                        ,
                                @ColumnResult(name = "dateOperation", type = ZonedDateTime.class)
                        ,
                                @ColumnResult(name = "statut", type = String.class)
                        ,
                                @ColumnResult(name = "codeService", type = String.class)
                        ,
                                @ColumnResult(name = "devisePaiementLibelle", type = String.class)
                    }))
    ,
    
     @SqlResultSetMapping(name = "Mapping.OperationNumbLastestDTO",
            classes = @ConstructorResult(targetClass = MOperationNumbLastestDTO.class,
                    columns = {
                        @ColumnResult(name = "codeOperation", type = String.class)
                        ,
                                @ColumnResult(name = "montantOp", type = BigDecimal.class)
                        ,
                                @ColumnResult(name = "description", type = String.class)

                    }))
    ,
        @SqlResultSetMapping(name = "Mapping.OperationByDashbordFindAllDTO",
            classes = @ConstructorResult(targetClass = OperationOptimiseDTO.class,
                    columns = {
                        @ColumnResult(name = "id", type = Long.class)
                        ,
                                @ColumnResult(name = "montantOp", type = BigDecimal.class)
                        ,
                                @ColumnResult(name = "codeOperation", type = String.class)
                        ,
                                @ColumnResult(name = "dateOperation", type = ZonedDateTime.class)
                        ,
                                @ColumnResult(name = "statut", type = String.class)
                        ,
                                @ColumnResult(name = "codeService", type = String.class)
                    }))
    ,
  @SqlResultSetMapping(name = "Mapping.OperationCodeExterne",
            classes = @ConstructorResult(targetClass = OperationOptimiseDTO.class,
                    columns = {
                        @ColumnResult(name = "id", type = Long.class)
                        ,
                                @ColumnResult(name = "montantOp", type = BigDecimal.class)
                        ,
                                @ColumnResult(name = "codeOperation", type = String.class)
                        ,
                                @ColumnResult(name = "dateOperation", type = ZonedDateTime.class)
                        ,
                                @ColumnResult(name = "statut", type = String.class)
                        ,
                                     @ColumnResult(name = "codeExterne", type = String.class)

                        ,
                                @ColumnResult(name = "typeService", type = Long.class)
                    }))
    ,

        @SqlResultSetMapping(name = "Mapping.OperationAllDTO",
            classes = @ConstructorResult(targetClass = OperationDTO.class,
                    columns = {
                        @ColumnResult(name = "id", type = Long.class)
                        ,
                                @ColumnResult(name = "montantOp", type = BigDecimal.class)
                        ,
                                @ColumnResult(name = "montantFrais", type = BigDecimal.class)
                        ,
                                @ColumnResult(name = "montantFraisClient", type = BigDecimal.class)
                        ,
                                @ColumnResult(name = "montantFraisEm", type = BigDecimal.class)
                        ,
                                @ColumnResult(name = "montantFraisAcq", type = BigDecimal.class)
                        ,
                                @ColumnResult(name = "montantTaxe", type = BigDecimal.class)
                        ,
                                @ColumnResult(name = "dateOperation", type = ZonedDateTime.class)
                        ,
                                @ColumnResult(name = "statut", type = String.class)
                        ,
                                @ColumnResult(name = "etape", type = String.class)
                        ,
                                @ColumnResult(name = "codeRetrait", type = String.class)
                        ,
                                @ColumnResult(name = "secret", type = String.class)
                        ,
                                @ColumnResult(name = "codeOperation", type = String.class)
                        ,
                                @ColumnResult(name = "dateExpiration", type = LocalDate.class)
                        ,
                                @ColumnResult(name = "motif", type = String.class)
                        ,
                                @ColumnResult(name = "codeExterne", type = String.class)
                        ,
                                @ColumnResult(name = "typeServiceId", type = Long.class)
                        ,
                                @ColumnResult(name = "code", type = String.class)
                        ,
                                @ColumnResult(name = "emetteurId", type = Long.class)
                        ,
                                @ColumnResult(name = "pointVenteId", type = Long.class)
                        ,
                                @ColumnResult(name = "paysId", type = Long.class)
                        ,
                                @ColumnResult(name = "canalId", type = Long.class)
                        ,
                                @ColumnResult(name = "uoAcquereurId", type = Long.class)
                        ,
                                @ColumnResult(name = "uoEmetteurId", type = Long.class)
                        ,
                                @ColumnResult(name = "uoBeneficiaireId", type = Long.class)
                        ,
                                @ColumnResult(name = "montantOpEnDevise", type = BigDecimal.class)
                        ,
                                @ColumnResult(name = "devisePaiementId", type = Long.class)
                        ,
                                @ColumnResult(name = "compteSrcId", type = Long.class)
                        ,
                                @ColumnResult(name = "compteDestId", type = Long.class)
                        ,
                                @ColumnResult(name = "commentaire", type = String.class)
                        ,
                                @ColumnResult(name = "userId", type = Long.class)
                        ,
                                @ColumnResult(name = "createdDate", type = Instant.class)
                        ,
                                @ColumnResult(name = "createdBy", type = String.class)
                        ,
                                @ColumnResult(name = "lastModifiedBy", type = String.class)
                        ,
                                @ColumnResult(name = "lastModifiedDate", type = Instant.class)
                        ,
                                @ColumnResult(name = "motifOperationId", type = Long.class)
                        ,
                                @ColumnResult(name = "beneficaireId", type = Long.class)
                        ,
                                @ColumnResult(name = "description", type = String.class)
                    }))
    ,

        @SqlResultSetMapping(name = "Mapping.OperationAllTestDTO",
            classes = @ConstructorResult(targetClass = OperationDTO.class,
                    columns = {
                        @ColumnResult(name = "id", type = Long.class)
                        ,
                                @ColumnResult(name = "montantOp", type = BigDecimal.class)
                        ,
                                @ColumnResult(name = "montantFrais", type = BigDecimal.class)
                        ,
                                @ColumnResult(name = "montantFraisClient", type = BigDecimal.class)
                        ,
                                @ColumnResult(name = "montantFraisEm", type = BigDecimal.class)
                        ,
                                @ColumnResult(name = "montantFraisAcq", type = BigDecimal.class)
                        ,
                                @ColumnResult(name = "montantTaxe", type = BigDecimal.class)

                    }))
    ,

        @SqlResultSetMapping(name = "Mapping.OperationIdDTO",
            classes = @ConstructorResult(targetClass = OperationDTO.class,
                    columns = {
                        @ColumnResult(name = "id", type = Long.class)
                        ,
                                @ColumnResult(name = "montantOp", type = BigDecimal.class)
                        ,
                                @ColumnResult(name = "montantFrais", type = BigDecimal.class)
                        ,
                                @ColumnResult(name = "montantFraisClient", type = BigDecimal.class)
                        ,
                                @ColumnResult(name = "montantFraisEm", type = BigDecimal.class)
                        ,
                                @ColumnResult(name = "montantFraisAcq", type = BigDecimal.class)
                        ,
                                @ColumnResult(name = "montantTaxe", type = BigDecimal.class)
                        ,
                                @ColumnResult(name = "dateOperation", type = ZonedDateTime.class)
                        ,
                                @ColumnResult(name = "statut", type = String.class)
                        ,
                                @ColumnResult(name = "etape", type = String.class)
                        ,
                                @ColumnResult(name = "codeRetrait", type = String.class)
                        ,
                                @ColumnResult(name = "secret", type = String.class)
                        ,
                                @ColumnResult(name = "codeOperation", type = String.class)
                        ,
                                @ColumnResult(name = "dateExpiration", type = LocalDate.class)
                        ,
                                @ColumnResult(name = "motif", type = String.class)
                        ,
                                @ColumnResult(name = "codeExterne", type = String.class)
                        ,
                                @ColumnResult(name = "typeServiceId", type = Long.class)
                        ,
                                @ColumnResult(name = "emetteurId", type = Long.class)
                        ,
                                @ColumnResult(name = "pointVenteId", type = Long.class)
                        ,
                                @ColumnResult(name = "paysId", type = Long.class)
                        ,
                                @ColumnResult(name = "canalId", type = Long.class)
                        ,
                                @ColumnResult(name = "uoAcquereurId", type = Long.class)
                        ,
                                @ColumnResult(name = "uoEmetteurId", type = Long.class)
                        ,
                                @ColumnResult(name = "uoBeneficiaireId", type = Long.class)
                        ,
                                @ColumnResult(name = "montantOpEnDevise", type = BigDecimal.class)
                        ,
                                @ColumnResult(name = "devisePaiementId", type = Long.class)
                        ,
                                @ColumnResult(name = "compteSrcId", type = Long.class)
                        ,
                                @ColumnResult(name = "compteDestId", type = Long.class)
                        ,
                                @ColumnResult(name = "commentaire", type = String.class)
                        ,
                                @ColumnResult(name = "userId", type = Long.class)
                        ,
                                @ColumnResult(name = "createdDate", type = Instant.class)
                        ,
                                @ColumnResult(name = "createdBy", type = String.class)
                        ,
                                @ColumnResult(name = "motifOperationId", type = Long.class)
                        ,
                                @ColumnResult(name = "beneficaireId", type = Long.class)
                        ,
                                @ColumnResult(name = "pieceEmetteurId", type = Long.class)
                        ,
                                @ColumnResult(name = "pieceBeneficiaireId", type = Long.class)
                        ,
                                @ColumnResult(name = "description", type = String.class),}))
    ,


        @SqlResultSetMapping(name = "Mapping.OperationLastestDTO",
            classes = @ConstructorResult(targetClass = OperationDTO.class,
                    columns = {
                        @ColumnResult(name = "id", type = Long.class)
                        ,
                                @ColumnResult(name = "montantOp", type = BigDecimal.class)
                        ,
                                @ColumnResult(name = "dateOperation", type = ZonedDateTime.class)
                        ,
                                @ColumnResult(name = "pointVenteId", type = Long.class),}
            )
    )
    ,
@SqlResultSetMapping(name = "Mapping.OperationStatutDTO",
            classes = @ConstructorResult(targetClass = OperationStatutDTO.class,
                    columns = {
                        @ColumnResult(name = "action_service", type = String.class)
                        ,
                                    @ColumnResult(name = "etape", type = String.class)
                    })),})

@NamedNativeQueries({
    @NamedNativeQuery(name = "operation.findTop10LastOperation",
            query = "SELECT op.id, op.montant_op as montantOp, op.code_operation as codeOperation, op.date_operation as dateOperation, op.statut, t.code as codeService, d.libelle as devisePaiementLibelle "
            + "FROM operation op LEFT JOIN type_service t ON op.type_service_id=t.id  LEFT JOIN devise d ON op.devise_paiement_id=d.id"
            + " WHERE op.statut in:statut and op.point_vente_id in:pointVenteIds "
            + "order by op.date_operation desc limit 10",
            resultSetMapping = "Mapping.OperationOptimiseDTO", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})
    ,
     @NamedNativeQuery(name = "operation.OperationCodeExterne",
            query = "SELECT op.id, op.montant_op as montantOp, op.code_operation as codeOperation, op.date_operation as dateOperation, op.statut, op.type_service_id as typeService, op.code_externe as  codeExterne  FROM operation op  WHERE op.code_externe=:codeExterne",
            resultSetMapping = "Mapping.OperationCodeExterne", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})
    ,

        @NamedNativeQuery(name = "operation.findAllOperations",
            query = "SELECT op.id, op.montant_op as montantOp,op.montant_frais as montantFrais, op.montant_frais_client as montantFraisClient,"
            + "op.montant_frais_em as montantFraisEm,op.montant_frais_acq as montantFraisAcq, op.montant_taxe as montantTaxe,"
            + " op.statut as statut, op.etape as etape, op.code_retrait as codeRetrait, op.secret as secret, op.code_operation as codeOperation,op.date_expiration as dateExpiration,"
            + " op.date_operation as dateOperation, op.motif as motif, ts.code as code, op.code_externe as codeExterne, op.type_service_id as typeServiceId,"
            + "op.emetteur_id as emetteurId,op.point_vente_id as pointVenteId,op.pays_id as paysId,op.canal_id as canalId,"
            + "op.uo_acquereur_id as uoAcquereurId,op.uo_emetteur_id as uoEmetteurId,op.uo_beneficiaire_id as uoBeneficiaireId,"
            + "op.montant_op_en_devise as montantOpEnDevise,op.devise_paiement_id as devisePaiementId,"
            + "op.compte_src_id as compteSrcId, op.compte_dest_id as compteDestId,op.commentaire as commentaire, op.user_id as userId,"
            + "op.created_date as createdDate,op.created_by as createdBy,op.last_modified_by as lastModifiedBy,op.last_modified_date as lastModifiedDate, op.motif_operation_id as motifOperationId,op.beneficaire_id as beneficaireId,op.description"
            + " FROM operation op"
            + " INNER JOIN type_service ts ON op.type_service_id=ts.id "
            + " WHERE ("
            + " (:tsId is null or op.type_service_id = CAST(CAST (:tsId AS TEXT) AS BIGINT ))"
            + "and (:canalId is null or op.canal_id= CAST(CAST (:canalId AS TEXT) AS BIGINT ))"
            + "and (:uoAcquereurId is null or op.uo_acquereur_id= CAST(CAST (:uoAcquereurId AS TEXT) AS BIGINT )) "
            + "and (op.point_vente_id in :pointVenteIds) "
            + "and (:montantOp is null or op.montant_op= CAST(CAST (:montantOp AS TEXT) AS NUMERIC)) "
            + "and (op.statut in :statut)"
            + "and (:codeOperation is null or op.code_operation= CAST (:codeOperation AS TEXT)) "
            + "and (:description is null or op.description like CONCAT ('%',:description,'%')  ) "
            + "and (CAST(CAST(:dateOperation as text) as timestamp) is null or op.date_operation BETWEEN CAST(CAST(:dateOperation AS text) AS timestamp ) and CAST(CAST(:dateOperation2 AS text) AS timestamp ))) order by op.date_operation desc limit :taille offset :page",
            resultSetMapping = "Mapping.OperationAllDTO", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})
    ,

        @NamedNativeQuery(name = "operation.findAllOperationsByPeriode",
            query = "SELECT op.id, op.montant_op as montantOp,op.code_operation as codeOperation,op.date_operation as dateOperation,op.statut as statut,ts.code as codeService"
            + " FROM operation op,type_service ts where op.type_service_id=ts.id and op.date_operation is not null limit :taille offset :page",
            resultSetMapping = "Mapping.OperationByDashbordFindAllDTO", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})
    ,

        @NamedNativeQuery(name = "operation.findByOperationId",
            query = "SELECT op.id, op.montant_op as montantOp,op.montant_frais as montantFrais, op.montant_frais_client as montantFraisClient,"
            + "op.montant_frais_em as montantFraisEm,op.montant_frais_acq as montantFraisAcq, op.montant_taxe as montantTaxe,"
            + " op.statut as statut, op.etape as etape, op.code_retrait as codeRetrait, op.secret as secret, op.code_operation as codeOperation,op.date_expiration as dateExpiration,"
            + " op.date_operation as dateOperation, op.motif as motif, op.code_externe as codeExterne, op.type_service_id as typeServiceId,"
            + "op.emetteur_id as emetteurId,op.point_vente_id as pointVenteId,op.pays_id as paysId,op.canal_id as canalId,"
            + "op.uo_acquereur_id as uoAcquereurId,op.uo_emetteur_id as uoEmetteurId,op.uo_beneficiaire_id as uoBeneficiaireId,"
            + "op.montant_op_en_devise as montantOpEnDevise,op.devise_paiement_id as devisePaiementId,"
            + "op.compte_src_id as compteSrcId, op.compte_dest_id as compteDestId,op.commentaire as commentaire, op.user_id as userId,"
            + "op.created_date as createdDate,op.created_by as createdBy, op.motif_operation_id as motifOperationId,op.beneficaire_id as beneficaireId,"
            + "op.piece_emetteur_id as pieceEmetteurId, op.piece_beneficiaire_id as pieceBeneficiaireId,op.description"
            + " FROM operation op"
            + " INNER JOIN type_service ts ON op.type_service_id=ts.id "
            + " WHERE op.id=CAST(CAST (:id AS TEXT) AS BIGINT )",
            resultSetMapping = "Mapping.OperationIdDTO", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})
    ,

        @NamedNativeQuery(name = "operation.findAllOperationByAndTypseServiceAndPointVente",
            query = "SELECT op.id, op.montant_op as montantOp, op.code_operation as codeOperation, op.date_operation as dateOperation, op.statut, ts.code as codeService"
            + " FROM operation op ,type_service ts, point_vente pv"
            + " WHERE op.type_service_id =:tsId and op.point_vente_id in :pvId and op.date_operation BETWEEN :dateOperation and :dateOperation2 and op.statut =:etape",
            resultSetMapping = "Mapping.OperationByDashbordFindAllDTO", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})
    ,



        @NamedNativeQuery(name = "operation.findByValeurCompteSrcAndValeurCompteDest",
            query = "SELECT op.id, op.montant_op as montantOp, op.code_operation as codeOperation, op.date_operation as dateOperation, op.statut, t.code as codeService, d.libelle as devisePaiementLibelle"
            + " FROM operation op left join compte src on op.compte_src_id=src.id left join compte dest on op.compte_dest_id=dest.id LEFT JOIN type_service t ON op.type_service_id=t.id  LEFT JOIN devise d ON op.devise_paiement_id=d.id  "
            + "WHERE src.valeur_compte=:valeurCompteSrc or dest.valeur_compte=:valeurCompteDest  "
            + "order by op.date_operation desc limit 1 ",
            resultSetMapping = "Mapping.OperationOptimiseDTO", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})
    ,

        @NamedNativeQuery(name = "operation.findlastestOperation",
            query = "SELECT o.id, o.montant_op as montantOp, o.date_operation as dateOperation, o.point_vente_id as pointVenteId from operation o where o.point_vente_id=:id  ORDER BY o.date_operation DESC LIMIT 1",
            resultSetMapping = "Mapping.OperationLastestDTO")
    ,

        @NamedNativeQuery(name = "operation.findOperationByUoIdAndServiceIds",
            query = "select o.id id, o.montant_op montantOp,o.date_operation as dateOperation, o.point_vente_id as pointVenteId from operation o  inner join type_service tp_s on o.type_service_id = tp_s.id inner join point_vente pv on pv.id = o.point_vente_id where tp_s.id in (:serviceIds) and pv.unite_organisation_id=:uoId",
            resultSetMapping = "Mapping.OperationLastestDTO"
    )
    ,
        @NamedNativeQuery(name = "operation.findAllOperationByMultiParamsOptimiser",
            query = "select op.id id, op.montant_op montantOp,op.date_operation as dateOperation, op.point_vente_id as pointVenteId from operation op  inner join type_service tp_s on op.type_service_id = tp_s.id inner join point_vente pv on pv.id = op.point_vente_id inner join canal ca on op.canal_id=ca.id inner join unite_organisation uo on op.uo_acquereur_id=uo.id where  (tp_s.id=:typeServiceId or :typeServiceId is null) and (ca.id=:canalId or :canalId is null) and (uo.id=:uoAcquereurId or :uoAcquereurId is null) and (pv.id in :pointVenteIds or :pointVenteIds is null) and (op.montantOp=:montantOp or :montantOp is null) and (op.statut=:statut or :statut is null) and (op.dateOperation between :dateOperation and  :dateOperation2 or cast(:dateOperation  as timestamp) is null) order by dateOperation desc",
            resultSetMapping = "Mapping.OperationLastestDTO"
    )
    ,

         @NamedNativeQuery(name = "operation.findLast3Operation",
            query = "SELECT op.id, op.montant_op as montantOp, op.code_operation as codeOperation, op.date_operation as dateOperation, op.statut, t.code as codeService,d.libelle as devisePaiementLibelle FROM operation op LEFT JOIN type_service t ON op.type_service_id=t.id LEFT JOIN devise d ON op.devise_paiement_id=d.id inner join emetteur em on em.id = op.emetteur_id inner join personne pers on pers.id = em.personne_id WHERE pers.id = CAST (CAST(:personneId AS TEXT) AS BIGINT) order by op.date_operation desc limit 3",
            resultSetMapping = "Mapping.OperationOptimiseDTO", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})
    ,
        
 @NamedNativeQuery(name = "operation.findLesTroisDernierOperation",
            query = "SELECT op.montant_op as montantOp, op.code_operation as codeOperation, CONCAT(t.code , ' ', op.date_operation) AS description  FROM operation op LEFT JOIN type_service t ON op.type_service_id=t.id inner join emetteur em on em.id = op.emetteur_id inner join personne pers on pers.id = em.personne_id WHERE pers.id = CAST (CAST(:personneId AS TEXT) AS BIGINT) and op.statut='PAYE' order by op.date_operation desc limit 3",
            resultSetMapping = "Mapping.OperationNumbLastestDTO", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})
    ,
 
@NamedNativeQuery(name = "operation.findByCompteIdSrcAndDest",
            query = "SELECT op.id, op.montant_op as montantOp, op.date_operation as dateOperation, op.point_vente_id as pointVenteId"
            + " FROM operation op "
            + "WHERE (op.compte_src_id=:compteId or op.compte_dest_id=:compteId) limit 1 ",
            resultSetMapping = "Mapping.OperationLastestDTO", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")}
    )
    ,

@NamedNativeQuery(name = "operation.getOperationStatut",
            //query = "select es.action_service, op.etape from operation_etape op inner join etape_service es on es.id = op.etape_service_id where op.operation_id = CAST(CAST (:operationId AS TEXT) AS BIGINT )",
            query = "SELECT es.action_service,op.etape,\n"
            + "CASE WHEN op.etape_service_id IS NOT NULL AND es.id IS NOT NULL THEN 'PROCEED'\n"
            + "WHEN op.etape_service_id IS NULL THEN 'INIT'\n"
            + "ELSE 'PROCEED'\n"
            + "END AS statut FROM etape_service es LEFT JOIN operation_etape op ON es.id = op.etape_service_id AND op.operation_id = CAST(CAST (:operationId AS TEXT) AS BIGINT )\n"
            + "UNION SELECT es2.action_service,op2.etape,'PROCEED' AS statut FROM operation_etape op2 LEFT JOIN etape_service es2 ON es2.id = op2.etape_service_id\n"
            + "WHERE op2.operation_id = CAST(CAST (:operationId AS TEXT) AS BIGINT ) AND es2.id IS NULL",
            resultSetMapping = "Mapping.OperationStatutDTO", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")}
    )
    ,
@NamedNativeQuery(name = "operation.findOperationReglementEnAttente",
            query = "SELECT op.id, op.montant_op as montantOp, op.code_operation as codeOperation, op.date_operation as dateOperation, op.statut, t.code as codeService, d.libelle as devisePaiementLibelle "
            + "FROM operation op LEFT JOIN type_service t ON op.type_service_id=t.id  LEFT JOIN devise d ON op.devise_paiement_id=d.id"
            + " WHERE op.montant_op>=0 and op.statut=:statut and op.type_service_id=:typeServiceId "
            + "order by op.date_operation desc",
            resultSetMapping = "Mapping.OperationOptimiseDTO", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})
})

@Entity
@Table(name = "operation")
@DynamicInsert
@DynamicUpdate
@EntityListeners(value = {EntityAuditEventListener.class})
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Operation extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "montant_op", precision = 20, scale = 2)
    private BigDecimal montantOp;

    @Column(name = "montant_op_en_devise", scale = 2)
    private BigDecimal montantOpEnDevise;

    @Column(name = "montant_frais", precision = 20, scale = 2)
    private BigDecimal montantFrais;

    @Column(name = "montant_taxe", precision = 20, scale = 2)
    private BigDecimal montantTaxe;

    @Column(name = "montant_frais_em", precision = 20, scale = 2)
    private BigDecimal montantFraisEm;

    @Column(name = "montant_frais_acq", precision = 20, scale = 2)
    private BigDecimal montantFraisAcq;

    @Column(name = "date_operation")
    private ZonedDateTime dateOperation;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut")
    private StatutOperation statut;

    @Column(name = "code_retrait")
    private String codeRetrait;

    @Column(name = "secret")
    private String secret;

    @Convert(converter = ColumnCryptDecrypt.class)
    @Column(name = "codeSecret")
    private String codeSecret;
    @Column(name = "code_operation")
    private String codeOperation;

    @Column(name = "date_expiration")
    private LocalDate dateExpiration;

    @Column(name = "motif")
    private String motif;

    @Column(name = "duree", nullable = true)
    private int duree = 0;

    @Column(name = "code_externe")
    private String codeExterne;

    @Column(name = "montant_frais_client")
    private BigDecimal montantFraisClient = BigDecimal.ZERO;

    @Column(name = "montant_avoir_client")
    private BigDecimal montantAvoirClient = BigDecimal.ZERO;

    @Column(name = "trf_id")
    private Integer tagTransitId;

    @Enumerated(EnumType.STRING)
    @Column(name = "etape")
    private Etape etape;

    @Column(name = "error")
    private String error;

    @Column(name = "description")
    private String description;
    @Column(columnDefinition = "TEXT")
    private String extraData;

    @Column(name = "narration")
    private String narration;

    @ManyToOne
    private TypeService typeService;

    @ManyToOne
    private MotifOperation motifOperation;

    @ManyToOne
    private Beneficiaire beneficaire;

    @ManyToOne
    private Emetteur emetteur;

    @ManyToOne
    private PointVente pointVente;

    @ManyToOne
    private Pays pays;

    @ManyToOne
    private Canal canal;

    @ManyToOne
    private UniteOrganisation uoAcquereur;

    @ManyToOne
    private UniteOrganisation uoEmetteur;

    @ManyToOne
    private UniteOrganisation uoBeneficiaire;

    @ManyToOne
    private Piece pieceEmetteur;

    @ManyToOne
    private Piece pieceBeneficiaire;

    @ManyToOne
    private Devise devisePaiement;

    @ManyToOne
    private Compte compteSrc;

    @ManyToOne
    private Compte compteDest;

    @Lob
    @Column(name = "bordereau")
    private byte[] bordereau;

    @Column(name = "bordereau_content_type")
    private String bordereauContentType;

    @Column(name = "commentaire")
    private String commentaire;

    @Column(name = "donneur_ordre")
    private String donneurOrdre;

    @Column(name = "date_traitement")
    private ZonedDateTime dateTraitement;

    @ManyToOne
    private User user;

    @OneToOne
    @JoinColumn(unique = true)
    private Operation operationTraite;
    @Column(name = "compte_externe_src", columnDefinition = "TEXT")
    private String compteExterneSrc;
    @Column(name = "compte_externe_dest", columnDefinition = "TEXT")
    private String compteExterneDest;
    @Column(name = "operation_externe")
    private String operationExterne;

    public Operation() {
    }

    public Operation(Operation other) {
        this.montantOp = other.montantOp;
        this.montantOpEnDevise = other.montantOpEnDevise;
        this.montantFrais = other.montantFrais;
        this.montantFraisClient = other.montantFraisClient;
        this.montantTaxe = other.montantTaxe;
        this.montantFraisEm = other.montantFraisEm;
        this.montantFraisAcq = other.montantFraisAcq;
        this.dateOperation = other.dateOperation;
        this.statut = other.statut;
        this.codeRetrait = other.codeRetrait;
        this.secret = other.secret;
        this.codeOperation = other.codeOperation;
        this.dateExpiration = other.dateExpiration;
        this.motif = other.motif;
        this.codeExterne = other.codeExterne;
        this.etape = other.etape;
        this.typeService = other.typeService;
        this.motifOperation = other.motifOperation;
        this.beneficaire = other.beneficaire;
        this.emetteur = other.emetteur;
        this.pointVente = other.pointVente;
        this.pays = other.pays;
        this.canal = other.canal;
        this.uoAcquereur = other.uoAcquereur;
        this.uoEmetteur = other.uoEmetteur;
        this.uoBeneficiaire = other.uoBeneficiaire;
        this.devisePaiement = other.devisePaiement;
        this.compteSrc = other.compteSrc;
        this.compteDest = other.compteDest;
        this.bordereau = other.bordereau;
        this.bordereauContentType = other.bordereauContentType;
        this.commentaire = other.commentaire;
        this.dateTraitement = other.dateTraitement;
        this.user = other.user;
        this.description = other.description;
        this.duree = other.duree;
        this.codeSecret = other.codeSecret;
        this.narration = other.narration;
    }

    public Devise getDevisePaiement() {
        return devisePaiement;
    }

    public void setDevisePaiement(Devise devisePaiement) {
        this.devisePaiement = devisePaiement;
    }

    public Operation devisePaiement(Devise devisePaiement) {
        this.devisePaiement = devisePaiement;
        return this;
    }

    public BigDecimal getMontantOpEnDevise() {
        return montantOpEnDevise;
    }

    public void setMontantOpEnDevise(BigDecimal montantOpEnDevise) {
        this.montantOpEnDevise = montantOpEnDevise;
    }

    public MotifOperation getMotifOperation() {
        return motifOperation;
    }

    public void setMotifOperation(MotifOperation motifOperation) {
        this.motifOperation = motifOperation;
    }

    public Canal getCanal() {
        return canal;
    }

    public Operation canal(Canal canal) {
        this.canal = canal;
        return this;
    }

    public BigDecimal getMontantTaxe() {
        return montantTaxe;
    }

    public void setMontantTaxe(BigDecimal montantTaxe) {
        this.montantTaxe = montantTaxe;
    }

    public Operation montantOpEnDevise(BigDecimal montantOpEnDevise) {
        this.montantOpEnDevise = montantOpEnDevise;
        return this;
    }

    public Operation montantTaxe(BigDecimal montantTaxe) {
        this.montantTaxe = montantTaxe;
        return this;
    }

    public Operation duree(int duree) {
        this.duree = duree;
        return this;
    }

    public void setCanal(Canal canal) {
        this.canal = canal;
    }

    public Piece getPieceEmetteur() {
        return pieceEmetteur;
    }

    public void setPieceEmetteur(Piece pieceEmetteur) {
        this.pieceEmetteur = pieceEmetteur;
    }

    public Piece getPieceBeneficiaire() {
        return pieceBeneficiaire;
    }

    public void setPieceBeneficiaire(Piece pieceBeneficiaire) {
        this.pieceBeneficiaire = pieceBeneficiaire;
    }

    public UniteOrganisation getUoAcquereur() {
        return uoAcquereur;
    }

    public Operation uoAcquereur(UniteOrganisation uniteOrganisation) {
        this.uoAcquereur = uniteOrganisation;
        return this;
    }

    public Integer getTagTransitId() {
        return tagTransitId;
    }

    public void setTagTransitId(Integer tagTransitId) {
        this.tagTransitId = tagTransitId;
    }

    public void setUoAcquereur(UniteOrganisation uniteOrganisation) {
        this.uoAcquereur = uniteOrganisation;
    }

    public UniteOrganisation getUoBeneficiaire() {
        return uoBeneficiaire;
    }

    public void setUoBeneficiaire(UniteOrganisation uoBeneficiaire) {
        this.uoBeneficiaire = uoBeneficiaire;
    }

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getMontantOp() {
        return montantOp;
    }

    public Operation montantOp(BigDecimal montantOp) {
        this.montantOp = montantOp;
        return this;
    }

    public void setMontantOp(BigDecimal montantOp) {
        this.montantOp = montantOp;
    }

    public BigDecimal getMontantFrais() {
        return montantFrais;
    }

    public Operation montantFrais(BigDecimal montantFrais) {
        this.montantFrais = montantFrais;
        return this;
    }

    public Operation montantFraisClient(BigDecimal montantFraisClient) {
        this.montantFraisClient = montantFraisClient;
        return this;
    }

    public void setMontantFrais(BigDecimal montantFrais) {
        this.montantFrais = montantFrais;
    }

    public BigDecimal getMontantFraisEm() {
        return montantFraisEm;
    }

    public Operation montantFraisEm(BigDecimal montantFraisEm) {
        this.montantFraisEm = montantFraisEm;
        return this;
    }

    public void setMontantFraisEm(BigDecimal montantFraisEm) {
        this.montantFraisEm = montantFraisEm;
    }

    public BigDecimal getMontantFraisAcq() {
        return montantFraisAcq;
    }

    public Operation montantFraisAcq(BigDecimal montantFraisAcq) {
        this.montantFraisAcq = montantFraisAcq;
        return this;
    }

    public void setMontantFraisAcq(BigDecimal montantFraisAcq) {
        this.montantFraisAcq = montantFraisAcq;
    }

    public ZonedDateTime getDateOperation() {
        return dateOperation;
    }

    public Operation dateOperation(ZonedDateTime dateOperation) {
        this.dateOperation = dateOperation;
        return this;
    }

    public void setDateOperation(ZonedDateTime dateOperation) {
        this.dateOperation = dateOperation;
    }

    public StatutOperation getStatut() {
        return statut;
    }

    public Operation statut(StatutOperation statut) {
        this.statut = statut;
        return this;
    }

    public Operation description(String description) {
        this.description = description;
        return this;
    }

    public void setStatut(StatutOperation statut) {
        this.statut = statut;
    }

    public String getCodeRetrait() {
        return codeRetrait;
    }

    public Operation codeRetrait(String codeRetrait) {
        this.codeRetrait = codeRetrait;
        return this;
    }

    public void setCodeRetrait(String codeRetrait) {
        this.codeRetrait = codeRetrait;
    }

    public String getSecret() {
        return secret;
    }

    public Operation secret(String secret) {
        this.secret = secret;
        return this;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getCodeOperation() {
        return codeOperation;
    }

    public Operation codeOperation(String codeOperation) {
        this.codeOperation = codeOperation;
        return this;
    }

    public void setCodeOperation(String codeOperation) {
        this.codeOperation = codeOperation;
    }

    public LocalDate getDateExpiration() {
        return dateExpiration;
    }

    public Operation dateExpiration(LocalDate dateExpiration) {
        this.dateExpiration = dateExpiration;
        return this;
    }

    public void setDateExpiration(LocalDate dateExpiration) {
        this.dateExpiration = dateExpiration;
    }

    public String getMotif() {
        return motif;
    }

    public Operation motif(String motif) {
        this.motif = motif;
        return this;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }

    public String getCodeExterne() {
        return codeExterne;
    }

    public Operation codeExterne(String codeExterne) {
        this.codeExterne = codeExterne;
        return this;
    }

    public void setCodeExterne(String codeExterne) {
        this.codeExterne = codeExterne;
    }

    public Compte getCompteSrc() {
        return compteSrc;
    }

    public Operation compteSrc(Compte compte) {
        this.compteSrc = compte;
        return this;
    }

    public void setCompteSrc(Compte compte) {
        this.compteSrc = compte;
    }

    public Compte getCompteDest() {
        return compteDest;
    }

    public Operation compteDest(Compte compte) {
        this.compteDest = compte;
        return this;
    }

    public void setCompteDest(Compte compte) {
        this.compteDest = compte;
    }

    public TypeService getTypeService() {
        return typeService;
    }

    public Operation typeService(TypeService typeService) {
        this.typeService = typeService;
        return this;
    }

    public void setTypeService(TypeService typeService) {
        this.typeService = typeService;
    }

    public Beneficiaire getBeneficaire() {
        return beneficaire;
    }

    public Operation beneficaire(Beneficiaire beneficiaire) {
        this.beneficaire = beneficiaire;
        return this;
    }

    public void setBeneficaire(Beneficiaire beneficiaire) {
        this.beneficaire = beneficiaire;
    }

    public Emetteur getEmetteur() {
        return emetteur;
    }

    public Operation emetteur(Emetteur emetteur) {
        this.emetteur = emetteur;
        return this;
    }

    public void setEmetteur(Emetteur emetteur) {
        this.emetteur = emetteur;
    }

    public PointVente getPointVente() {
        return pointVente;
    }

    public Operation pointVente(PointVente pointVente) {
        this.pointVente = pointVente;
        return this;
    }

    public void setPointVente(PointVente pointVente) {
        this.pointVente = pointVente;
    }

    public Pays getPays() {
        return pays;
    }

    public Operation pays(Pays pays) {
        this.pays = pays;
        return this;
    }

    public void setPays(Pays pays) {
        this.pays = pays;
    }

    public byte[] getBordereau() {
        return bordereau;
    }

    public Operation bordereau(byte[] bordereau) {
        this.bordereau = bordereau;
        return this;
    }

    public void setBordereau(byte[] bordereau) {
        this.bordereau = bordereau;
    }

    public String getBordereauContentType() {
        return bordereauContentType;
    }

    public Operation bordereauContentType(String bordereauContentType) {
        this.bordereauContentType = bordereauContentType;
        return this;
    }

    public void setBordereauContentType(String bordereauContentType) {
        this.bordereauContentType = bordereauContentType;
    }

    public Etape getEtape() {
        return etape;
    }

    public Operation etape(Etape etape) {
        this.etape = etape;
        return this;
    }

    public void setEtape(Etape etape) {
        this.etape = etape;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public Operation commentaire(String commentaire) {
        this.commentaire = commentaire;
        return this;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public ZonedDateTime getDateTraitement() {
        return dateTraitement;
    }

    public Operation dateTraitement(ZonedDateTime dateTraitement) {
        this.dateTraitement = dateTraitement;
        return this;
    }

    public void setDateTraitement(ZonedDateTime dateTraitement) {
        this.dateTraitement = dateTraitement;
    }

    public Operation getOperationTraite() {
        return operationTraite;
    }

    public Operation operationTraite(Operation operation) {
        this.operationTraite = operation;
        return this;
    }
public Operation montantAvoirClient(BigDecimal montantAvoirClient) {
        this.montantAvoirClient = montantAvoirClient;
        return this;
    }
    public void setOperationTraite(Operation operation) {
        this.operationTraite = operation;
    }

    public UniteOrganisation getUoEmetteur() {
        return uoEmetteur;
    }

    public Operation uoEmetteur(UniteOrganisation uoEmetteur) {
        this.uoEmetteur = uoEmetteur;
        return this;
    }

    public Operation uoBeneficiaire(UniteOrganisation uoBeneficiaire) {
        this.uoBeneficiaire = uoBeneficiaire;
        return this;
    }

    public void setUoEmetteur(UniteOrganisation uoEmetteur) {
        this.uoEmetteur = uoEmetteur;
    }

    public String getCodeSecret() {
        return codeSecret;
    }

    public void setCodeSecret(String codeSecret) {
        this.codeSecret = codeSecret;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Operation user(User user) {
        this.user = user;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        String desc = "";
        if (description == null) {
            if (this.typeService != null) {
                desc = this.typeService.getLibelle();
            }
            this.description = desc;
        } else {
            this.description = description;
        }

    }

    public String getExtraData() {
        return extraData;
    }

    public void setExtraData(String extraData) {
        this.extraData = extraData;
    }

    public int getDuree() {
        return duree;
    }

    public void setDuree(int duree) {
        this.duree = duree;
    }

    public String getNarration() {
        return narration;
    }

    public void setNarration(String narration) {
        this.narration = narration;
    }

    public BigDecimal getMontantFraisClient() {
        return montantFraisClient;
    }

    public void setMontantFraisClient(BigDecimal montantFraisClient) {
        this.montantFraisClient = montantFraisClient;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getCompteExterneSrc() {
        return compteExterneSrc;
    }

    public void setCompteExterneSrc(String compteExterneSrc) {
        this.compteExterneSrc = compteExterneSrc;
    }

    public String getCompteExterneDest() {
        return compteExterneDest;
    }

    public void setCompteExterneDest(String compteExterneDest) {
        this.compteExterneDest = compteExterneDest;
    }

    public String getOperationExterne() {
        return operationExterne;
    }

    public void setOperationExterne(String operationExterne) {
        this.operationExterne = operationExterne;
    }

    public String getDonneurOrdre() {
        return donneurOrdre;
    }

    public void setDonneurOrdre(String donneurOrdre) {
        this.donneurOrdre = donneurOrdre;
    }

    public BigDecimal getMontantAvoirClient() {
        return montantAvoirClient;
    }

    public void setMontantAvoirClient(BigDecimal montantAvoirClient) {
        this.montantAvoirClient = montantAvoirClient;
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
        Operation operation = (Operation) o;
        if (operation.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), operation.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "Operation{"
                + "id=" + getId()
                + ", montantOp=" + getMontantOp()
                + ", montantFrais=" + getMontantFrais()
                + ", montantFraisEm=" + getMontantFraisEm()
                + ", montantFraisAcq=" + getMontantFraisAcq()
                + ", dateOperation='" + getDateOperation() + "'"
                + ", statut='" + getStatut() + "'"
                + ", secret='" + getSecret() + "'"
                + ", codeOperation='" + getCodeOperation() + "'"
                + ", dateExpiration='" + getDateExpiration() + "'"
                + ", motif='" + getMotif() + "'"
                + ", codeExterne='" + getCodeExterne() + "'"
                + "}";
    }
}
