package com.m2i.pti.repository;

import com.m2i.pti.domain.Utilitaire;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;


/**
 * Spring Data JPA repository for the Utilitaire entity.
 */
@SuppressWarnings("unused")
@Repository
public interface UtilitaireRepository extends JpaRepository<Utilitaire, Long> {

}
