package com.m2i.pti.repository;

import com.m2i.pti.domain.DetailHistorique;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;

import java.util.List;


/**
 * Spring Data JPA repository for the DetailHistorique entity.
 */
@SuppressWarnings("unused")
@Repository
public interface DetailHistoriqueRepository extends JpaRepository<DetailHistorique, Long> {

    Page<DetailHistorique> findAllByHistoriqueId(Long historique_id, Pageable pageable);

    @Query("select d from DetailHistorique d where d.historique.id=?1")
    List<DetailHistorique> getListHistorique(Long historiqueId);

    @Query("select d from DetailHistorique d where d.historique.id=:historiqueId and (:telephone is null or :telephone='' or d.telephone=:telephone)")
    Page<DetailHistorique> findByHistoriqueIdWithCriteria(@Param("historiqueId") Long historiqueId,@Param("telephone") String telephone,Pageable pageable);
}
