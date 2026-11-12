package com.m2i.pti.repository;

import com.m2i.pti.domain.GroupeProfilActeur;
import com.m2i.pti.domain.ProfilActeur;
import com.m2i.pti.domain.enumeration.TypeProfilActeur;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;

import java.util.List;
import java.util.Optional;


/**
 * Spring Data JPA repository for the GroupeProfilActeur entity.
 */
@SuppressWarnings("unused")
@Repository
public interface GroupeProfilActeurRepository extends JpaRepository<GroupeProfilActeur, Long> {
    List<GroupeProfilActeur> findAllByType(TypeProfilActeur type);

    Optional<GroupeProfilActeur> findByTypeAndActiveTrue(TypeProfilActeur type);

    @Query(value = "select nom from groupe_profil_acteur where id=CAST(CAST (:gpaId as TEXT) as BIGINT ) ",nativeQuery = true)
    String getNomGroupeProfilActeur(@Param("gpaId") Long gpaId);

}
