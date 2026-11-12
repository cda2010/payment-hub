package com.m2i.pti.repository;

import com.m2i.pti.domain.Ville;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;

import java.util.List;
import java.util.Set;


/**
 * Spring Data JPA repository for the Ville entity.
 */
@SuppressWarnings("unused")
@Repository
public interface VilleRepository extends JpaRepository<Ville, Long> {
    List<Ville> findByPaysId(Long paysId);

    @Query(value="select v.nom from ville v where v.id=CAST(CAST (:villeId AS TEXT) AS BIGINT )",nativeQuery = true )
    Set<String> getNomVille(@Param("villeId") Long villeId);
}
