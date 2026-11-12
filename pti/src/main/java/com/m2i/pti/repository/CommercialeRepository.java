package com.m2i.pti.repository;

import com.m2i.pti.domain.Commerciale;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;


/**
 * Spring Data JPA repository for the Commerciale entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CommercialeRepository extends JpaRepository<Commerciale, Long> {

}
