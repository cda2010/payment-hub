package com.m2i.omnicanal.application.repository;

import com.m2i.omnicanal.application.domain.ParametreSchem;
import java.util.List;

import org.springframework.data.jpa.repository.*;


/**
 * Spring Data JPA repository for the ParametreMethode entity.
 */
@SuppressWarnings("unused")
public interface ParametreSchemaRepository extends EntityRepository<ParametreSchem,Long> {
@Query("Select p from ParametreSchem p where p.ligneSchemaEcriture.id=?1")
    public List<ParametreSchem> findByLigne(Long ligne);

}
