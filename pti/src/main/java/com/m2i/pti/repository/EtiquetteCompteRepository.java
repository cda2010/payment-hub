package com.m2i.pti.repository;

import com.m2i.pti.domain.EtiquetteCompte;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;

import java.util.List;


/**
 * Spring Data JPA repository for the EtiquetteCompte entity.
 */
@SuppressWarnings("unused")
@Repository
public interface EtiquetteCompteRepository extends JpaRepository<EtiquetteCompte, Long> {
    List<EtiquetteCompte> findByObligatoire(Boolean obligatoire);
    EtiquetteCompte findTop1ByCode(String code);
    @Query("select e.libelle from EtiquetteCompte  e where e.code=:etiqu")
    String finByEtiquette(@Param("etiqu") String etiqu);
    @Query("select e from EtiquetteCompte  e where e.code like '%PTI%' ")
    List<EtiquetteCompte> finByEtiquettePTI();
    @Query("select e from EtiquetteCompte e where e.obligatoire=true ")
    List<EtiquetteCompte> findByObligatoireForOtherCompte();

}
