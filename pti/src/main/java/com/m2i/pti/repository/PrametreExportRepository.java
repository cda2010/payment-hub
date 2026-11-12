package com.m2i.pti.repository;

import com.m2i.pti.domain.ParametreExport;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;

import org.springframework.data.domain.Pageable;

import org.springframework.data.domain.Page;


/**
 * Spring Data JPA repository for the PrametreExport entity.
 */
@SuppressWarnings("unused")
@Repository
public interface PrametreExportRepository extends JpaRepository<ParametreExport, Long> {

    @Query(value = "select p from ParametreExport p where p.exportTable.id=?1")
    Page<ParametreExport> findParamExportByExportId(Long exportId, Pageable pageable);

}
