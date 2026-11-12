package com.m2i.pti.repository;

import com.m2i.pti.domain.ListeDiffusion;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;


/**
 * Spring Data JPA repository for the ListeDiffusion entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ListeDiffusionRepository extends JpaRepository<ListeDiffusion, Long> {

}
