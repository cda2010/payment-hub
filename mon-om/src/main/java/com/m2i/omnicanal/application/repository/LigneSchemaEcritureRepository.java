package com.m2i.omnicanal.application.repository;



import com.m2i.omnicanal.application.domain.LigneSchemaEcrit;
import com.m2i.omnicanal.application.domain.MethodeFonction;
import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the LigneSchemaEcriture entity.
 */
@SuppressWarnings("unused")
public interface LigneSchemaEcritureRepository extends EntityRepository<LigneSchemaEcrit,Long> {
@Query("Select m from LigneSchemaEcriture m where m.schemaEcriture.id=?1")

    public List<LigneSchemaEcrit> findBySch(Long idschema);

}
