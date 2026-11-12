package com.m2i.pti.repository;

import com.m2i.pti.domain.TaxeOperation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


/**
 * Spring Data  repository for the TaxeOperation entity.
 */
@SuppressWarnings("unused")
@Repository
public interface TaxeOperationRepository extends JpaRepository<TaxeOperation, Long> {

    @Query(value = "select distinct taxeOperation from TaxeOperation taxeOperation left join fetch taxeOperation.typeServices",
            countQuery = "select count(distinct taxeOperation) from Taxe taxeOperation")
    Page<TaxeOperation> findAllWithEagerRelationships(Pageable pageable);

    @Query(value = "select distinct taxeOperation from TaxeOperation taxeOperation left join fetch taxeOperation.typeServices")
    List<TaxeOperation> findAllWithEagerRelationships();

    @Query("select taxeOperation from TaxeOperation taxeOperation left join fetch taxeOperation.typeServices")
    Optional<TaxeOperation> findOneWithEagerRelationships(@Param("id") Long id);

    List<TaxeOperation> findByTypeServices_Id(Long id);

    List<TaxeOperation> findByUniteOrganisationIdAndTaxeIdAndTypeServices_Id(Long orgId,Long taxeId,Long typeId);

    List<TaxeOperation> findByUniteOrganisationIdAndTaxeIdAndTypeServices_IdAndZoneIdAndActivatedIsTrue(Long orgId, Long taxeId, Long typeId, Long zoneId);
}
