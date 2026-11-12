package com.m2i.pti.repository;

import com.m2i.pti.domain.RepartitionFrais;
import com.m2i.pti.domain.enumeration.Acteur;
import java.math.BigDecimal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * Spring Data JPA repository for the RepartitionFrais entity.
 */
@SuppressWarnings("unused")
@Repository
public interface RepartitionFraisRepository extends JpaRepository<RepartitionFrais, Long> {
    
         
     @Query("select f from RepartitionFrais f where f.typeService.id=:typeServiceId and f.acteur=:acteur " +
            " And (( f.min<=:montantMin " +
            " And f.max>=:montantMax and f.surFrais=true )or (( f.min<=:montantMinOp " +
            " And f.max>=:montantMaxOp and f.surFrais=false ))) And f.activated=true" +
            " order by f.poidsNum desc ")
    RepartitionFrais findByTypeServiceIdAndActeurAndMinLessThanEqualAndMaxGreaterThanEqualAndActivatedIsTrue(
            
            @Param("typeServiceId") Long typeServiceId, @Param("acteur") Acteur acteur, @Param("montantMin") Double montantMin, 
            @Param("montantMax") Double montantMax, @Param("montantMinOp") Double montantMinOp, @Param("montantMaxOp") Double montantMaxOp
            
          );

   // RepartitionFrais findByTypeServiceIdAndActeurAndFseSourceIdAndFseDestAndMinLessThanEqualAndMaxGreaterThanEqualAndActivatedIsTrue(Long typeServiceId, Acteur acteur, Long fseSrcId, Long fseDestId, Integer montantMin, Integer montantMax);

    RepartitionFrais findByTypeServiceIdAndActeur(Long typeServiceId, Acteur acteur);

    Page<RepartitionFrais> findByTypeServiceId(Long typeServiceId, Pageable pageable);

    @Query(value = "select f from RepartitionFrais f where ( :typeServiceId is null or f.typeService.id=:typeServiceId) " +
            "and (:acteur is null or  f.acteur=:acteur) " +
            " And ( :fseSourceId is null or f.fseSource.id=:fseSourceId) " +
            " And (:fseDestId is null or f.fseDest.id=:fseDestId) ")
    Page<RepartitionFrais> findByMultiCriteria(@Param("typeServiceId") Long typeServiceId, @Param("acteur") Acteur acteur, @Param("fseSourceId") Long fseSourceId, @Param("fseDestId") Long fseDestId, Pageable pageable);


    @Query("select f from RepartitionFrais f where (f.typeService.id=:typeServiceId or f.typeService.id is null ) and f.acteur=:acteur " +
            " And (f.fseSource.id=:fseSourceId or f.fseSource.id is null) " +
            " And (f.fseDest.id=:fseDestId or f.fseDest.id is null) " +
            " And f.min<=:montantMin " +
            " And f.max>=:montantMax And f.activated=true" +
            " order by f.poidsNum desc ")
    List<RepartitionFrais> findByMultiParams(@Param("typeServiceId") Long typeServiceId, @Param("acteur") Acteur acteur, @Param("fseSourceId") Long fseSourceId, @Param("fseDestId") Long fseDestId, @Param("montantMin") Double montantMin, @Param("montantMax") Double montantMax);

    
    
     @Query("select f from RepartitionFrais f where (f.typeService.id=:typeServiceId or f.typeService.id is null ) and f.acteur=:acteur " +
            " And (f.fseSource.id=:fseSourceId or f.fseSource.id is null) " +
            " And (f.fseDest.id=:fseDestId or f.fseDest.id is null) " +
            " And (( f.min<=:montantMin  And f.max>=:montantMax and f.surFrais=true ) or (( f.min<=:montantMinOp  And f.max>=:montantMaxOp and f.surFrais=false ))) And f.activated=true" +
            " order by f.poidsNum desc ")
    List<RepartitionFrais> findByMultiParams(@Param("typeServiceId") Long typeServiceId, @Param("acteur") Acteur acteur, @Param("fseSourceId") Long fseSourceId, @Param("fseDestId") Long fseDestId, @Param("montantMin") Double montantMin, @Param("montantMax") Double montantMax, @Param("montantMinOp") Double montantMinOp, @Param("montantMaxOp") Double montantMaxOp);

    
    
}
