package com.m2i.pti.repository;

import com.m2i.pti.domain.LigneReglement;
import java.util.List;

import com.m2i.pti.service.LigneReglementDTO;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;


/**
 * Spring Data JPA repository for the LigneReglement entity.
 */
@SuppressWarnings("unused")
@Repository
public interface LigneReglementRepository extends JpaRepository<LigneReglement, Long> {
@Query("select l from LigneReglement l where l.reglement.id=?1 and l.valide=false and (l.reglement.estAnnuler is null or l.reglement.estAnnuler=false) and (l.estDetacher is null or l.estDetacher=false) ")
     List<LigneReglement> findByReglementIdAndValideIsFalse(Long reglementId);
    List<LigneReglement> findByReglementId(Long reglementId);
    Integer countByReglementIdAndValideIsTrue(Long reglementId);
    Integer countByReglementIdAndValideIsFalse(Long reglementId);
    Integer countByReglementId(Long reglementId);
    @Query("select coalesce(sum(l.montantCommission),0) from LigneReglement l where l.reglement.id=?1")
    Double getMontantTotal(Long reglementId);
    @Query(name = "ligneReglement.findByIdReglement",nativeQuery = true)
    List<LigneReglementDTO> findByIdReglement(@Param("idReglement") Long idReglement);
}
