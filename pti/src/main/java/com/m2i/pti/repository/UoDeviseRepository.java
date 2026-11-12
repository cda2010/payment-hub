package com.m2i.pti.repository;

import com.m2i.pti.domain.UoDevise;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;

import java.util.List;


/**
 * Spring Data JPA repository for the UoDevise entity.
 */
@SuppressWarnings("unused")
@Repository
public interface UoDeviseRepository extends JpaRepository<UoDevise, Long> {

    UoDevise findByUniteOrganisationIdAndDeviseId(Long uoId, Long deviseId);

    List<UoDevise> findByUniteOrganisationId(Long uoId);

    UoDevise findByUniteOrganisationIdAndDefaultDevise(Long uoId, Boolean defaultDevise);
}
