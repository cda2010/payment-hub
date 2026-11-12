package com.m2i.pti.repository;

import com.m2i.pti.domain.Bouton;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;


/**
 * Spring Data JPA repository for the Bouton entity.
 */
@SuppressWarnings("unused")
@Repository
public interface BoutonRepository extends JpaRepository<Bouton, Long> {

}
