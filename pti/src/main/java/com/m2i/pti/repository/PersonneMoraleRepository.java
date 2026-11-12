package com.m2i.pti.repository;

import com.m2i.pti.domain.Personne;
import com.m2i.pti.domain.PersonneMorale;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;

import java.util.Optional;


/**
 * Spring Data JPA repository for the PersonneMorale entity.
 */
@SuppressWarnings("unused")
@Repository
public interface PersonneMoraleRepository extends JpaRepository<PersonneMorale, Long> {
    Optional<PersonneMorale> findTopByRaisonSociale(String raisonSociale);

}
