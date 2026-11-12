package com.m2i.pti.repository;

import com.m2i.pti.domain.FournisseurService;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;


/**
 * Spring Data JPA repository for the FournisseurService entity.
 */
@SuppressWarnings("unused")
@Repository
public interface FournisseurServiceRepository extends JpaRepository<FournisseurService, Long> {
    FournisseurService findByCodeFse(String codeFse);
}
