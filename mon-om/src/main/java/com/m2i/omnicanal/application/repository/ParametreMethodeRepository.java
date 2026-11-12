package com.m2i.omnicanal.application.repository;

import com.m2i.omnicanal.application.domain.ParametreMethod;
import java.util.List;

import org.springframework.data.jpa.repository.*;


/**
 * Spring Data JPA repository for the ParametreMethode entity.
 */
@SuppressWarnings("unused")
public interface ParametreMethodeRepository extends EntityRepository<ParametreMethod,Long> {
@Query("Select p from ParametreMethod p where p.methode.id=?1")
    public List<ParametreMethod> findByMethode(Long idmethode);

}
