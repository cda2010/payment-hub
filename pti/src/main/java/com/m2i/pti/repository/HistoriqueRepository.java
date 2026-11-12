package com.m2i.pti.repository;

import com.m2i.pti.domain.Historique;
import com.m2i.pti.domain.enumeration.HistoryType;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;

import java.time.LocalDate;
import java.time.ZonedDateTime;


/**
 * Spring Data JPA repository for the Historique entity.
 */
@SuppressWarnings("unused")
@Repository
public interface HistoriqueRepository extends JpaRepository<Historique, Long> {
  @Query("select COUNT(h) from Historique h where h.etiquette=?1 and DATE(h.dateHistorique)=?2")
    Long findCount(HistoryType typeHistorique, LocalDate dateEnregistrement);
    @Query("select distinct h from Historique h where h.etiquette=?1 and DATE(h.dateHistorique)=?2")
    Historique findByCriteria(HistoryType typeHistorique, LocalDate dateEnregistrement);
}
