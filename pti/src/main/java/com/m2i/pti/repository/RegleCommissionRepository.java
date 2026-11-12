package com.m2i.pti.repository;

import com.m2i.pti.domain.RegleCommission;
import com.m2i.pti.domain.enumeration.TypeOperateur;
import java.math.BigDecimal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;

import java.time.LocalDate;
import java.util.List;


/**
 * Spring Data JPA repository for the RegleCommission entity.
 */
@SuppressWarnings("unused")
@Repository
public interface RegleCommissionRepository extends JpaRepository<RegleCommission, Long> {
    RegleCommission findByTypeOperateurAndTeneurSrcIdAndTeneurDestIdAndTypeServiceIdAndDateDebutIsLessThanEqualAndDateFinIsGreaterThanEqual(TypeOperateur typeOperateur, Long teneurSrcId, Long teneurDestId, Long typeServiceId, LocalDate dateDebut, LocalDate dateFin);

    @Query(value = "select f from RegleCommission f where (f.typeService.id=:typeServiceId or :typeServiceId is null ) " +
            " And (f.teneurSrc.id=:teneurSourceId or :teneurSourceId is null) " +
            " And (f.teneurDest.id=:teneurDestId or :teneurDestId is null) ",
            countQuery = "select f from RegleCommission f where (f.typeService.id=:typeServiceId or :typeServiceId is null ) " +
                    " And (f.teneurSrc.id=:teneurSourceId or :teneurSourceId is null) " +
                    " And (f.teneurDest.id=:teneurDestId or :teneurDestId is null) ")
    Page<RegleCommission> findByMultiCriteria(@Param("typeServiceId") Long typeServiceId, @Param("teneurSourceId") Long teneurSourceId, @Param("teneurDestId") Long teneurDestId, Pageable pageable);

    @Query(value = "select f from RegleCommission f where (f.typeService.id=:typeServiceId or f.typeService.id is null ) " +
            " And (f.teneurSrc.id=:teneurSourceId or f.teneurSrc.id is null) " +
            " And (f.teneurDest.id=:teneurDestId or f.teneurDest.id is null) " +
            " And (f.uoSrc.id=:uoSrcId or f.uoSrc.id is null) " +
            " And (f.uoDest.id=:uoDestId or f.uoSrc.id is null) " +
            " And (f.typeOperateur=:typeOperateur or f.typeOperateur is null) " +
            " And f.activated=true order by f.poidsNum desc ")
    List<RegleCommission> findByCriteria(@Param("typeServiceId") Long typeServiceId, @Param("teneurSourceId") Long teneurSourceId, @Param("teneurDestId") Long teneurDestId, @Param("uoDestId") Long uoDestId, @Param("uoSrcId") Long uoSrcId, @Param("typeOperateur") TypeOperateur typeOperateur);
 @Query(value = "select f from RegleCommission f where (f.typeService.id=:typeServiceId or f.typeService.id is null ) " +
            " And (f.teneurSrc.id=:teneurSourceId or f.teneurSrc.id is null) " +
            " And (f.teneurDest.id=:teneurDestId or f.teneurDest.id is null) " +
            " And (f.uoSrc.id=:uoSrcId or :uoSrcId is null) " +
            " And (f.uoDest.id=:uoDestId or :uoDestId  is null) " +
            " And (f.typeOperateur=:typeOperateur or f.typeOperateur is null) " +
            " And f.activated=true And (  (f.min <=:montant or f.min  is null) and (f.max >=:montant or f.max  is null)) order by f.poidsNum desc " )
   List<RegleCommission> findByCriteria(@Param("typeServiceId") Long typeServiceId, @Param("teneurSourceId") Long teneurSourceId, @Param("teneurDestId") Long teneurDestId, @Param("uoDestId") Long uoDestId, @Param("uoSrcId") Long uoSrcId, @Param("typeOperateur") TypeOperateur typeOperateur,@Param("montant") BigDecimal montant);
   
}
