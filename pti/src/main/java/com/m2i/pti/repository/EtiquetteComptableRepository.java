package com.m2i.pti.repository;

import com.m2i.pti.domain.EtiquetteComptable;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;


/**
 * Spring Data JPA repository for the EtiquetteComptable entity.
 */
@SuppressWarnings("unused")
@Repository
public interface EtiquetteComptableRepository extends JpaRepository<EtiquetteComptable, Long> {

}
