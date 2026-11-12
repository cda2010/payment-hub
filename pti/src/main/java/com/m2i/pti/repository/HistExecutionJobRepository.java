package com.m2i.pti.repository;

import com.m2i.pti.domain.HistExecutionJob;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;

/**
 * Spring Data JPA repository for the HistExecutionJob entity.
 */
@SuppressWarnings("unused")
@Repository
public interface HistExecutionJobRepository extends JpaRepository<HistExecutionJob, Long> {
    Page<HistExecutionJob> findAllByPtiJobDetailId(Long id, Pageable pageable);

}
