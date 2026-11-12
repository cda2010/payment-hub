package com.m2i.pti.repository;

import com.m2i.pti.domain.ProduitCanal;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;

import java.util.List;


/**
 * Spring Data JPA repository for the ProduitCanal entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ProduitCanalRepository extends JpaRepository<ProduitCanal, Long> {

    List<ProduitCanal> findByProduitId(Long produitId);

    @Query("select p.canal.id from ProduitCanal p where p.produit.id=?1")
    List<Long> findIdByProduitId(Long produitId);

    @Query("select p from ProduitCanal p where p.canal.id=?1 and p.produit.id=?2")
    ProduitCanal findByCanalIdAndProduitId(Long canalId,Long produitId);
}
