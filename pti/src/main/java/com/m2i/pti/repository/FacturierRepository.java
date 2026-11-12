package com.m2i.pti.repository;

import com.m2i.pti.domain.Facturier;
import com.m2i.pti.domain.UniteOrganisation;
import com.m2i.pti.domain.enumeration.SecteurActivite;
import com.m2i.pti.external.domain.ExFacturier;
import com.m2i.pti.service.dto.FacturierDTO;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Spring Data JPA repository for the Facturier entity.
 */
@SuppressWarnings("unused")
@Repository
public interface FacturierRepository extends JpaRepository<Facturier, Long> {

    List<Facturier> findAllByClassificationId(Long classificationId);

    @Query(value = "select  f from Facturier  f where f.classification.id=:classificationId and f.secteurActivite!='TELEPHONIE' and f.activated=true")
    List<Facturier> findAllByClassificationAndDiscarteTelephonie(@Param("classificationId") Long classificationId);

    @Query(value = "select  f from Facturier  f where f.classification.id=:classificationId and f.secteurActivite!='TELEPHONIE' and f.activated=true and EXISTS  (SELECT  p.facturier from ProduitCanal pc, Produit p  where pc.canal.id=:canalId and p.id= pc.produit.id and  p.facturier=f) ")
    List<Facturier> findAllByClassificationAndDiscarteTelephonie(@Param("classificationId") Long classificationId, @Param("canalId") Long canalId);

    @Query("select f from Facturier f where f.possesseur=?1 and f.secteurActivite=?2 and f.activated=true")
    public List<Facturier> findByPossesseurAndSecteurActivite(UniteOrganisation uo, SecteurActivite secteurActivite);

    @Query("select f from Facturier f where ("
            + "(:nomFacturier is null or :nomFacturier='' or f.nomFacturier like UPPER('%'||:nomFacturier||'%') ) and "
            + "(:codeFacturier is null or :codeFacturier='' or f.codeFacturier like UPPER('%'||:codeFacturier||'%')) and "
            + "(:classification_id is null  or f.classification.id=:classification_id) and "
            + "(:typeFacturier is null or :typeFacturier = :typeFacturier) and "
            + "(:possesseur_id is null or f.possesseur.id=:possesseur_id) ) ")
    List<Facturier> findFacturierByCriteria(@Param("nomFacturier") String nomFacturier, @Param("codeFacturier") String codeFacturier,
            @Param("classification_id") Long classification_id, @Param("typeFacturier") Long typeFacturier,
            @Param("possesseur_id") Long possesseur_id);

    @Query("select f from Facturier f where f.uniteOrganisation.codeUO=?1 and f.activated=true")
    public Facturier findByCodeUo(String codeUo);

    @Query("select f from Facturier f where f.uniteOrganisation.codeUO=?1 and f.activated=true")
    public List<Facturier> findListeFacturierByCodeUo(String codeUo);

    @Query("select f from Facturier f where  f.activated=true")
    public List<Facturier> findListeOfAllFacturier();

    @Query(value = "select f from Facturier f where f.possesseur.codeUO=?1 and f.activated=true")
    public List<Facturier> findListeFacturierByPosseseur(String codeUo);

    @Query(name = "Facturier.findByCodePosseurAndSecteur", nativeQuery = true)
    public List<ExFacturier> findListeFacturierByPossesseur(@Param("codeUo") String codeUo, @Param("secteur") List<String> secteur);

    @Query("select f from Facturier f where f.codeFacturier=?1")
    public Facturier findByCodeFacturier(String codeFacturier);

    @Query(name = "Facturier.findAllFacturierOptimiser2", nativeQuery = true)
    public List<FacturierDTO> findAllFacturierOptimiser(@Param("taille") Long taille, @Param("page") Long page);

    @Query("SELECT count(f) from Facturier f")
    Integer countAllFacturier();

    @Query(name = "Facturier.findByCodePosseuAndSecteurUo", nativeQuery = true)
    public ExFacturier findListeFacturierByPossesseurUo(@Param("codeUo") String codeUo, @Param("secteur") List<String> secteur);

}
