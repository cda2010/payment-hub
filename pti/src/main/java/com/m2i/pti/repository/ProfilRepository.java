package com.m2i.pti.repository;

import com.m2i.pti.domain.Profil;
import com.m2i.pti.domain.TypeUniteOrganisation;
import com.m2i.pti.service.dto.ProfilDTO;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;

import java.util.List;
import java.util.Set;


/**
 * Spring Data JPA repository for the Profil entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ProfilRepository extends JpaRepository<Profil, Long> {
    @Query("select distinct profil from Profil profil left join fetch profil.roles")
    List<Profil> findAllWithEagerRelationships();

    @Query("select profil from Profil profil left join fetch profil.roles where profil.id = :id")
    Profil findOneWithEagerRelationships(@Param("id") Long id);

    List<Profil> findByTypeUO(TypeUniteOrganisation typeUniteOrganisation);

    @Query(name = "Profil.findByTuoId", nativeQuery = true)
    List<ProfilDTO> findByTuoId(@Param("typeUoId") Long typeUoId);

    @Query(name = "Profil.findAllProfil", nativeQuery = true)
    List<ProfilDTO> findAllProfil(@Param("taille") Long taille, @Param("page") Long page);

    @Query("select count(p.id) from Profil p")
    Integer countAllProfil();

    @Query(value = "select authority_name from profil_role where profils_id = CAST(CAST (:id AS TEXT) AS BIGINT ) ", nativeQuery = true)
    Set<String> findRoleByProfilId(@Param("id") Long  id);


}
