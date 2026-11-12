package com.m2i.pti.repository;

import com.m2i.pti.domain.LigneChargementTransfert;
import com.m2i.pti.domain.enumeration.StatutLigneChargement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;


/**
 * Spring Data JPA repository for the LigneChargementTransfert entity.
 */
@SuppressWarnings("unused")
@Repository
public interface LigneChargementTransfertRepository extends JpaRepository<LigneChargementTransfert, Long> {
    @Query("select c from LigneChargementTransfert c where c.chargementTransfert.id=?1 ")
    public List<LigneChargementTransfert> queryDetail(Long id);
    @Query("select count(c) from  LigneChargementTransfert c where c.chargementTransfert.id=?1")
    public Integer countByChargementTransfert(Long id);
    @Modifying
    @Query("delete from LigneChargementTransfert l  where l.chargementTransfert.id=?1")
    public void deleteAlls(Long id);
    @Query("select c from LigneChargementTransfert c where c.chargementTransfert.id=?1 order by c.id desc")
    public List<LigneChargementTransfert> findByChargementTransfertIdAndStatutLigneChargement(Long id);

    @Transactional
    @Query("select c from LigneChargementTransfert c where c.chargementTransfert.id=?1 order by c.id desc ")
    public Page<LigneChargementTransfert> queryDetailPage(Long id, Pageable pageable);

    @Query("select c from LigneChargementTransfert c where c.chargementTransfert.id=?1 and c.statutLigne=?2 order by c.id desc ")
    public Page<LigneChargementTransfert> queryDetailPageByStatut(Long id, StatutLigneChargement statut, Pageable pageable);

    @Query("select c from LigneChargementTransfert c where c.chargementTransfert.id=?1 and c.statutLigne=?2 order by c.id desc ")
    public List<LigneChargementTransfert> queryDetailListByStatut(Long id, StatutLigneChargement statut);

    @Query("select c from LigneChargementTransfert c where c.chargementTransfert.id=?1 and (c.statutLigne=?2 or c.statutLigne=?3) order by c.id desc ")
    public List<LigneChargementTransfert> queryDetailListByStatut1(Long id, StatutLigneChargement statut, StatutLigneChargement statut1);


    @Query("select c from LigneChargementTransfert c where c.chargementTransfert.id=?1 and (c.statutLigne=?2 or c.statutLigne=?3) order by c.id desc ")
    public Page<LigneChargementTransfert> queryDetailPageByStatut1(Long id, StatutLigneChargement statut, StatutLigneChargement statut1,Pageable pageable);

    
     @Query("select c from LigneChargementTransfert c where c.chargementTransfert.id=?1 order by c.id desc")
    public Page<LigneChargementTransfert> findByChargementTransfertIdAndStatutLigneChargement(Pageable page, Long id);
    
    
    
      @Query("select count(c) from LigneChargementTransfert c where c.chargementTransfert.id=?1 and c.statutLigne=?2 ")
    public Integer findCompteByChargementTransfertIdAndStatutLigneChargement(Long idChargement, StatutLigneChargement valueOf);
     @Query("select sum(c.montant) from LigneChargementTransfert c where c.chargementTransfert.id=?1 and c.statutLigne=?2 ")
    public Double findSumByChargementTransfertIdAndStatutLigneChargement(Long idChargement, StatutLigneChargement valueOf);
}
