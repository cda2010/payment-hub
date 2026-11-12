package com.m2i.omnicanal.presentation.repository;



import com.m2i.omnicanal.model.FournisseurService;
import com.m2i.omnicanal.model.Relation;
import com.m2i.omnicanal.model.TypeService;
import com.m2i.omnicanal.presentation.domain.RelationFse;
import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the Relation entity.
 */
@SuppressWarnings("unused")
public interface RelationRepository extends EntityRepository<RelationFse,Long> {
@Query("select r.fsePartenaire from RelationFse r where r.fseSource=?1 and r.fseDestination=?2 and r.service=?3")
    public FournisseurService findPartenaireRelation(FournisseurService fseSource, FournisseurService fseDest,TypeService tservice);
@Query("select r from RelationFse r where r.fseSource=?1 and r.fseDestination=?2 and (?3 is null or r.service = ?3)")
    public Relation findRelation(FournisseurService fseSource, FournisseurService fseDest,TypeService tservice);
}
