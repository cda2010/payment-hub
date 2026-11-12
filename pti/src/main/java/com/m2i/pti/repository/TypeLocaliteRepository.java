package com.m2i.pti.repository;

import com.m2i.pti.domain.TypeLocalite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;


/**
 * Spring Data JPA repository for the TypeLocalite entity.
 */
@SuppressWarnings("unused")
@Repository
public interface TypeLocaliteRepository extends JpaRepository<TypeLocalite, Long> {

    @Query("select t from TypeLocalite t order by t.id asc")
    Page<TypeLocalite> findAll(Pageable pageable);
}
