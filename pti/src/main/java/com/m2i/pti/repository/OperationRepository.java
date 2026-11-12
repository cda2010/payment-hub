package com.m2i.pti.repository;

import com.m2i.pti.domain.Compte;
import com.m2i.pti.domain.Operation;
import com.m2i.pti.domain.PointVente;
import com.m2i.pti.domain.TypeService;
import com.m2i.pti.domain.UniteOrganisation;
import com.m2i.pti.domain.enumeration.Etape;
import com.m2i.pti.domain.enumeration.StatutOperation;
import com.m2i.pti.mobile.dto.MOperationNumbLastestDTO;
import com.m2i.pti.service.dto.OperationCustom;
import com.m2i.pti.service.dto.OperationDTO;
import com.m2i.pti.service.dto.OperationOptimiseDTO;
import com.m2i.pti.service.dto.OperationStatutDTO;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Operation entity.
 */
@SuppressWarnings("unused")
@Repository
public interface OperationRepository extends JpaRepository<Operation, Long> {
  Operation findTopByCodeRetraitAndMontantOp(String code, BigDecimal montant);

  Operation findTopByCodeRetrait(String code);

  Page<Operation> findByPointVente(PointVente pointVente, Pageable pageable);

  List<Operation> findByStatut(StatutOperation statutOperation);

  List<Operation> findByTypeService(TypeService typeService);

  Optional<Operation> findByCodeOperation(String codeOperation);

  List<Operation> findByTypeServiceAndUoAcquereurIdInAndPointVenteAndEtape(
    TypeService typeService,
    List<Long> uoIds,
    PointVente pointVente,
    Etape etape
  );

  List<Operation> findByTypeServiceAndUoAcquereur(
    TypeService typeService,
    UniteOrganisation organisation
  );

  @Query(
    "select op from Operation op where  op.typeService.id=?1 and op.compteSrc=?2 and op.statut=?3"
  )
  List<Operation> findByCompteSourceAndEtat(
    Long typeServiceId,
    Compte compteSrc,
    StatutOperation statutOperation
  );

  Boolean existsByCodeExterne(String codeExterne);

  @Query(
    "select op from Operation op where  op.typeService.id=:typeServiceId and op.pointVente.id=:pointVenteId and op.dateOperation between :dateOperation and :dateOperation2 and op.etape=:etape"
  )
  List<Operation> findAllOperationByAndTypseServiceAndPointVente(
    @Param("typeServiceId") Long typeServiceId,
    @Param("pointVenteId") Long pointVenteId,
    @Param("dateOperation") ZonedDateTime dateOperation,
    @Param("dateOperation2") ZonedDateTime dateOperation2,
    @Param("etape") Etape etape
  );

  List<Operation> findByPointVenteIdAndStatut(
    Long pvId,
    StatutOperation statutOperation
  );

  /*
        Filtre en fonction de {
            -canal
            -typeService
            -montantOp
            -statut
        }
     */

    @Query(name = "operation.findByCompteIdSrcAndDest", nativeQuery = true)
    OperationOptimiseDTO findByCompteIdSrcAndDest(@Param("compteId") Long  compteId);


  @Query(
    value = "select op from Operation op where  (op.typeService.id=:typeServiceId or :typeServiceId is null) " +
    "and (op.canal.id=:canalId or :canalId is null) " +
    "and (op.uoAcquereur.id=:uoAcquereurId or :uoAcquereurId is null) " +
    "and (op.pointVente.id in :pointVenteIds or :pointVenteIds is null) " +
    "and (op.montantOp=:montantOp or :montantOp is null) " +
    "and (op.statut in (:statut) or :statut is null) " +
    "and (op.codeOperation=:codeOperation or :codeOperation is null) " +
    "and (op.dateOperation between :dateOperation and  :dateOperation2 or cast(:dateOperation  as timestamp) is null)" +
    " order by dateOperation desc"
  )
  Page<Operation> findOperationByMultiParams(
    @Param("typeServiceId") Long typeServiceId,
    @Param("canalId") Long canalId,
    @Param("uoAcquereurId") Long uoAcquereurId,
    @Param("pointVenteIds") List<Long> pointVenteIds,
    @Param("montantOp") BigDecimal montantOp,
    @Param("statut") List<StatutOperation> statut,
    @Param("dateOperation") ZonedDateTime dateOperation,
    @Param("dateOperation2") ZonedDateTime dateOperation2,
    @Param("codeOperation") String codeOperation,
    Pageable pageable
  );

