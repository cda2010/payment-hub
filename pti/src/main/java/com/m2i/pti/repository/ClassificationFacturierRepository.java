package com.m2i.pti.repository;

import com.m2i.pti.domain.ClassificationFacturier;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;

import java.util.List;


/**
 * Spring Data JPA repository for the ClassificationFacturier entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ClassificationFacturierRepository extends JpaRepository<ClassificationFacturier, Long> {
    @Query("SELECT c FROM ClassificationFacturier c WHERE c.uniteOrganisation.id = ?1  and  " +
            "c.id not in ( select pere.id from ClassificationFacturier fils inner join fils.niveauSuperieur pere)  ")
    List<ClassificationFacturier> findClassificationFinalOfUo(Long UoId);

    @Query("SELECT c FROM ClassificationFacturier c WHERE c.uniteOrganisation.id = ?1 and activated=true")
    List<ClassificationFacturier> findClassificationOfUo(Long UoId);
}
