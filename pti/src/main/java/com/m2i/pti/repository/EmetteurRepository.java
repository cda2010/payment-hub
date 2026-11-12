package com.m2i.pti.repository;

import com.m2i.pti.domain.Emetteur;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;

import java.util.List;


/**
 * Spring Data JPA repository for the Emetteur entity.
 */
@SuppressWarnings("unused")
@Repository
public interface EmetteurRepository extends JpaRepository<Emetteur, Long> {
    Emetteur findByPersonneId(Long personneId);
    Emetteur findTop1ByPersonneId(Long personneId);

    @Query(value = "select distinct personne_id from emetteur where id=:emetteurId",nativeQuery = true)
    Long getPersonneIdByEmetteurId(@Param("emetteurId") Long emetteurId);

    List<Emetteur> findAllByPersonneId(Long personneId);

}