  @Query(
    value = "select op from Operation op where  (op.typeService.id=:typeServiceId or :typeServiceId is null) and (op.canal.id=:canalId or :canalId is null) and (op.uoAcquereur.id=:uoAcquereurId or :uoAcquereurId is null) and (op.pointVente.id in :pointVenteIds or :pointVenteIds is null) and (op.montantOp=:montantOp or :montantOp is null) and (op.statut=:statut or :statut is null) and (op.dateOperation between :dateOperation and  :dateOperation2 or cast(:dateOperation  as timestamp) is null) order by dateOperation desc"
  )
  List<Operation> findAllOperationByMultiParams(
    @Param("typeServiceId") Long typeServiceId,
    @Param("canalId") Long canalId,
    @Param("uoAcquereurId") Long uoAcquereurId,
    @Param("pointVenteIds") List<Long> pointVenteIds,
    @Param("montantOp") BigDecimal montantOp,
    @Param("statut") StatutOperation statut,
    @Param("dateOperation") ZonedDateTime dateOperation,
    @Param("dateOperation2") ZonedDateTime dateOperation2
  );

  @Query(
    value = "select op from Operation op where  op.statut='ENATTENTE' and op.operationTraite.id is null and (op.typeService.code='APPEL_FOND' and op.uoEmetteur.id in :uoAcquereurIds) and (op.montantOp=:montantOp or :montantOp is null)  and (op.dateOperation between :dateOperation and :dateOperation2 or cast(:dateOperation  as timestamp) is null)"
  )
  Page<Operation> findOperationMadByMultiParams(
    @Param("uoAcquereurIds") List<Long> uoAcquereurIds,
    @Param("montantOp") BigDecimal montantOp,
    @Param("dateOperation") ZonedDateTime dateOperation,
    @Param("dateOperation2") ZonedDateTime dateOperation2,
    Pageable pageable
  );

  @Query(
    value = "select op from Operation op where  op.statut='ENATTENTE' and op.operationTraite.id is null and (op.typeService.code='ACCOUNT_ACCOUNT' and op.uoAcquereur.id in :uoAcquereurIds) and (op.montantOp=:montantOp or :montantOp is null)  and (op.dateOperation between :dateOperation and :dateOperation2 or cast(:dateOperation  as timestamp) is null)",
    countQuery = "select op from Operation op where  op.statut='ENATTENTE' and op.operationTraite.id is null and  (op.typeService.code='ACCOUNT_ACCOUNT' and op.uoAcquereur.id in :uoAcquereurIds)  and (op.montantOp=:montantOp or :montantOp is null)  and (op.dateOperation between :dateOperation and :dateOperation2 or cast(:dateOperation  as timestamp) is null)"
  )
  Page<Operation> findOperationAccountAccountByMultiParams(
    @Param("uoAcquereurIds") List<Long> uoAcquereurIds,
    @Param("montantOp") BigDecimal montantOp,
    @Param("dateOperation") ZonedDateTime dateOperation,
    @Param("dateOperation2") ZonedDateTime dateOperation2,
    Pageable pageable
  );

  @Query(
    value = "select op from Operation op where  (op.typeService.id=:typeServiceId or :typeServiceId is null) and (op.pointVente.id=:pointVenteId or :pointVenteId is null) and (op.statut='ENATTENTE' or op.statut='PAYE') and (op.dateOperation between :dateOperation and :dateOperation2 or cast(:dateOperation  as timestamp) is null) order by dateOperation desc"
  )
  List<Operation> findOperationCTCByMultiParams(
    @Param("typeServiceId") Long typeServiceId,
    @Param("pointVenteId") Long pointVenteId,
    @Param("dateOperation") ZonedDateTime dateOperation,
    @Param("dateOperation2") ZonedDateTime dateOperation2
  );

