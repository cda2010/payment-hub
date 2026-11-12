package com.m2i.pti.partenaire.repository;

import com.m2i.pti.partenaire.domain.FactureOnea;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;


/**
 * Spring Data JPA repository for the FactureOnea entity.
 */
@SuppressWarnings("unused")
@Repository
public interface FactureOneaRepository extends JpaRepository<FactureOnea, Long> {

}
