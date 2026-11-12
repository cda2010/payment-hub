package com.m2i.pti.repository;

import com.m2i.pti.domain.Canal;
import com.m2i.pti.service.dto.CanalDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for the Canal entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CanalRepository extends JpaRepository<Canal, Long> {
    @Query("select distinct canal from Canal canal left join fetch canal.typeServices")
    List<Canal> findAllWithEagerRelationships();

    @Query("select canal from Canal canal left join fetch canal.typeServices where canal.id =:id")
    Canal findOneWithEagerRelationships(@Param("id") Long id);

    Canal findByLibelleCanal(String libelle);
    Canal findTopByCodeCanal(String codeCanal);
    Boolean existsByCodeCanal(String codeCanal);
    Boolean existsByLibelleCanal(String libelle);
    @Query(name = "Canal.findByCodeCanal", nativeQuery = true)
    CanalDTO findByCodeCanal(@Param("code") String code);

    @Query(value = "select c.libelle_canal from canal c where c.id=:id",nativeQuery = true)
    String getLibelleCanalById(@Param("id") Long id);



}