  @Query(
    value = "SELECT EXTRACT(MONTH FROM DATE date_operation) as dateOperation,sum(op.montant_op) as montantTotal FROM operation op  GROUP BY EXTRACT(MONTH FROM DATE op.date_operation)",
    nativeQuery = true
  )
  List<Object> getSumMontantOperationByMontant();

  @Query(
    value = "SELECT EXTRACT(MONTH FROM DATE date_operation) as dateOperation,sum(op.montant_op) as montantTotal FROM operation op WHERE op.type_service_id=:typeServiceId  GROUP BY EXTRACT(MONTH FROM DATE op.date_operation)",
    nativeQuery = true
  )
  List<Object> getSumMontantOperationByMonthAndTypeService(
    @Param("typeServiceId") Long typeServiceId
  );

  @Query(
    value = "SELECT sum(op.montant_op) FROM operation op WHERE op.type_service_id=:typeServiceId  and EXTRACT(MONTH FROM DATE op.date_operation)=:month and op.point_vente_id=:pointVenteId and op.etape=:etape",
    nativeQuery = true
  )
  List<Object> getSumMontantByMonthAndTypeServiceAndEtape(
    @Param("month") Integer month,
    @Param("typeServiceId") Long typeServiceId,
    @Param("pointVenteId") Long pointVenteId,
    @Param("etape") String etape
  );

  @Query(value = "SELECT op.montant_op FROM operation op ", nativeQuery = true)
  List<OperationCustom> getOperationMonth();

  @Query(
    value = "SELECT sum(op.montant_op) FROM operation op WHERE op.type_service_id=:typeServiceId   and op.point_vente_id = :pointVenteId",
    nativeQuery = true
  )
  List<Object> getBilanMensuelByServiceAndPointVente(
    @Param("typeServiceId") Long typeServiceId,
    @Param("pointVenteId") Long pointVenteId
  );

  @Query(
    value = "SELECT * FROM operation op WHERE op.type_service_id=:typeServiceId  and op.point_vente_id = :pointVenteId ",
    nativeQuery = true
  )
  List<Operation> getOperationMensuelByServiceAndPointVente(
    @Param("typeServiceId") Long typeServiceId,
    @Param("pointVenteId") Long pointVenteId
  );

  Page<Operation> findByPointVenteAndDateExpirationBefore(
    PointVente pointVente,
    LocalDate now,
    Pageable pageable
  );
  Operation findByOperationTraiteId(Long operationId);

  @Query("select o from Operation o where o.id=?1")
  Operation findByOperationId(Long operationId);

  @Query(
    value = "select op from Operation op where   (op.typeService.id=:typeServiceId or :typeServiceId is null) and" +
    " (op.canal.id=:canalId or :canalId is null) " +
    " and (op.uoAcquereur.id=:uoAcquereurId or :uoAcquereurId is null) " +
    " and (op.montantOp=:montantOp or :montantOp is null) " +
    " and (op.statut=:statut or :statut is null or op.statut=:statutAnnuler or :statutAnnuler is null ) " +
    " and (op.codeOperation=:codeOperation or :codeOperation is null) " +
    " and (op.dateOperation between :dateOperation and  :dateOperation2 or cast(:dateOperation  as timestamp) is null)" +
    " and op.beneficaire.id=:beneficiaireId" +
    " order by dateOperation desc"
  )
  List<Operation> findOperationByPersonneBeneficiaire(
    @Param("typeServiceId") Long typeServiceId,
    @Param("canalId") Long canalId,
    @Param("uoAcquereurId") Long uoAcquereurId,
    @Param("montantOp") BigDecimal montantOp,
    @Param("statut") StatutOperation statut,
    @Param("statutAnnuler") StatutOperation statutAnnuler,
    @Param("dateOperation") ZonedDateTime dateOperation,
    @Param("dateOperation2") ZonedDateTime dateOperation2,
    @Param("codeOperation") String codeOperatio,
    @Param("beneficiaireId") Long beneficiaireId
  );

