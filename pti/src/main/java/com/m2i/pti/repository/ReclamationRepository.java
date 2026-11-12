package com.m2i.pti.repository;

import com.m2i.pti.domain.Reclamation;

import com.m2i.pti.domain.UniteOrganisation;
import com.m2i.pti.domain.enumeration.EtatReclamation;
import com.m2i.pti.domain.enumeration.QualiteReclamation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data  repository for the Reclamation entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ReclamationRepository extends JpaRepository<Reclamation, Long> {
    Optional<Reclamation> findById(Long id);
    void deleteById(Long id);

    @Query( " select  r from  Reclamation r where " +
            "( :titre is null or r.typeReclamation.id= :titre )" +
            "and (r.etat= :etat or :etat is null)" +
            "and(:qualiteActeur is null or r.qualiteAuteur=:qualiteActeur )" +
            "and ( :telephoneAuteur is null or r.telephone like %:telephoneAuteur% ) order by r.id DESC")
    List<Reclamation> findByCriteria(@Param("titre")Long titre,
                              @Param("etat") EtatReclamation etat,
                              @Param("qualiteActeur") QualiteReclamation qualiteReclamation,
                              @Param("telephoneAuteur") String telephoneAuteur);
}
