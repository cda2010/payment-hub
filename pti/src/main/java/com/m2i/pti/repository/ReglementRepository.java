package com.m2i.pti.repository;

import com.m2i.pti.domain.LigneReglement;
import com.m2i.pti.domain.Reglement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;

import java.util.List;


/**
 * Spring Data JPA repository for the Reglement entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ReglementRepository extends JpaRepository<Reglement, Long> {
    @Query("select l from LigneReglement l where l.reglement.id=?1")
    Page<LigneReglement> getligneReglement(Long id, Pageable pageable);
    @Query("select r from Reglement r order by r.id desc")
    Page<Reglement> getAllReglement(Pageable pageable);
}
