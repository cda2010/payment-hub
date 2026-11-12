package com.m2i.pti.repository;

import com.m2i.pti.domain.LigneRepartitionCommission;
import java.math.BigDecimal;

import com.m2i.pti.service.dto.LigneRepartitionCommissionDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;

import java.math.BigDecimal;
import java.util.List;


/**
 * Spring Data JPA repository for the LigneRepartitionCommission entity.
 */
@SuppressWarnings("unused")
@Repository
public interface LigneRepartitionCommissionRepository extends JpaRepository<LigneRepartitionCommission, Long> {


    
    @Query("select l from LigneRepartitionCommission l where l.tableauRepartitionCommission.id =?1 and l.max >=?2 and l.min <=?3")
     List<LigneRepartitionCommission> findByTableauRepartitionCommissionAnMinMax(Long tableauId, BigDecimal max, BigDecimal min);
     
     
     
    List<LigneRepartitionCommission> findByTableauRepartitionCommissionId(Long tableauId);

    List<LigneRepartitionCommission> findByTableauRepartitionCommissionIdAndMinAndMax(Long tableauRepartitionCommission_id, BigDecimal min, BigDecimal max);
  //  List<LigneRepartitionCommission> findByTableauRepartitionCommissionId(Long tableauRepartitionCommission_id);
    List<LigneRepartitionCommission> findByTableauRepartitionCommissionIdAndNiveau(Long tableauId,Integer niveau);


    
   // @Query("select l from LigneRepartitionCommission l where l.tableauRepartitionCommission.id =?1 and l.max >=?2 and l.min <=?3")
 //    List<LigneRepartitionCommission> findByTableauRepartitionCommissionAnMinMax(Long tableauId, BigDecimal max, BigDecimal min);
  //   List<LigneRepartitionCommission> findByTableauRepartitionCommissionIdAndMinAndMax(Long tableauRepartitionCommission_id, BigDecimal min, BigDecimal max);
    // List<LigneRepartitionCommission> findByTableauRepartitionCommissionId(Long tableauRepartitionCommission_id);
 //    List<LigneRepartitionCommission> findByTableauRepartitionCommissionIdAndNiveau(Long tableauId,Integer niveau);

    Page<LigneRepartitionCommission> findByTableauRepartitionCommissionId(Long tableauId, Pageable pageable);



    @Query("select l from LigneRepartitionCommission l where l.tableauRepartitionCommission.id=?1")
    List<LigneRepartitionCommission> findByTableauRepartitionCommissionListId(Long tableauId);

    boolean existsByNiveauAndTableauRepartitionCommissionId(Integer niveau,Long tableauId);

    @Query(name = "LigneRepartitionCommission.findTableauRepartitionCommissionId", nativeQuery = true)
    List<LigneRepartitionCommissionDTO> findByTableauRepartitionCommissionOptimise(@Param("tableauId") Long tableauId);


}
