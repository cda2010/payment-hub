package com.m2i.pti.repository;

import com.m2i.pti.domain.TypeCompte;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;

import java.util.Optional;


/**
 * Spring Data JPA repository for the TypeCompte entity.
 */
@SuppressWarnings("unused")
@Repository
public interface TypeCompteRepository extends JpaRepository<TypeCompte, Long> {
    Optional<TypeCompte> findByCodeTypeCompte(String code);

    Optional<TypeCompte> findByLibelleTypeCompte(String libelle);

}
