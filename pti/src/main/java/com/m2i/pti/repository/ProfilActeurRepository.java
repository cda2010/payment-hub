package com.m2i.pti.repository;

import com.m2i.pti.domain.GroupeProfilActeur;
import com.m2i.pti.domain.ProfilActeur;
import com.m2i.pti.domain.enumeration.TypeProfilActeur;
import com.m2i.pti.service.dto.ProfilActeurDTO;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the ProfilActeur entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ProfilActeurRepository extends JpaRepository<ProfilActeur, Long> {
    @Query("select distinct profil_acteur from ProfilActeur profil_acteur left join fetch profil_acteur.tableauFrais left join fetch profil_acteur.tableauCumuls left join fetch profil_acteur.tableauRepartitionCommissions")
    List<ProfilActeur> findAllWithEagerRelationships();

    @Query("select profil_acteur from ProfilActeur profil_acteur left join fetch profil_acteur.tableauFrais left join fetch profil_acteur.tableauCumuls left join fetch profil_acteur.tableauRepartitionCommissions left join fetch profil_acteur.horaireAuthentifications left join fetch profil_acteur.groupRepartitions where profil_acteur.id = :id")
    ProfilActeur findOneWithEagerRelationships(@Param("id") Long id);

    Optional<ProfilActeur> findByTypeAndActiveTrue(TypeProfilActeur type);
    @Query("select profil_acteur from ProfilActeur profil_acteur where   profil_acteur.type=?1")
    List<ProfilActeur> findByTypeAndActiveAll(TypeProfilActeur type);

    @Query("select distinct profil_acteur from ProfilActeur profil_acteur left join fetch profil_acteur.tableauFrais left join fetch profil_acteur.tableauCumuls left join fetch profil_acteur.tableauRepartitionCommissions left join fetch profil_acteur.horaireAuthentifications left join fetch profil_acteur.groupRepartitions where profil_acteur.groupe.id = :idGroupe")
    List<ProfilActeur> findAllByGroupeWithEagerRelationships(@Param("idGroupe") Long idGroupe);
    @Query("select distinct profil_acteur from ProfilActeur profil_acteur where profil_acteur.groupe.id = :idGroupe")
    List<ProfilActeur> findAllByGroupeWithEagerRelationshipsNew(@Param("idGroupe") Long idGroupe);
    @Query(value = "select  profil_acteur from ProfilActeur profil_acteur where profil_acteur.groupe.id = :idGroupe")
    List<ProfilActeur> findAllProfilsByGroupe(@Param("idGroupe") Long idGroupe);

    @Query(value = "select nom from profil_acteur where id=CAST(CAST (:pActeurId as TEXT) as BIGINT ) ",nativeQuery = true)
    String getNomProfilActeur(@Param("pActeurId") Long pActeurId);

}
