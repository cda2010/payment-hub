package com.m2i.pti.repository;

import com.m2i.pti.domain.GroupRepartition;
import com.m2i.pti.service.dto.GroupRepartitionDTO;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;

import java.util.List;


/**
 * Spring Data JPA repository for the GroupRepartition entity.
 */
@SuppressWarnings("unused")
@Repository
public interface GroupRepartitionRepository extends JpaRepository<GroupRepartition, Long> {
    @Query(value = "select distinct gr from GroupRepartition gr left join fetch gr.tableauRepartitionCommissions ")
    List<GroupRepartition> findAllWhithRelationships();

    @Query(name = "GroupRepartition.findOne", nativeQuery = true)
    GroupRepartitionDTO findOneOptimise(@Param("id") Long id);

}
