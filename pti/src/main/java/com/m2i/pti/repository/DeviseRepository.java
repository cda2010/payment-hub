package com.m2i.pti.repository;

import com.m2i.pti.domain.Canal;
import com.m2i.pti.domain.Devise;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;

import java.util.Optional;


/**
 * Spring Data JPA repository for the Devise entity.
 */
@SuppressWarnings("unused")
@Repository
public interface DeviseRepository extends JpaRepository<Devise, Long> {
    Optional<Devise> findByLibelle(String libelle);

    Optional<Devise> findByCode(String code);

    Optional<Devise> findBySymbole(String code);

}
