package com.m2i.pti.repository;

import com.m2i.pti.domain.Compte;
import com.m2i.pti.domain.ReleveCompte;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;


/**
 * Spring Data JPA repository for the ReleveCompte entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ReleveCompteRepository extends JpaRepository<ReleveCompte, Long> {
    @Query(value = "select r from ReleveCompte r where r.compte.id=:compteId")
    List<ReleveCompte> findOptionalByCompteAndDateDebutAndDateFin(@Param("compteId") Long compteId);
}
