package com.m2i.pti.repository;

import com.m2i.pti.domain.ExportTable;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;


/**
 * Spring Data JPA repository for the ExportTable entity.
 */
/**
 *
 * @author cda
 */
@SuppressWarnings("unused")
@Repository
public interface ExportTableRepository extends JpaRepository<ExportTable, Long>{
    
}