  @Query(
    value = "select op from Operation op where   (op.typeService.id=:typeServiceId or :typeServiceId is null) and" +
    " (op.canal.id=:canalId or :canalId is null) " +
    " and (op.uoAcquereur.id=:uoAcquereurId or :uoAcquereurId is null) " +
    " and (op.montantOp=:montantOp or :montantOp is null) " +
    " and (op.statut=:statut or :statut is null or op.statut=:statutAnnuler or :statutAnnuler is null) " +
    " and (op.codeOperation=:codeOperation or :codeOperation is null) " +
    " and (op.dateOperation between :dateOperation and  :dateOperation2 or cast(:dateOperation  as timestamp) is null)" +
    " and op.emetteur.id=:emetteurId" +
    " order by dateOperation desc"
  )
  List<Operation> findOperationByPersonneEmetteur(
    @Param("typeServiceId") Long typeServiceId,
    @Param("canalId") Long canalId,
    @Param("uoAcquereurId") Long uoAcquereurId,
    @Param("montantOp") BigDecimal montantOp,
    @Param("statut") StatutOperation statut,
    @Param("statutAnnuler") StatutOperation statutAnnuler,
    @Param("dateOperation") ZonedDateTime dateOperation,
    @Param("dateOperation2") ZonedDateTime dateOperation2,
    @Param("codeOperation") String codeOperation,
    @Param("emetteurId") Long emetteurId
  );

  @Query(
    value = "select op from Operation op where   op.typeService.id=:typeServiceId  and op.pointVente.id=:pointVenteId" +
    " and (op.dateOperation between :dateOperation and  :dateOperation2 or cast(:dateOperation  as timestamp) is null)" +
    " order by dateOperation desc"
  )
  List<Operation> findOperationByPointVenteAndPeriodeAndService(
    @Param("typeServiceId") Long typeServiceId,
    @Param("pointVenteId") Long pointVenteId,
    @Param("dateOperation") ZonedDateTime dateOperation,
    @Param("dateOperation2") ZonedDateTime dateOperation2
  );

  @Query(
    value = "select op FROM Operation op WHERE op.typeService.id=:typeServiceId  and op.pointVente.id=:pointVenteId"
  )
  List<Operation> findOperationByPointVenteAndMonth(
    @Param("typeServiceId") Long typeServiceId,
    @Param("pointVenteId") Long pointVenteId
  );

 @Query(name = "operation.OperationCodeExterne",nativeQuery=true)
  public OperationOptimiseDTO findByCodeOperationEx(@Param("codeExterne") String codeExterne );
  
   @Query(value = "select op FROM Operation op WHERE op.codeExterne=?1")
  public Operation findByCodeOperationExterne(String codeExterne);

  //    @Query(value = "select sum(op) from Operation op where " +
  //            "day(op.dateOperation)=day(:date) group by hour(op.dateOperation) order by" +
  //            " hour(op.dateOperation) asc")
  //    public int[] findSumOperationOfDateGroupByHour(@Param("date") ZonedDateTime date);

  List<Operation> findAll();

  @Query("select op from Operation op where op.statut=:typeS ")
  List<Operation> findEtatByStatut(@Param("typeS") StatutOperation typeS);

  @Query(
    "select op from Operation op where op.statut=:typeS and  coalesce(op.dateExpiration,:maintenant )< :maintenant and op.typeService.code in :typeService"
  )
  List<Operation> findOperationExpire(
    @Param("typeS") StatutOperation typeStatu,
    @Param("maintenant") LocalDate now,
    @Param("typeService") List<String> typeService
  );

  List<Operation> findByCompteSrcValeurCompteAndDateOperationIsBetweenAndCompteSrcClientIsNotNullAndCompteDestIsNotNullAndCompteDestClientIsNotNull(
    String valeur,
    ZonedDateTime debut,
    ZonedDateTime fin
  );
  //Operation findTop1ByCompteDestValeurCompteOrderByDateOperationDesc(String valeurCompteSrc);
  Operation findTop1ByCompteSrcValeurCompteOrCompteDestValeurCompteOrderByDateOperationDesc(
    String valeurCompteSrc,
    String valeurCompteDest
  );

  //  Operation findTop1ByCompteDestValeurCompteO

  @Query(
    value = "select op from Operation op where  (op.typeService.id=:typeServiceId or :typeServiceId is null)  and (op.uoAcquereur.id=:uoAcquereurId or :uoAcquereurId is null)  and (op.dateOperation between :dateOperation and  :dateOperation2 or cast(:dateOperation  as timestamp) is null) order by dateOperation desc"
  )
  List<Operation> findAllOperationByAgent(
    @Param("typeServiceId") Long typeServiceId,
    @Param("uoAcquereurId") Long uoAcquereurId,
    @Param("dateOperation") ZonedDateTime dateOperation,
    @Param("dateOperation2") ZonedDateTime dateOperation2
  );

