package com.m2i.pti.repository;

import com.m2i.pti.domain.Produit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;
import  com.m2i.pti.service.dto.ProduitDTO;
import java.util.List;
import org.springframework.data.repository.query.Param;


/**
 * Spring Data JPA repository for the Produit entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ProduitRepository extends JpaRepository<Produit, Long> {

    @Query("select new com.m2i.pti.service.dto.ProduitDTO(p.id, p.nomProduit, p.numeroOrdre, p.actif, p.transactionnel, p.aUnMontant, p.montant) from Produit p order by p.nomProduit")
    List<ProduitDTO> findLabels();

    @Query("select new com.m2i.pti.service.dto.ProduitDTO(p.id, p.nomProduit, p.numeroOrdre, p.actif, p.transactionnel, p.aUnMontant, p.montant, f.id, f.nomFacturier) from Produit p left join p.facturier f order by p.nomProduit")
    List<ProduitDTO> findWithFacturier();
    List<Produit> findProduitByFacturierIdOrderByNumeroOrdreAsc(Long idFacturier);

    Page<Produit> findPageProduitByFacturierIdOrderByNumeroOrdreAsc(Long idFacturier, Pageable pageable);
    
        @Query(name = "Produit.findByProduitByFacturier", nativeQuery = true)
    List<ProduitDTO> findAllProduitByFacturierIdOrderByNumeroOrdreAsc(  @Param("idFacturier") Long idFacturier  );
    
    
@Query("select p from Produit p where p.id=?1")
    public Produit findByProduitId(Long produitId);
}
