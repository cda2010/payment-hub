package com.m2i.pti.repository;

import com.m2i.pti.batch.reader.LigneChargementTransDTO;
import com.m2i.pti.domain.ChargementTransfert;
import com.m2i.pti.domain.Client;
import com.m2i.pti.domain.LigneChargement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import org.springframework.data.repository.query.Param;


/**
 * Spring Data JPA repository for the ChargementTransfert entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ChargementTransfertRepository extends JpaRepository<ChargementTransfert, Long> {
    @Query("select count(c)   from ChargementTransfert c where c.hashFichier=?1")
    public Integer findByHashFichier(String hash);
    @Query("select count(c)  from ChargementTransfert c where c.nomFichier=?1  ")
    public Integer findByNomFichier(String nomFichier);
    @Query("select c  from ChargementTransfert c where c.reference is not null  order by reference desc , dateChargement desc")
    public List<ChargementTransfert> findAllOrderByDate();

   // public List<ChargementTransfert> findAllOrderByDateChargement();
    @Query("select c  from ChargementTransfert c where c.client=?1 and  c.statut=?2 order by reference desc , dateChargement desc")
    public List<ChargementTransfert> findByClient(Client c, String statut);



    List<ChargementTransfert> findAllByClientId(Long clientId);


    @Query("select c from ChargementTransfert c where c.client.id=?1 order by c.id desc")
    Page<ChargementTransfert> findAllByClientIdPage(Long clientId, Pageable pageable);
 @Query(name = "chargementTransfert.findByChargementId", nativeQuery = true)
      //@Query(value="SELECT l.id as idLigne, ?1 as codeoperation ,?2 as compteId FROM ligne_chargement_transfert l  where l.chargement_transfert_id= ?3 and statut_ligne!='OC' and compte_id is not null order by id", nativeQuery = true)
    List<LigneChargementTransDTO> findByChargementId(  @Param("codeoperation") Long codeoperation, @Param("compteId") Long compteId ,@Param("idChargement") Long idChargement);

}
