package com.m2i.pti.repository;

import com.m2i.pti.domain.BilanOperation;
import com.m2i.pti.service.dto.BilanOperationDTO;
import com.m2i.pti.service.dto.ClientDTO;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import jakarta.persistence.LockModeType;


/**
 * Spring Data JPA repository for the BilanOperation entity.
 */
@SuppressWarnings("unused")
@Repository
public interface BilanOperationRepository extends JpaRepository<BilanOperation, Long> {

    @Query("select b from BilanOperation b where (b.acteur.id=:acteurId) and b.typeService.id=:typeServiceId  And b.canal.id=:canalId  And (b.clientBeneficiaire.id=:clientBeneficiaireId or b.clientBeneficiaire.id is null) " +
            " And (b.clientEmetteur.id=:clientEmetteurId or b.clientEmetteur.id is null) And b.organisationEm.id =:uoEmetteurId And b.organisationAcq.id =:uoAcqId  And b.dateDerniereOperation=:dateDerniereOperation ")
    List<BilanOperation> findFraisByMultiParamsAndActeur(@Param("acteurId") Long acteurId, @Param("typeServiceId") Long typeServiceId, @Param("canalId") Long canalId, @Param("clientBeneficiaireId") Long clientBeneficiaireId, @Param("clientEmetteurId") Long clientEmetteurId, @Param("uoEmetteurId") Long uoEmetteurId, @Param("uoAcqId") Long uoAcqId, @Param("dateDerniereOperation") LocalDate dateDerniereOperation);

    
//      @Query("select b from BilanOperation b where b.acteur.id is null and b.typeService.id=:typeServiceId  And b.canal.id=:canalId  "
//            + "And (b.clientBeneficiaire.id=:clientBeneficiaireId or b.clientBeneficiaire.id is null) " +
//            " And (b.clientEmetteur.id=:clientEmetteurId or b.clientEmetteur.id is null) And b.organisationEm.id =:uoEmetteurId And b.organisationAcq.id =:uoAcqId  And b.dateDerniereOperation=:dateDerniereOperation ")
//    
    
   // List<BilanOperation> findFraisByMultiParamsAndClient(@Param("typeServiceId") Long typeServiceId, @Param("canalId") Long canalId, @Param("clientBeneficiaireId") Long clientBeneficiaireId, @Param("clientEmetteurId") Long clientEmetteurId, @Param("uoEmetteurId") Long uoEmetteurId, @Param("uoAcqId") Long uoAcqId, @Param("dateDerniereOperation") LocalDate dateDerniereOperation);
//@Lock(LockModeType.PESSIMISTIC_READ )
    @Query("select b from BilanOperation b where b.acteur.id is null and b.typeService.id=:typeServiceId  And b.canal.id = :canalId  "
            + "And (b.clientBeneficiaire.id = :clientBeneficiaireId ) " +
            "  And b.organisationEm.id = :uoEmetteurId And b.organisationAcq.id = :uoAcqId  And b.dateDerniereOperation = :dateDerniereOperation ")
    List<BilanOperation> findFraisByMultiParamsAndClient(@Param("typeServiceId") Long typeServiceId, @Param("canalId") Long canalId, @Param("clientBeneficiaireId") Long clientBeneficiaireId, @Param("uoEmetteurId") Long uoEmetteurId, @Param("uoAcqId") Long uoAcqId, @Param("dateDerniereOperation") LocalDate dateDerniereOperation);

    
    @Query("select b from BilanOperation b where (b.acteur.id=:acteurId or :acteurId is null) and b.typeService.id=:typeServiceId  And b.canal.id = :canalId  And (b.beneficiaire.id = :clientBeneficiaireId or :clientBeneficiaireId is null) " +
            " And (b.emetteur.id = :clientEmetteurId or :clientEmetteurId is null) And (b.organisationEm.id =:uoEmetteurId or b.organisationEm.id is null) And (b.organisationAcq.id = :uoAcqId or b.organisationAcq.id is null)" +
            " and b.dateDerniereOperation between :dateDerniereOperation and :dateDerniereOperation2")
    List<BilanOperation> findByMultiParamsAndActeurBetweenDateDebutAndDateFin(@Param("acteurId") Long acteurId, @Param("typeServiceId") Long typeServiceId,
                                                                              @Param("canalId") Long canalId, @Param("clientBeneficiaireId") Long clientBeneficiaireId,
                                                                              @Param("clientEmetteurId") Long clientEmetteurId, @Param("uoEmetteurId") Long uoEmetteurId,
                                                                              @Param("uoAcqId") Long uoAcqId, @Param("dateDerniereOperation") LocalDate dateDerniereOperation,
                                                                              @Param("dateDerniereOperation2") LocalDate dateDerniereOperation2);


//    @Query("select b from BilanOperation b where (b.acteur.id=:acteurId or :acteurId is null ) and (b.typeService.id=:typeServiceId or :typeServiceId is null)   And b.canal.id=:canalId "
//            + " And (( b.clientBeneficiaire.id=:clientBeneficiaireId or b.clientEmetteur.id=:clientBeneficiaireId) or :clientBeneficiaireId is null) " +
//            " And ( (b.clientEmetteur.id=:clientEmetteurId or b.clientBeneficiaire.id=:clientEmetteurId)  or :clientEmetteurId is null) And ((b.organisationEm.id =:uoEmetteurId or :uoEmetteurId  is null) or (b.organisationAcq.id =:uoAcqId or :uoAcqId is null))" +
//            " and b.dateDerniereOperation between :dateDerniereOperation and :dateDerniereOperation2")
//            
//                List<BilanOperation> findByMultiParamsAndActeurBetweenDateDebutAndDateFinAll(@Param("acteurId") Long acteurId, @Param("typeServiceId") Long typeServiceId,
//                                                                                 @Param("canalId") Long canalId, @Param("clientBeneficiaireId") Long clientBeneficiaireId,
//                                                                                 @Param("clientEmetteurId") Long clientEmetteurId, @Param("uoEmetteurId") Long uoEmetteurId,
//                                                                                 @Param("uoAcqId") Long uoAcqId, @Param("dateDerniereOperation") LocalDate dateDerniereOperation,
//                                                                                 @Param("dateDerniereOperation2") LocalDate dateDerniereOperation2);
    @Query("select b from BilanOperation b where (b.acteur.id = :acteurId or :acteurId is null ) and (b.typeService.id = :typeServiceId or :typeServiceId is null)   And b.canal.id = :canalId "
            + " And (( b.clientBeneficiaire.id = :clientBeneficiaireId ) ) " +
            "  And ((b.organisationEm.id = :uoEmetteurId or :uoEmetteurId  is null) or (b.organisationAcq.id = :uoAcqId or :uoAcqId is null))" +
            " and b.dateDerniereOperation between :dateDerniereOperation and :dateDerniereOperation2")
      
