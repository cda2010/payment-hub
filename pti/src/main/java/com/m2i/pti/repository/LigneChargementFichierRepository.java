package com.m2i.pti.repository;

import com.m2i.pti.domain.LigneChargementCompte;
import com.m2i.pti.domain.LigneChargementFichier;
import com.m2i.pti.domain.LigneChargementTransfert;
import com.m2i.pti.domain.enumeration.StatutLigneChargement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LigneChargementFichierRepository extends JpaRepository<LigneChargementFichier, Long> {
    @Query("select c from LigneChargementFichier c where c.chargementFichier.id=?1 and c.statutLigne=?2 order by c.id desc ")
    public Page<LigneChargementFichier> queryDetailPageByStatut(Long id, StatutLigneChargement statut, Pageable pageable);

    @Query("select c from LigneChargementFichier c where c.chargementFichier.id=?1 and (c.statutLigne=?2 or c.statutLigne=?3) order by c.id desc ")
    public Page<LigneChargementFichier> queryDetailPageByStatut1(Long id, StatutLigneChargement statut, StatutLigneChargement statut1,Pageable pageable);

    @Query("select c from LigneChargementFichier c where c.chargementFichier.id=?1 and c.statutLigne=?2 ")
    public List<LigneChargementFichier> findByChargementFichierIdAndStatutLigneChargement(Long id, StatutLigneChargement statut);

    @Query("select c from LigneChargementFichier c where c.chargementFichier.id=?1 and c.statutLigne=?2 order by c.id desc ")
    public List<LigneChargementFichier> queryDetailListByStatut(Long id, StatutLigneChargement statut);

    @Query("select c from LigneChargementFichier c where c.chargementFichier.id=?1 and (c.statutLigne=?2 or c.statutLigne=?3) order by c.id desc ")
    public List<LigneChargementFichier> queryDetailListByStatut1(Long id, StatutLigneChargement statut, StatutLigneChargement statut1);

    @Query("select c from LigneChargementFichier c where c.chargementFichier.uniteOrganisation.id=?1 order by c.id desc ")
    public Page<LigneChargementFichier> queryDetailPageByOrganisation(Long id, Pageable pageable);

}