  @Query(
    " select op from Operation op where op.compteSrc.id=:compteBanqueId or op.compteDest.id=:compteBanqueId "
  )
  List<Operation> findOperationByCompteBanque(
    @Param("compteBanqueId") Long compteBanqueId
  );

  @Query("select count(o) from Operation  o")
  Integer countOpeation();
  
 @Query("select o.extraData from Operation  o where o.id=?1")
 String  getExtraData(Long id);
  
  @Query(name = "operation.findTop10LastOperation", nativeQuery = true)
  List<OperationOptimiseDTO> findTop10LastOperation(
    @Param("statut") List<String> statut,
    @Param("pointVenteIds") List<Long> pointVenteIds
  );

  @Query(name = "operation.findAllOperations",nativeQuery=true)
  List<OperationDTO> findOperationByMultiParamsOptimise(
    @Param("tsId") Long tsId,
    @Param("canalId") Long canalId,
    @Param("uoAcquereurId") Long uoAcquereurId,
    @Param("pointVenteIds") List<Long> pointVenteIds,
    @Param("montantOp") BigDecimal montantOp,
    @Param("statut") List<String> statut,
    @Param("codeOperation") String codeOperation,
    @Param("description") String description,
    @Param("dateOperation") ZonedDateTime dateOperation,
    @Param("dateOperation2") ZonedDateTime dateOperation2,
    @Param("taille") Long taile,
    @Param("page") Long page
  );

  @Query(
    value = "select count(op.id) from Operation op where op.typeService.id is not null  and (op.typeService.id=:typeServiceId or :typeServiceId is null) " +
    "and (op.canal.id=:canalId or :canalId is null) " +
    "and (op.uoAcquereur.id=:uoAcquereurId or :uoAcquereurId is null) " +
    "and (op.pointVente.id in :pointVenteIds) " +
    "and (op.montantOp=:montantOp or :montantOp is null) " +
    "and ((:statut) is null or op.statut in (:statut) ) " +
    "and (op.codeOperation=:codeOperation or :codeOperation is null) " +
    "and (:description is null or op.description=:codeOperation  ) " +
    "and (op.dateOperation between :dateOperation and  :dateOperation2 or cast(:dateOperation  as timestamp) is null)"
  )
  Integer countByMultiParamsOptimise(
    @Param("typeServiceId") Long typeServiceId,
    @Param("canalId") Long canalId,
    @Param("uoAcquereurId") Long uoAcquereurId,
    @Param("pointVenteIds") List<Long> pointVenteIds,
    @Param("montantOp") BigDecimal montantOp,
    @Param("statut") List<StatutOperation> statut,
    @Param("dateOperation") ZonedDateTime dateOperation,
    @Param("dateOperation2") ZonedDateTime dateOperation2,
    @Param("codeOperation") String codeOperation,
    @Param("description") String description
  );

  @Query(name = "operation.findAllOperationsByPeriode", nativeQuery = true)
  List<OperationOptimiseDTO> findAllOperationsByPeriode(
    @Param("taille") Long taile,
    @Param("page") Long page
  );

  @Query(name = "operation.findByOperationId", nativeQuery = true)
  OperationDTO findOperationById(@Param("id") Long id);

  @Query(
    "select count(o) from Operation  o where o.dateOperation between :dateStar and :dateEnd"
  )
  Integer countOpeationOptimise(
    @Param("dateStar") ZonedDateTime dateStar,
    @Param("dateEnd") ZonedDateTime dateEnd
  );

  @Query(name = "operation.findAllOperationByAndTypseServiceAndPointVente", nativeQuery = true)
  List<OperationOptimiseDTO> findAllOperationByAndTypseServiceAndPointVenteOptimise(
    @Param("tsId") Long tsId,
    @Param("pvId") List<Long> pvId,
    @Param("dateOperation") ZonedDateTime dateOperation,
    @Param("dateOperation2") ZonedDateTime dateOperation2,
    @Param("etape") String etape
  );

