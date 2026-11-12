package com.m2i.pti.repository;

import com.m2i.pti.domain.Liste;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;


/**
 * Spring Data JPA repository for the Liste entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ListeRepository extends JpaRepository<Liste, Long> {

}
