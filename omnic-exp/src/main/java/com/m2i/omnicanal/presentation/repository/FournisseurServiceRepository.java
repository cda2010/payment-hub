package com.m2i.omnicanal.presentation.repository;

import com.m2i.omnicanal.model.FournisseurService;
import com.m2i.omnicanal.presentation.domain.FournisseurFSE;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the FournisseurService entity.
 */
@SuppressWarnings("unused")
public interface FournisseurServiceRepository extends EntityRepository<FournisseurFSE,Long> {

    public FournisseurFSE findByCodeFse(String fseSource);
@Query ("select f.fseCompense from FournisseurFSE f where f=?1 ")
    public FournisseurFSE findByFseCompense(FournisseurService FseDest);

}