  @Query(name = "operation.findByValeurCompteSrcAndValeurCompteDest", nativeQuery = true)
  OperationOptimiseDTO findByValeurCompteSrcAndValeurCompteDest(
    @Param("valeurCompteSrc") String valeurCompteSrc,
    @Param("valeurCompteDest") String valeurCompteDest
  );

  @Query(name = "operation.findlastestOperation", nativeQuery = true)
  OperationDTO findLastestPVOperation(@Param("id") BigInteger id);

  @Query(
    value = "select distinct(b.id) from beneficiaire b where b.personne_id not in (select c.personne_id from client c where c.supprime=false)",
    nativeQuery = true
  )
  List<BigInteger> getOperationCompteBeneficiaireCompteBanque();

  @Query(
    name = "operation.findOperationByUoIdAndServiceIds",
    nativeQuery = true
  )
  List<OperationDTO> getAllOperationsByUoIdAndTypeService(
    @Param("uoId") Long uoId,
    @Param("serviceIds") List<Long> serviceIds
  );

  @Query(name = "operation.findAllOperationByMultiParamsOptimiser", nativeQuery = true)
  List<OperationDTO> findAllOperationByMultiParamsOptimiser(
    @Param("typeServiceId") Long typeServiceId,
    @Param("canalId") Long canalId,
    @Param("uoAcquereurId") Long uoAcquereurId,
    @Param("pointVenteIds") List<Long> pointVenteIds,
    @Param("montantOp") BigDecimal montantOp,
    @Param("statut") StatutOperation statut,
    @Param("dateOperation") ZonedDateTime dateOperation,
    @Param("dateOperation2") ZonedDateTime dateOperation2
  );

  @Query(
    "select count(*) from Operation where typeService.id=?1 and statut=?2 and montantOp=?3"
  )
  Long getTotalOperationByTypeServiceAndMontantEquals(
    Long typeServiceId,
    StatutOperation statut,
    BigDecimal montant
  );

  @Query(
    "select count(*) from Operation where typeService.id=?1 and statut=?2 and montantOp>?3"
  )
  Long getTotalOperationByTypeServiceAndMontantSup(
    Long typeServiceId,
    StatutOperation statut,
    BigDecimal montant
  );

  @Query(
    "select count(*) from Operation where typeService.id=?1 and statut=?2 and montantOp>=?3"
  )
  Long getTotalOperationByTypeServiceAndMontantSupEquals(
    Long typeServiceId,
    StatutOperation statut,
    BigDecimal montant
  );

  @Query(
    "select count(*) from Operation where typeService.id=?1 and statut=?2 and montantOp<?3"
  )
  Long getTotalOperationByTypeServiceAndMontantINF(
    Long typeServiceId,
    StatutOperation statut,
    BigDecimal montant
  );

  
  
  @Query(name = "operation.findLesTroisDernierOperation", nativeQuery = true)
     List<MOperationNumbLastestDTO> findLast3Operation(@Param("personneId") Long personneId);
  
  
  
  @Query(
    "select count(*) from Operation where typeService.id=?1 and statut=?2 and montantOp<=?3"
  )
  Long getTotalOperationByTypeServiceAndMontantINFEquals(
    Long typeServiceId,
    StatutOperation statut,
    BigDecimal montant
  );
// Statut Get Value
    @Query(name = "operation.getOperationStatut", nativeQuery = true)
  List<OperationStatutDTO> getOperationStatut(@Param("operationId") Long operationId);
  
  @Query(name = "operation.findOperationReglementEnAttente", nativeQuery = true)
  List<OperationOptimiseDTO> findOperationReglementEnAttente(@Param("statut") String statut,@Param("typeServiceId") Long typeServiceId);
  
  @Query(value="select * from operation op WHERE (cast(:dateDebut  as timestamp) is null IS NULL OR op.date_operation BETWEEN :dateDebut AND :dateFin) and type_service_id is not null ORDER BY op.date_operation DESC LIMIT :taille",nativeQuery = true)
  List<Operation> findByDateLimite(@Param("dateDebut") ZonedDateTime dateDebut ,@Param("dateFin") ZonedDateTime dateFin,@Param("taille") Long taille);

}
