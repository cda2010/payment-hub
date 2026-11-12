package com.m2i.pti.repository;

import com.m2i.pti.domain.RecepteurAlarme;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;

import java.util.List;


/**
 * Spring Data JPA repository for the RecepteurAlarme entity.
 */
@SuppressWarnings("unused")
@Repository
public interface RecepteurAlarmeRepository extends JpaRepository<RecepteurAlarme, Long> {
    RecepteurAlarme findByEtiquette(String etiquette);
    @Query("select a from RecepteurAlarme a where ( " +
            " (:etiquette is null or :etiquette='' or a.etiquette like('%'||:etiquette||'%')) and " +
            "(:sms is null or :sms='' or a.sms like('%'||:sms||'%') )   )")
    List<RecepteurAlarme> findAlarmeByCriterial(@Param("etiquette") String etiquette, @Param("sms") String sms);
}
