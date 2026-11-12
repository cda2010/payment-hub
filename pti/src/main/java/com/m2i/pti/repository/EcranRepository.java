package com.m2i.pti.repository;

import com.m2i.pti.domain.Ecran;
import com.m2i.pti.service.dto.EcranListeDTO;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;

import java.util.List;


/**
 * Spring Data JPA repository for the Ecran entity.
 */
@SuppressWarnings("unused")
@Repository
public interface EcranRepository extends JpaRepository<Ecran, Long> {
    
    List<Ecran> findEcransByProduitIdOrderByNumeroOrdreAsc(Long produitID);

    @Query("select new com.m2i.pti.service.dto.EcranListeDTO(e.id, e.numeroOrdre) from Ecran e where e.produit.id = :id order by e.numeroOrdre")
    List<EcranListeDTO> findListeByProduit(@Param("id") Long id);
}
