package com.m2i.pti.repository;

import com.m2i.pti.domain.TauxChange;
import com.m2i.pti.domain.enumeration.TypeChange;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;

import java.time.LocalDate;


/**
 * Spring Data JPA repository for the TauxChange entity.
 */
@SuppressWarnings("unused")
@Repository
public interface TauxChangeRepository extends JpaRepository<TauxChange, Long> {
    TauxChange findTopByDeviseDepartIdAndDeviseArriveeIdAndDateTauxAndTypeChange(Long deviseDepartId, Long deviseArriveeId, LocalDate dateTaux,TypeChange typeChange);
}
