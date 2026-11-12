package com.m2i.pti.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.m2i.pti.service.dto.TransactionComptePtiDTO;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Objects;

import com.m2i.pti.domain.enumeration.TypeTransactionCompte;

import com.m2i.pti.domain.enumeration.EtatValidite;
import com.m2i.pti.service.dto.TransactionComptePtiOryx;
import java.math.RoundingMode;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

/**
 * A TransactionComptePti.
 */
@SqlResultSetMappings(value = {
    @SqlResultSetMapping(name = "Mapping.TransactionComptePtiDTO",
            classes = @ConstructorResult(targetClass = TransactionComptePtiDTO.class,
                    columns = {
                        @ColumnResult(name = "id", type = Long.class)
                        ,
                                @ColumnResult(name = "jhiType", type = String.class)
                        ,
                                @ColumnResult(name = "etat", type = String.class)
                        ,
                                @ColumnResult(name = "compteAutreId", type = Long.class)
                        ,
                                @ColumnResult(name = "soldeCourant", type = BigDecimal.class)
                        ,
                                @ColumnResult(name = "soldeApres", type = BigDecimal.class)
                        ,
                                @ColumnResult(name = "montant", type = BigDecimal.class)
                        ,
                                @ColumnResult(name = "operationId", type = Long.class)
                        ,
                                @ColumnResult(name = "createdBy", type = String.class)

                    }))
    ,
@SqlResultSetMapping(name = "Mapping.TransactionCompteNombre", columns = {
        @ColumnResult(name = "nombre")})
    ,

        @SqlResultSetMapping(name = "Mapping.TransactionComptePtiAllDTO",
            classes = @ConstructorResult(targetClass = TransactionComptePtiDTO.class,
                    columns = {
                        @ColumnResult(name = "id", type = Long.class)
                        ,
                                @ColumnResult(name = "type", type = String.class)
                        ,
                                @ColumnResult(name = "etat", type = String.class)
                        ,
                                @ColumnResult(name = "date", type = ZonedDateTime.class)
                        ,
                                @ColumnResult(name = "montant", type = BigDecimal.class)
                        ,
                                @ColumnResult(name = "soldeCourant", type = BigDecimal.class)
                        ,
                                @ColumnResult(name = "soldeApres", type = BigDecimal.class)
                        ,
                                @ColumnResult(name = "operationId", type = Long.class)
                        ,
                                @ColumnResult(name = "operationStatut", type = String.class)
                        ,
                                @ColumnResult(name = "codeOperation", type = String.class)
                        ,
                                @ColumnResult(name = "operationMotif", type = String.class)
                        ,
                                @ColumnResult(name = "compteAutreId", type = Long.class)
                        ,
                                @ColumnResult(name = "createdBy", type = String.class)
                        ,
                                @ColumnResult(name = "typeService", type = String.class)
                        ,
                                @ColumnResult(name = "operationCompteSrc", type = String.class)
                        ,
                                @ColumnResult(name = "codeExterne", type = String.class)}))
    ,
        
        @SqlResultSetMapping(name = "Mapping.TransactionComptePtiOryx",
                classes = @ConstructorResult(targetClass = TransactionComptePtiOryx.class,
                        columns = {
                                @ColumnResult(name = "id", type = Long.class)
                                ,
                                @ColumnResult(name = "type", type = String.class)
                                ,
                                @ColumnResult(name = "etat", type = String.class)
                                ,
                                @ColumnResult(name = "date_operation", type = ZonedDateTime.class)
                                ,
                                @ColumnResult(name = "montant", type = BigDecimal.class)
                                ,
                                @ColumnResult(name = "soldeCourant", type = BigDecimal.class)
                                ,
                                @ColumnResult(name = "soldeApres", type = BigDecimal.class)
                                ,
                                @ColumnResult(name = "operationId", type = Long.class)
                                ,
                                @ColumnResult(name = "operationStatut", type = String.class)
                                ,
                                @ColumnResult(name = "codeOperation", type = String.class)
                                ,
                                @ColumnResult(name = "operationMotif", type = String.class)
                                ,
                                @ColumnResult(name = "compteAutreId", type = Long.class)
                                ,
                                @ColumnResult(name = "createdBy", type = String.class)
                                ,
                                @ColumnResult(name = "typeService", type = String.class)
                                ,
                                @ColumnResult(name = "operationCompteSrc", type = String.class)
                                ,
                                @ColumnResult(name = "codeExterne", type = String.class)
                                ,
                                @ColumnResult(name = "soldeApresDernierJour", type = String.class)
                                ,
                                @ColumnResult(name = "dateDernierJour", type = String.class)
 
                })),

        @SqlResultSetMapping(name = "Mapping.TransactionComptePtiAllDTO2",
            classes = @ConstructorResult(targetClass = TransactionComptePtiDTO.class,
                    columns = {
                        @ColumnResult(name = "id", type = Long.class)
                        ,
                                @ColumnResult(name = "type", type = String.class)
                        ,
                                @ColumnResult(name = "etat", type = String.class)
                        ,
                                @ColumnResult(name = "date", type = ZonedDateTime.class)
                        ,
                                @ColumnResult(name = "montant", type = BigDecimal.class)
                        ,
                                @ColumnResult(name = "soldeCourant", type = BigDecimal.class)
                        ,
                                @ColumnResult(name = "soldeApres", type = BigDecimal.class)
                        ,
                                @ColumnResult(name = "operationId", type = Long.class)
                        ,
                                @ColumnResult(name = "operationStatut", type = String.class)
                        ,
                                @ColumnResult(name = "codeOperation", type = String.class)
                        ,
                                @ColumnResult(name = "operationMotif", type = String.class)
                        ,
                                @ColumnResult(name = "compteAutreId", type = Long.class)
                        ,
                                @ColumnResult(name = "createdBy", type = String.class)
                        ,
                                @ColumnResult(name = "typeService", type = String.class)
                        ,
                                @ColumnResult(name = "operationCompteSrc", type = String.class)
                        ,
                                @ColumnResult(name = "codeExterne", type = String.class)
                        ,
                                @ColumnResult(name = "description", type = String.class)}))})







