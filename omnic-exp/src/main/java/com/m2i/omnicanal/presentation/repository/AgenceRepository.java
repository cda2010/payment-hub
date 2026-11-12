package com.m2i.omnicanal.presentation.repository;

import com.m2i.omnicanal.model.Agence;
import com.m2i.omnicanal.model.FournisseurService;
import com.m2i.omnicanal.presentation.domain.AgenceFse;
import com.m2i.omnicanal.presentation.domain.FournisseurFSE;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the Agence entity.
 */
@SuppressWarnings("unused")
public interface AgenceRepository extends EntityRepository<AgenceFse,Long> {
@Query("select a from Agence a where a.codeAgence=?1 and a.fournisseurService.id=?2")
    public AgenceFse findByCodeAgenceAndFournisseurServiceId(String codeAgence, Long fseId);
@Query("select a from Agence a where  a.fournisseurService=?1 and a.codeAgence=a.fournisseurService.codeAgenceSiege")
    public Agence findAgenceSiege(FournisseurService fournisseur);

}
