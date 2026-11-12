package com.m2i.omnicanal.presentation.repository;



import com.m2i.omnicanal.model.LigneSchemaEcriture;
import com.m2i.omnicanal.presentation.domain.LigneSchemaEcrit;
import com.m2i.omnicanal.presentation.domain.MethodeFonction;
import org.springframework.data.jpa.repository.*;

import java.util.List;
import java.util.Set;

/**
 * Spring Data JPA repository for the LigneSchemaEcriture entity.
 */
@SuppressWarnings("unused")
public interface LigneSchemaEcritureRepository extends EntityRepository<LigneSchemaEcrit,Long> {
@Query("Select m from LigneSchemaEcriture m where m.schemaEcriture.id=?1")
    public List<LigneSchemaEcrit> findBySch(Long idschema);
    @Query("Select m from LigneSchemaEcriture m where m.schemaEcriture.id=?1")
    public Set<LigneSchemaEcriture> findLigneBySch(Long idschema);

}
