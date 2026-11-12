package com.m2i.pti.repository;

import com.m2i.pti.domain.Commission;
import com.m2i.pti.domain.Operation;
import com.m2i.pti.domain.enumeration.Acteur;
import com.m2i.pti.domain.enumeration.EtatValidite;
import com.m2i.pti.service.dto.CommissionDTO;
import com.m2i.pti.service.dto.UniteOrganisationDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;


/**
 * Spring Data JPA repository for the Commission entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CommissionRepository extends JpaRepository<Commission, Long> {
    @Query(value = "select c from Commission c where (c.estPaye=:estPaye or :estPaye is null ) And (c.organisation.id=:uoId or :uoId is null ) And (c.dateEnregistrement between :dateEnregistrement and :dateEnregistrement2 or cast(:dateEnregistrement  as timestamp) is null)",
            countQuery = "select count(c) from Commission c where (c.estPaye=:estPaye or :estPaye is null ) And (c.organisation.id=:uoId or :uoId is null ) and (c.dateEnregistrement between :dateEnregistrement and :dateEnregistrement2 or cast(:dateEnregistrement  as timestamp) is null)")
    Page<Commission> findCommissionByMultiCriteria(@Param("estPaye") Boolean estPaye, @Param("uoId") Long uoId, @Param("dateEnregistrement") ZonedDateTime dateEnregistrement, @Param("dateEnregistrement2") ZonedDateTime dateEnregistrement2, Pageable pageable);

    List<Commission> findByEstPayeAndEstEmetteur(Boolean estPaye, Boolean estEmetteur);

    List<Commission> findByEstPayeAndEstEmetteurAndOrganisationId(Boolean estPaye, Boolean estEmetteur, Long orgId);

    List<Commission> findByEstPayeAndOrganisationId(Boolean estPaye, Long orgId);

    List<Commission> findByEstComptabiliseAndOrganisationId(Boolean estPaye, Long orgId);

    List<Commission> findByActeurAndEstPaye(Acteur acteur, Boolean estPaye);

    List<Commission> findByEstPayeAndEstEmetteurAndEstComptabilise(Boolean estPaye, Boolean estEmetteur, Boolean estComptabilise);

    List<Commission> findByEstPayeAndEstComptabiliseAndEtatAndOrganisationId(Boolean estPaye, Boolean estComptabilise, EtatValidite etat, Long orgId);

    List<Commission> findByEstPayeAndEstEmetteurAndEstComptabiliseAndEtat(Boolean estPaye, Boolean estEmetteur, Boolean estComptabilise, EtatValidite etat);

    @Query(value = "select c from Commission c where (c.estPaye=:estPaye or :estPaye is null ) And (c.organisation.id=:uoId or :uoId is null ) and (c.dateEnregistrement between :dateEnregistrement and :dateEnregistrement2 or cast(:dateEnregistrement  as timestamp) is null)")
    List<Commission> findAllCommissionByMultiCriteria(@Param("estPaye") Boolean estPaye, @Param("uoId") Long uoId, @Param("dateEnregistrement") ZonedDateTime dateEnregistrement, @Param("dateEnregistrement2") ZonedDateTime dateEnregistrement2);

    List<Commission> findByOperationIdAndEstPayeAndEstComptabilise(Long operationId, Boolean estPaye, Boolean estComptabilise);

    List<Commission> findByEstComptabiliseAndEtat(Boolean estComptabilise, EtatValidite etatValidite);
    List<Commission> findByOperationIdAndEstComptabiliseAndEtat(Long operationId ,Boolean estComptabilise, EtatValidite etatValidite);
      List<Commission> findByEstPayeAndEstComptabiliseAndEtatAndEnTraitementAndOrganisationIdAndOperationId(Boolean estPaye, Boolean estComptabilise, EtatValidite etatValidite,Boolean enTraitement ,Long orgId, Long operationId);
      Page<Commission> findByOperationId(Long operationId, Pageable pageable);

      @Query(" select coalesce(sum(coalesce(montant,0)),0) from Commission c where c.organisation.id=:UoId and c.estPaye=:estPay and c.dateEnregistrement <= :dateEnregistrement ")
    BigDecimal findCommissionOfUoBeforeDateReglement(@Param("UoId") Long UoId, @Param("estPay") Boolean estPay, @Param("dateEnregistrement") ZonedDateTime dateEnregistrement);
    @Query(" select c from Commission c where c.montant >0 and c.ligneReglement is null and c.organisation.id=:UoId and  c.estPaye=true  and (c.estRegle is null or c.estRegle=false) and c.dateEnregistrement <= :dateEnregistrement ")
     List<Commission> findListCommissionOfUoBeforeDateReglement(@Param("UoId") Long UoId, @Param("dateEnregistrement") ZonedDateTime dateEnregistrement);
     
     @Query("select coalesce(sum(c.montant),0) from Commission c where c.montant >0 and c.ligneReglement is null and c.organisation.id=:UoId and  c.estPaye=true  and (c.estRegle is null or c.estRegle=false) and c.dateEnregistrement <= :dateEnregistrement ")
     BigDecimal getMontantTotalCommission(@Param("UoId") Long UoId, @Param("dateEnregistrement") ZonedDateTime dateEnregistrement);
        @Modifying
     @Query(value="UPDATE Commission SET ligneReglement.id=:ligneId,  resteApayer=:resteApayer where id =:commissionId")
     void updateReglementCommissionById( @Param("ligneId") Long ligneId,@Param("commissionId") Long commissionId ,@Param("resteApayer") BigDecimal resteApayer  );
     
     
     
       @Modifying
     @Query(value="UPDATE Commission SET  resteApayer=:resteApayer where id =:commissionId")
     void updateRestantAPayerCommissionById( @Param("commissionId") Long commissionId ,@Param("resteApayer") BigDecimal resteApayer  );
     
   
     
      @Modifying
     @Query(value="UPDATE Commission SET estRegle=true where id =:commissionId")
     void updateReglementCommissionEstRegle( @Param("commissionId") Long commissionId  );
     
     @Modifying
     @Query(value="UPDATE Commission SET ligneReglement.id=null where id =:commissionId")
     void updateDetacherReglementCommissionById(@Param("commissionId") Long commissionId  );
   
     
     
     
     
     
     
     @Query(value="select c.id from commission c where c.montant >0 and c.ligne_reglement_id is null and c.organisation_id=:UoId and  c.est_paye=true  and (c.est_regle is null or c.est_regle=false) and c.date_enregistrement <= :dateEnregistrement",nativeQuery=true)
     List<Long> findListIdCommissionOfUoBeforeDateReglement(@Param("UoId") Long UoId, @Param("dateEnregistrement") ZonedDateTime dateEnregistrement);
     @Query(name = "Commission.findListCommissionOfUoBeforeDateReglement", nativeQuery = true)
     List<CommissionDTO> findListCommissionOfUoBeforeDateReglementOptimiser(@Param("UoId") Long UoId, @Param("dateEnregistrement") ZonedDateTime dateEnregistrement);

     @Query(value="select * from commission c where c.ligne_reglement_id =?1", nativeQuery = true)
    public List<Commission> findListCommissionOfLigneReglement(Long id);

    
     @Query(name = "Commission.findLigneReglement", nativeQuery = true)
    public List<CommissionDTO> findListCommissionOfLigneReglementOptimise( @Param("ligne") Long ligne);
    
    
    
    @Query(value = "select DISTINCT c.operation from Commission c where (c.estPaye=?1 And c.estComptabilise=?2 and c.etat=?3)")
    public List<Operation> findByDistinctOperationEstPayeAndEstComptabilise(Boolean estPaye, Boolean estComptabilise,EtatValidite etat);
    
   @Query(name = "Commission.findCommissionNonPaye", nativeQuery = true)
  List<CommissionDTO>   findByCommissionPasPayeAndEstComptabilise();
  
   @Query(name = "Commission.findCommissionNonPayeOrganisation", nativeQuery = true)
  List<CommissionDTO>   findByCommissionOrganisationPasPayeAndEstComptabilise(@Param("uoId") Long uoId);
    
    
   List<Commission> findByEstPayeAndEstComptabiliseAndEtatAndEnTraitement(Boolean estPaye, Boolean estComptabilise, EtatValidite etatValidite,Boolean enTraitement);

    @Query("select count(c.id) from Commission c where (c.estPaye=:estPaye or :estPaye is null ) And (c.organisation.id=:uoId or :uoId is null ) and (c.dateEnregistrement between :dateEnregistrement and :dateEnregistrement2 or cast(:dateEnregistrement  as timestamp) is null)")
    Integer countCommissionByMultiCriteria(@Param("estPaye") Boolean estPaye, @Param("uoId") Long uoId, @Param("dateEnregistrement") ZonedDateTime dateEnregistrement, @Param("dateEnregistrement2") ZonedDateTime dateEnregistrement2);

    @Query(name = "Commission.findAllByCriteria", nativeQuery = true)
    List<CommissionDTO> findCommissionByMultiCriteriaOptimise(@Param("estPaye") Boolean estPaye, @Param("uoId") Long uoId, @Param("dateEnregistrement") ZonedDateTime dateEnregistrement, @Param("dateEnregistrement2") ZonedDateTime dateEnregistrement2, @Param("taille") Long taille, @Param("page") Long page);

    @Query(name = "Commission.findAllByUoIdAndEstPayer", nativeQuery = true)
    List<CommissionDTO> findCommissionByUoIdAndEstPayer( @Param("uoId") Long uoId,@Param("estPaye") Boolean estPaye);

    @Query(value ="SELECT coalesce(sum(c.montant),0)" +
                                  " FROM commission c" +
                                  " INNER JOIN operation op on c.operation_id=op.id" +
                                  " INNER JOIN unite_organisation uo on c.organisation_id=uo.id" +
                                  " INNER JOIN type_service ts on op.type_service_id=ts.id" +
                                  " Where (" +
                                  "(:estPaye is null or c.est_paye= CAST(CAST (:estPaye AS TEXT) AS boolean ))" +
                                  "and (:uoId is null or c.organisation_id= CAST(CAST (:uoId AS TEXT) AS BIGINT ) ) and c.created_date between :debut and :fin )",nativeQuery = true)
    BigDecimal sommeCommissionByUoIdAndEstPayer(@Param("uoId") Long uoId,@Param("estPaye") Boolean estPaye,@Param("debut") ZonedDateTime debut,@Param("fin") ZonedDateTime fin);

    
}
