package com.m2i.pti.repository;

import com.m2i.pti.domain.BilanTaxe;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * Spring Data  repository for the BilanTaxe entity.
 */
@SuppressWarnings("unused")
@Repository
public interface BilanTaxeRepository extends JpaRepository<BilanTaxe, Long> {

    List<BilanTaxe> findByEstPayeAndOrganisationId(Boolean estPaye,Long uoId);
 }