    List<BilanOperation> findByMultiParamsAndActeurBetweenDateDebutAndDateFinAll(@Param("acteurId") Long acteurId, @Param("typeServiceId") Long typeServiceId,
                                                                                 @Param("canalId") Long canalId, @Param("clientBeneficiaireId") Long clientBeneficiaireId,
                                                                                @Param("uoEmetteurId") Long uoEmetteurId,
                                                                                 @Param("uoAcqId") Long uoAcqId, @Param("dateDerniereOperation") LocalDate dateDerniereOperation,
                                                                                 @Param("dateDerniereOperation2") LocalDate dateDerniereOperation2);


   @Query("select b from BilanOperation b where b.acteur.id is null and b.typeService.id = :typeServiceId  And b.canal.id = :canalId  "
          + "And (b.clientBeneficiaire.id=:clientBeneficiaireId ) " +
          "  And b.organisationEm.id =:uoEmetteurId And b.organisationAcq.id = :uoAcqId  And b.dateDerniereOperation = :dateDerniereOperation ")
   List<BilanOperation> findFraisByMultiParamsAndClientX(@Param("typeServiceId") Long typeServiceId, @Param("canalId") Long canalId, @Param("clientBeneficiaireId") Long clientBeneficiaireId, @Param("uoEmetteurId") Long uoEmetteurId, @Param("uoAcqId") Long uoAcqId, @Param("dateDerniereOperation") LocalDate dateDerniereOperation);
   
    @Query("select b from BilanOperation b where b.acteur.id is null and b.typeService.id = :typeServiceId    "
          + " And (b.clientEmetteur.id=:clientEmeteurId ) " +
          "   And b.dateDerniereOperation = :dateDerniereOperation ")
   List<BilanOperation> findBilanClient(@Param("typeServiceId") Long typeServiceId,  @Param("clientEmeteurId") Long clientEmeteurId, @Param("dateDerniereOperation") LocalDate dateDerniereOperation);
   
   
//     @Query(value="select * from bilan_operation b where b.acteur_id is null and b.type_service_id=:typeServiceId  And b.canal_id=:canalId  "
//            + "And (b.client_beneficiaire_id=:clientBeneficiaireId ) " +
//            "  And b.organisation_em_id =:uoEmetteurId And b.organisation_acq_id =:uoAcqId  And b.date_derniere_operation=:dateDerniereOperation ", nativeQuery = true)
//   /// List<BilanOperation> findFraisByMultiParamsAndClientX(@Param("typeServiceId") Long typeServiceId, @Param("canalId") Long canalId, @Param("clientBeneficiaireId") Long clientBeneficiaireId, @Param("uoEmetteurId") Long uoEmetteurId, @Param("uoAcqId") Long uoAcqId, @Param("dateDerniereOperation") LocalDate dateDerniereOperation);
//

    @Query("SELECT count (b.id) FROM BilanOperation b  WHERE " +
            "(" +
            "(b.clientBeneficiaire.id=:personneId or b.clientEmetteur.id=:personneId) and  (b.dateDerniereOperation between :dateDebut and  :dateFin or cast(:dateDebut  as timestamp) is null) )")
    Integer countBilanOperationByPersonne(@Param("personneId") Long personneId,@Param("dateDebut") LocalDate dateDebut,@Param("dateFin") LocalDate dateFin);

    @Query(name = "BilanOperation.findBilanOpByPersonneId",nativeQuery = true)
    List<BilanOperationDTO> findBilanOpByPersonneId(@Param("personneId") Long personneId, @Param("taille") Long taille, @Param("page") Long page);

    @Query(name = "BilanOperation.findBilanOpByPersonneIdDate",nativeQuery = true)
    List<BilanOperationDTO> findBilanOpByPersonneIdDate(@Param("personneId") Long personneId,@Param("debut") LocalDate debut,@Param("fin") LocalDate fin, @Param("taille") Long taille, @Param("page") Long page);


}