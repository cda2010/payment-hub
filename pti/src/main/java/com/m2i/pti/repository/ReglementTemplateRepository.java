package com.m2i.pti.repository;

import com.m2i.pti.domain.Reglement;
import com.m2i.pti.domain.ReglementTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;



/**
 * Spring Data JPA repository for the Reglement entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ReglementTemplateRepository extends JpaRepository<ReglementTemplate, Long> {
    @Query("select r from ReglementTemplate r order by r.id desc")
    Page<Reglement> getAllReglement(Pageable pageable);
}
