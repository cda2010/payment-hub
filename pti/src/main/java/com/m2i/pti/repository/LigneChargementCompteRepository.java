package com.m2i.pti.repository;

import com.m2i.pti.domain.LigneChargementCompte;
import com.m2i.pti.domain.enumeration.StatutLigneChargement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * Spring Data JPA repository for the LigneChargementCompte entity.
 */
@SuppressWarnings("unused")
@Repository
public interface LigneChargementCompteRepository extends JpaRepository<LigneChargementCompte, Long> {
    @Query("select c from LigneChargementCompte c where c.chargementCompte.id=?1 ")
    public List<LigneChargementCompte> queryDetail(Long id);
    @Query("select count(c) from  LigneChargementCompte c where c.chargementCompte.id=?1")
    public Integer countByChargementCompte(Long id);
    @Modifying
    @Query("delete from LigneChargementCompte l  where l.chargementCompte.id=?1")
    public void deleteAlls(Long id);
   @Query("select c from LigneChargementCompte c where c.chargementCompte.id=?1 and c.statutLigne=?2 ")
    public List<LigneChargementCompte> findByChargementCompteIdAndStatutLigneChargement(Long id, StatutLigneChargement statut);

    @Transactional
    @Query("select c from LigneChargementCompte c where c.chargementCompte.id=?1 ")
    public Page<LigneChargementCompte> queryDetailPage(Long id,Pageable pageable);
 
    
  
}
