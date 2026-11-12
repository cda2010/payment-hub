package com.m2i.pti.repository;

import com.m2i.pti.domain.ClassificationFacturier;
import com.m2i.pti.domain.Facture;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;

import java.time.ZonedDateTime;
import java.util.List;

import com.m2i.pti.service.dto.FactureDTO;
import java.math.BigInteger;


/**
 * Spring Data JPA repository for the Facture entity.
 */
@SuppressWarnings("unused")
@Repository
public interface FactureRepository extends JpaRepository<Facture, Long> {
        @Query("SELECT f FROM Facture f inner join f.produit where f.produit.facturier.id=?1")
        List<Facture> findFacturesOfProduit(Long idProduit);
 @Query("SELECT f FROM Facture f where f.referenceFacature=?1 and referencePaiement is null")
    public List<Facture> findByReferenceFacature(String reference);
 @Query("SELECT f FROM Facture f where (" +
         "(:refFacture is null or :refFacture='' or UPPER(f.referenceFacature) like UPPER('%'||:refFacture||'%'))" +
         "and (:refPaiement is null or :refPaiement='' or UPPER(f.referencePaiement) like UPPER('%'||:refPaiement||'%'))" +
         "and (:produit is null or f.produit.id=:produit)" +
         "and(:datePaiement is null or :datePaiement='' or f.datePaiement=:datePaiement  )) order by f.id desc")
    List<Facture> findFactureByCredential(@Param("refFacture") String refFacture, @Param("refPaiement") String refPaiement, @Param("produit") Long produit, @Param("datePaiement") String datePaiement);
@Query("SELECT f FROM Facture f where f.produit.id=?1")
    public   List<Facture>  findByProduitId(Long id);
    
    
    @Query("SELECT f FROM Facture f where  f.referencePaiement=?1")
    public Facture findByReferencePaiement(String reference);

    @Query(name = "Facture.findFactureOptimise",nativeQuery = true)
    List<FactureDTO> findFactureByCredentialOptimise(@Param("produit") Long produit, @Param("refFacture") String refFacture, @Param("refPaiement") String refPaiement, @Param("datePaiement") String datePaiement);

    @Query(name = "Facture.findFactureOfProduitOptimise",nativeQuery = true)
    List<FactureDTO> findFactureOfProduitOptimise(@Param("produit") Long produit);

    @Query(name = "Facture.findFactureByProduitIdOptimise",nativeQuery = true)
    List<FactureDTO> findFactureByProduitIdOptimise(@Param("produit") Long produit);
    
    
    @Query(name = "Facture.findFactureByFacturierIdAndPersonne",nativeQuery = true)
    List<FactureDTO> findFactureByFacturierAndPersonne(@Param("persId") Long persId,@Param("facturierId") Long facturierId, @Param("taille") Long taille,@Param("page") Long page);


}





