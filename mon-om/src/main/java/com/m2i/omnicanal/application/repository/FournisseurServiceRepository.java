package com.m2i.omnicanal.application.repository;

import com.m2i.omnicanal.application.domain.FournisseurFSE;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the FournisseurService entity.
 */
@SuppressWarnings("unused")
public interface FournisseurServiceRepository extends EntityRepository<FournisseurFSE,Long> {
@Query("select f from FournisseurFSE f where f.id<>?1")
    public List<FournisseurFSE> findButMinusFseEnCours(Long idFse);

    public FournisseurFSE findByCodeFse(String code);

}
