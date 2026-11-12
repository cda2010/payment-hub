package com.m2i.pti.repository;

import com.m2i.pti.domain.LigneFrais;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the LigneFrais entity.
 */
@SuppressWarnings("unused")
@Repository
public interface LigneFraisRepository extends JpaRepository<LigneFrais, Long> {
    LigneFrais findByTableauFraisIdAndMinimunLessThanEqualAndMaximunGreaterThanEqual(Long fraisId, Double montantMin,Double montantMax);
    Page<LigneFrais> findByTableauFraisIdOrderByMinimun(Long fraisId, Pageable pageable);
    Boolean existsByMaximunAndTableauFraisId(Double montant,Long fraisId);
    Boolean existsByMinimunAndTableauFraisId(Double montant,Long fraisId);

    List<LigneFrais> findByTableauFraisId(Long tableauFraisId);
    List<LigneFrais> findByTableauFraisIdOrderByMinimun(Long fraisId);
}
