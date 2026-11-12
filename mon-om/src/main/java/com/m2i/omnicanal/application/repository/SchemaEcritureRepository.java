package com.m2i.omnicanal.application.repository;



import com.m2i.omnicanal.model.SchemaEcriture;

import com.m2i.omnicanal.application.domain.SchemaEcrit;
import com.m2i.omnicanal.model.TypeService;
import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the SchemaEcriture entity.
 */
@SuppressWarnings("unused")
public interface SchemaEcritureRepository extends EntityRepository<SchemaEcrit,Long> {
 @Query("select s from  SchemaEcrit   s where s.typeService=?1")
    public List<SchemaEcriture> findByService(TypeService typeService);

}