@NamedNativeQueries({
    @NamedNativeQuery(name = "TransactionComptePti.findByCompteAutreAndDateBetweenOrderByIdDescOptimise",
            query = "select tc.id as id, tc.jhi_type as type, tc.etat as etat, tc.jhi_date as date, tc.montant as montant, tc.solde_courant as soldeCourant,tc.solde_apres as soldeApres,"
            + " tc.operation_id as operationId, op.statut as operationStatut, op.code_operation as codeOperation, op.motif as operationMotif,"
            + " tc.compte_autre_id as compteAutreId, tc.created_by as createdBy,ts.libelle as typeService,"
            + "cpt.valeur_compte as operationCompteSrc, op.code_externe as codeExterne"
            + " FROM Transaction_compte_pti tc"
            + " LEFT JOIN operation op on tc.operation_id=op.id"
            + " LEFT JOIN compte cpt on op.compte_src_id=cpt.id"
            + " LEFT JOIN type_service ts on op.type_service_id=ts.id"
            + " WHERE ("
            + "(tc.compte_autre_id= CAST (CAST(:compteAutreId AS TEXT) AS BIGINT))"
            + "and (:jhiType is null or tc.jhi_type=CAST(:jhiType AS TEXT ) )"
            + "and (CAST(CAST(:dateDebut as text) as timestamp) is null or tc.jhi_date BETWEEN CAST(CAST(:dateDebut AS text) AS timestamp ) and CAST(CAST(:dateFin AS text) AS timestamp ))) order by tc.jhi_date desc  limit :taille offset :page",
            resultSetMapping = "Mapping.TransactionComptePtiAllDTO", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})
    ,

    
@NamedNativeQuery(
            name = "TransactionComptePti.cpte",
            query = "query = select COUNT(*) as nombre from Transaction_compte_pti t, operation op where t.operation_id=op.id and op.code_externe=:codeExterne and t.compte_autre_id =:compteAutreId ",
            resultSetMapping = "Mapping.TransactionCompteNombre", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})
    ,


        @NamedNativeQuery(name = "TransactionComptePti.findByCompteAutreIdOptimise",
            query = "select tc.id as id, tc.jhi_type as type, tc.etat as etat, tc.jhi_date as date, tc.montant as montant, tc.solde_courant as soldeCourant,tc.solde_apres as soldeApres,"
            + " tc.operation_id as operationId, op.statut as operationStatut, op.code_operation as codeOperation, op.motif as operationMotif,"
            + " tc.compte_autre_id as compteAutreId, tc.created_by as createdBy,ts.libelle as typeService,"
            + "cpt.valeur_compte as operationCompteSrc, op.code_externe as codeExterne"
            + " FROM Transaction_compte_pti tc"
            + " LEFT JOIN operation op on tc.operation_id=op.id"
            + " LEFT JOIN compte cpt on op.compte_src_id=cpt.id"
            + " LEFT JOIN type_service ts on op.type_service_id=ts.id"
            + " WHERE tc.compte_autre_id= CAST (CAST (:compteAutreId AS TEXT) AS BIGINT) order by tc.id desc",
            resultSetMapping = "Mapping.TransactionComptePtiAllDTO", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})
    ,

        @NamedNativeQuery(name = "TransactionComptePti.findByOperationId",
            query = "select id ,jhi_type as jhiType, etat, compte_autre_id as compteAutreId, solde_courant as soldeCourant,"
            + "solde_apres as soldeApres, montant,operation_id as operationId, created_by as createdBy"
            + " from Transaction_compte_pti where operation_id= CAST (CAST(:operationId AS TEXT) AS BIGINT) limit :taille offset :page",
            resultSetMapping = "Mapping.TransactionComptePtiDTO", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})
    ,

        @NamedNativeQuery(name = "TransactionComptePti.findByCompteAutreAndDateBetweenAndOperationPointVenteIdOrderByIdDescOptimise",
            query = "select tc.id as id, tc.jhi_type as type, tc.etat as etat, tc.jhi_date as date, tc.montant as montant, tc.solde_courant as soldeCourant,tc.solde_apres as soldeApres,"
            + " tc.operation_id as operationId, op.statut as operationStatut, op.code_operation as codeOperation, op.motif as operationMotif,"
            + " tc.compte_autre_id as compteAutreId, tc.created_by as createdBy,ts.libelle as typeService,"
            + "cpt.valeur_compte as operationCompteSrc, op.code_externe as codeExterne"
            + " FROM Transaction_compte_pti tc"
            + " LEFT JOIN operation op on tc.operation_id=op.id"
            + " LEFT JOIN compte cpt on op.compte_src_id=cpt.id"
            + " LEFT JOIN type_service ts on op.type_service_id=ts.id"
            + " WHERE ("
            + "(tc.compte_autre_id= CAST (CAST(:compteAutreId AS TEXT) AS BIGINT))"
            + "and (op.point_vente_id= CAST (CAST(:pointventeId AS TEXT) AS BIGINT))"
            + "and (CAST(CAST(:dateDebut as text) as timestamp) is null or tc.jhi_date BETWEEN CAST(CAST(:dateDebut AS text) AS timestamp ) and CAST(CAST(:dateFin AS text) AS timestamp ))) order by tc.jhi_date desc",
            resultSetMapping = "Mapping.TransactionComptePtiAllDTO", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})
    ,

        
          @NamedNativeQuery(name = "TransactionComptePti.findByCompteAutreAndDateBetweenAndOperationPointVenteIdLimitOrderByIdDescOptimise",
            query = "select tc.id as id, tc.jhi_type as type, tc.etat as etat, tc.jhi_date as date, tc.montant as montant, tc.solde_courant as soldeCourant,tc.solde_apres as soldeApres,"
            + " tc.operation_id as operationId, op.statut as operationStatut, op.code_externe as codeOperation, op.motif as operationMotif,"
            + " tc.compte_autre_id as compteAutreId, tc.created_by as createdBy,ts.libelle as typeService,"
            + "cpt.valeur_compte as operationCompteSrc, op.code_externe as codeExterne, op2.description as description"
            + " FROM Transaction_compte_pti tc"
            + " LEFT JOIN operation op on tc.operation_id=op.id"
            + "	 LEFT JOIN operation op2 on op2.code_operation=op.code_externe "
            + " LEFT JOIN compte cpt on op.compte_src_id=cpt.id"
            + " LEFT JOIN type_service ts on op2.type_service_id=ts.id"
            + " WHERE ("
            + "(tc.compte_autre_id= CAST (CAST(:compteAutreId AS TEXT) AS BIGINT))"
            + "and (op2.point_vente_id= CAST (CAST(:pointventeId AS TEXT) AS BIGINT))"
            + "and (CAST(CAST(:dateDebut as text) as timestamp) is null or tc.jhi_date BETWEEN CAST(CAST(:dateDebut AS text) AS timestamp ) and CAST(CAST(:dateFin AS text) AS timestamp ))) order by tc.jhi_date desc limit :taille ",
            resultSetMapping = "Mapping.TransactionComptePtiAllDTO", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})
    ,
        
        @NamedNativeQuery(name = "TransactionComptePti.findTop200ByCompteAutreAndDateBetweenOrderByIdDescOptimiser",
            query = "select tc.id as id, tc.jhi_type as type, tc.etat as etat, tc.jhi_date as date, tc.montant as montant, tc.solde_courant as soldeCourant,tc.solde_apres as soldeApres,"
            + " tc.operation_id as operationId, op.statut as operationStatut, op.code_operation as codeOperation, op.motif as operationMotif,"
            + " tc.compte_autre_id as compteAutreId, tc.created_by as createdBy,ts.libelle as typeService,"
            + "cpt.valeur_compte as operationCompteSrc, op.code_externe as codeExterne,op2.description as description"
            + " FROM Transaction_compte_pti tc"
            + " LEFT JOIN operation op on tc.operation_id=op.id"
            + " LEFT JOIN compte cpt on op.compte_src_id=cpt.id"
            + " LEFT JOIN type_service ts on op.type_service_id=ts.id"
            + " WHERE tc.compte_autre_id= CAST (CAST (:compteAutreId AS TEXT) AS BIGINT)"
            + "and (CAST(CAST(:dateDebut as text) as timestamp) is null or tc.jhi_date BETWEEN CAST(CAST(:dateDebut AS text) AS timestamp ) and CAST(CAST(:dateFin AS text) AS timestamp ))  order by tc.jhi_date desc limit 20",
            resultSetMapping = "Mapping.TransactionComptePtiAllDTO2", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})
    ,
 @NamedNativeQuery(name = "TransactionComptePti.findLastThreeByCompteAutreIdDescOptimise",
            query = "select tc.id as id, tc.jhi_type as type, tc.etat as etat, tc.jhi_date as date, tc.montant as montant, tc.solde_courant as soldeCourant,tc.solde_apres as soldeApres,"
            + " tc.operation_id as operationId, "
            + " op.statut as operationStatut, op.code_operation as codeOperation, op.motif as operationMotif,"
            + " tc.compte_autre_id as compteAutreId, tc.created_by as createdBy,ts.libelle as typeService,"
            + "cpt.valeur_compte as operationCompteSrc, op.code_externe as codeExterne"
            + " FROM Transaction_compte_pti tc"
            + " LEFT JOIN operation op on tc.operation_id=op.id"
            + " LEFT JOIN compte cpt on op.compte_src_id=cpt.id"
            + " LEFT JOIN type_service ts on op.type_service_id=ts.id"
            + " WHERE ("
            + "(tc.compte_autre_id= CAST (CAST(:compteAutreId AS TEXT) AS BIGINT))) order by tc.id desc limit 3"
            + " ",
            resultSetMapping = "Mapping.TransactionComptePtiAllDTO", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})
    ,
 
 
 
 @NamedNativeQuery(name = "TransactionComptePti.queryResultAFB120",
                query = "SELECT" +
                        "    tc.id as id, \n" +
                        "    tc.jhi_type as type, \n" +
                        "    tc.etat AS etat, \n" +
                        "    tc.jhi_date AS date_operation, \n" +
                        "    tc.montant AS montant, \n" +
                        "    tc.solde_courant AS soldeCourant, \n" +
                        "    tc.solde_courant AS montant_solde, \n" +
                        "    tc.solde_apres AS soldeApres, \n" +
                        "    tc.operation_id AS operationId, \n" +
                        "    op.statut AS operationStatut, \n" +
                        "    op.code_operation AS codeOperation, \n" +
                        "    op.motif AS operationMotif, \n" +
                        "    tc.compte_autre_id AS compteAutreId, \n" +
                        "    tc.created_by AS createdBy, \n" +
                        "    ts.libelle AS typeService, \n" +
                        "    cpt.valeur_compte AS operationCompteSrc, \n" +
                        "    op.code_externe AS codeExterne, \n" +
                        "    (\n" +
                        "        SELECT solde_apres \n" +
                        "        FROM Transaction_compte_pti \n" +
                        "        WHERE compte_autre_id = tc.compte_autre_id \n" +
                        "          AND date_operation < :datedebut \n" +
                        "        ORDER BY date_operation DESC \n" +
                        "        LIMIT 1 \n" +
                        "    ) AS soldeApresDernierJour, \n" +
                        "    (\n" +
                        "        SELECT date_operation \n" +
                        "        FROM Transaction_compte_pti \n" +
                        "        WHERE compte_autre_id = tc.compte_autre_id \n" +
                        "          AND date_operation < :datedebut \n" +
                        "        ORDER BY date_operation DESC \n" +
                        "        LIMIT 1 \n" +
                        "    ) AS dateDernierJour \n" +
                        "FROM \n" +
                        "    Transaction_compte_pti tc\n" +
                        "LEFT JOIN \n" +
                        "    operation op ON tc.operation_id = op.id \n" +
                        "LEFT JOIN \n" +
                        "    compte cpt ON op.compte_src_id = cpt.id \n" +
                        "LEFT JOIN \n" +
                        "    type_service ts ON op.type_service_id = ts.id \n" +
                        "WHERE \n" +
                        "    tc.compte_autre_id = (\n" +
                        "        SELECT compte_autre_id \n" +
                        "        FROM compte \n" +
                        "        WHERE valeur_compte = :valeurCompte\n" +
                        "    ) \n" +
                        "    AND op.date_operation BETWEEN :datedebut  AND :datefin  \n" +
                        "GROUP BY \n" +
                        "    tc.id, tc.jhi_type, tc.etat, tc.jhi_date, tc.montant, tc.solde_courant, \n" +
                        "    tc.solde_apres, tc.operation_id, op.statut, op.code_operation, op.motif, \n" +
                        "    tc.compte_autre_id, tc.created_by, ts.libelle, cpt.valeur_compte, \n" +
                        "    op.code_externe, op.date_operation\n" +
                        "ORDER BY \n" +
                        "    op.date_operation DESC ;",
                resultSetMapping = "Mapping.TransactionComptePtiOryx", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})
        ,
 
 
 @NamedNativeQuery(name = "TransactionComptePti.queryResultAFB120Null",

                query = "SELECT" +

                        "    tc.id as id, \n" +

                        "    tc.jhi_type as type, \n" +

                        "    tc.etat AS etat, \n" +

                        "    tc.jhi_date AS date_operation, \n" +

                        "    tc.montant AS montant, \n" +

                        "    tc.solde_courant AS soldeCourant, \n" +

                        "    tc.solde_courant AS montant_solde, \n" +

                        "    tc.solde_apres AS soldeApres, \n" +

                        "    tc.operation_id AS operationId, \n" +

                        "    op.statut AS operationStatut, \n" +

                        "    op.code_operation AS codeOperation, \n" +

                        "    op.motif AS operationMotif, \n" +

                        "    tc.compte_autre_id AS compteAutreId, \n" +

                        "    tc.created_by AS createdBy, \n" +

                        "    ts.libelle AS typeService, \n" +

                        "    cpt.valeur_compte AS operationCompteSrc, \n" +

                        "    op.code_externe AS codeExterne, \n" +

                        "    (\n" +

                        "        SELECT solde_apres \n" +

                        "        FROM Transaction_compte_pti \n" +

                        "        WHERE compte_autre_id = tc.compte_autre_id \n" +

                        "          AND date_operation < :datedebut \n" +

                        "        ORDER BY date_operation DESC \n" +

                        "        LIMIT 1 \n" +

                        "    ) AS soldeApresDernierJour, \n" +

                        "    (\n" +

                        "        SELECT date_operation \n" +

                        "        FROM Transaction_compte_pti \n" +

                        "        WHERE compte_autre_id = tc.compte_autre_id \n" +

                        "          AND date_operation < :datedebut \n" +

                        "        ORDER BY date_operation DESC \n" +

                        "        LIMIT 1 \n" +

                        "    ) AS dateDernierJour \n" +

                        "FROM \n" +

                        "    Transaction_compte_pti tc\n" +

                        "LEFT JOIN \n" +

                        "    operation op ON tc.operation_id = op.id \n" +

                        "LEFT JOIN \n" +

                        "    compte cpt ON op.compte_src_id = cpt.id \n" +

                        "LEFT JOIN \n" +

                        "    type_service ts ON op.type_service_id = ts.id \n" +

                        "WHERE \n" +

                        "    tc.compte_autre_id = (\n" +

                        "        SELECT compte_autre_id \n" +

                        "        FROM compte \n" +

                        "        WHERE valeur_compte = :valeurCompte\n" +

                        "    ) \n" +

                        "    AND op.date_operation < :datedebut  \n" +

                        "GROUP BY \n" +

                        "    tc.id, tc.jhi_type, tc.etat, tc.jhi_date, tc.montant, tc.solde_courant, \n" +

                        "    tc.solde_apres, tc.operation_id, op.statut, op.code_operation, op.motif, \n" +

                        "    tc.compte_autre_id, tc.created_by, ts.libelle, cpt.valeur_compte, \n" +

                        "    op.code_externe, op.date_operation\n" +

                        "ORDER BY \n" +

                        "    op.date_operation DESC LIMIT 1 ;",

                resultSetMapping = "Mapping.TransactionComptePtiOryx", hints = {

                @QueryHint(name = "org.hibernate.cacheable", value = "false")})

        ,
 
 
 
 
        @NamedNativeQuery(name = "TransactionComptePti.findTransByCompteAutreDateOptimiser",
            query = "select tc.id as id, tc.jhi_type as type, tc.etat as etat, tc.jhi_date as date, tc.montant as montant, tc.solde_courant as soldeCourant,tc.solde_apres as soldeApres,"
            + " tc.operation_id as operationId, op.statut as operationStatut, op.code_operation as codeOperation, op.motif as operationMotif,"
            + " tc.compte_autre_id as compteAutreId, tc.created_by as createdBy,ts.libelle as typeService,"
            + "cpt.valeur_compte as operationCompteSrc, op.code_externe as codeExterne,op.description as description"
            + " FROM Transaction_compte_pti tc"
            + " LEFT JOIN operation op on tc.operation_id=op.id"
            + " LEFT JOIN compte cpt on op.compte_src_id=cpt.id"
            + " LEFT JOIN type_service ts on op.type_service_id=ts.id"
            + " WHERE tc.compte_autre_id= CAST (CAST (:compteAutreId AS TEXT) AS BIGINT)"
            + "and (CAST(CAST(:dateDebut as text) as timestamp) is null or tc.jhi_date BETWEEN CAST(CAST(:dateDebut AS text) AS timestamp ) and CAST(CAST(:dateFin AS text) AS timestamp ))  order by tc.jhi_date desc ",
            resultSetMapping = "Mapping.TransactionComptePtiAllDTO2", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")}),})

@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "transaction_compte_pti")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class TransactionComptePti extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    @Transient
    @JsonIgnore
    private final Object lock = new Object();
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "jhi_type", nullable = false)
    private TypeTransactionCompte type;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "etat", nullable = false)
    private EtatValidite etat;

    @NotNull
    @Column(name = "jhi_date", nullable = false)
    private ZonedDateTime date;

    @NotNull
    @Column(name = "montant", nullable = false)
    private BigDecimal montant;

    @Column(name = "solde_courant")
    private BigDecimal soldeCourant;

    @Column(name = "solde_apres")
    private BigDecimal soldeApres;

    @OneToOne
    @JoinColumn(unique = false)
    private Operation operation;

    @ManyToOne
    private CompteAutre compteAutre;
    @ManyToOne
    private CompteAutre compteAutreOp;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TypeTransactionCompte getType() {
        return type;
    }

    public TransactionComptePti type(TypeTransactionCompte type) {
        this.type = type;
        return this;
    }

    public void setType(TypeTransactionCompte type) {
        this.type = type;
    }

    public EtatValidite getEtat() {
        return etat;
    }

    public TransactionComptePti etat(EtatValidite etat) {
        this.etat = etat;
        return this;
    }

    public void setEtat(EtatValidite etat) {
        this.etat = etat;
    }

    public ZonedDateTime getDate() {
        return date;
    }

    public TransactionComptePti date(ZonedDateTime date) {
        this.date = date;
        return this;
    }

    public void setDate(ZonedDateTime date) {
        this.date = date;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public TransactionComptePti montant(BigDecimal montant) {
        this.montant = montant;
        return this;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }

    public BigDecimal getSoldeCourant() {
        synchronized (lock) {
            return soldeCourant;
        }

    }

    public TransactionComptePti withdraw(BigDecimal montant) {
        synchronized (lock) {
            this.soldeApres = compteAutre.getSolde();
            this.soldeCourant = compteAutre.getSolde().add(montant.abs().setScale(0, RoundingMode.HALF_UP));

        }

        return this;
    }

    public TransactionComptePti deposit(BigDecimal montant) {
        synchronized (lock) {
            this.soldeApres = compteAutre.getSolde();
            this.soldeCourant = compteAutre.getSolde().subtract(montant.abs().setScale(0, RoundingMode.HALF_UP));

        }

        return this;
    }

    public TransactionComptePti soldeCourant(BigDecimal soldeCourant) {
        synchronized (lock) {
            this.soldeCourant = soldeCourant;
            return this;
        }

    }

    public void setSoldeCourant(BigDecimal soldeCourant) {
        synchronized (lock) {
            this.soldeCourant = soldeCourant;
        }

    }

    public BigDecimal getSoldeApres() {
        synchronized (lock) {
            return soldeApres;
        }

    }

    public TransactionComptePti soldeApres(BigDecimal soldeApres) {
        this.soldeApres = soldeApres;
        return this;
    }

    public void setSoldeApres(BigDecimal soldeApres) {
        synchronized (lock) {
            this.soldeApres = soldeApres;
        }

    }

    public CompteAutre getCompteAutreOp() {
        return compteAutreOp;
    }

    public void setCompteAutreOp(CompteAutre compteAutreOp) {
        this.compteAutreOp = compteAutreOp;
    }

    public Operation getOperation() {
        return operation;
    }

    public TransactionComptePti operation(Operation operation) {
        this.operation = operation;
        return this;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    public CompteAutre getCompteAutre() {
        synchronized (lock) {
            return compteAutre;
        }

    }

    public void setCompteAutre(CompteAutre compteAutre) {
        synchronized (lock) {
            this.compteAutre = compteAutre;
        }

    }

    public TransactionComptePti compteAutre(CompteAutre compteAutre) {
        this.compteAutre = compteAutre;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TransactionComptePti transactionComptePti = (TransactionComptePti) o;
        if (transactionComptePti.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), transactionComptePti.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "TransactionComptePti{"
                + "id=" + getId()
                + ", type='" + getType() + "'"
                + ", etat='" + getEtat() + "'"
                + ", date='" + getDate() + "'"
                + ", montant=" + getMontant()
                + ", soldeCourant=" + getSoldeCourant()
                + ", soldeApres=" + getSoldeApres()
                + "}";
    }
}
