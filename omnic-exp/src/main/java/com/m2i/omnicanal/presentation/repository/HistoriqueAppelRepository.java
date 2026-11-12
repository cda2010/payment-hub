/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.omnicanal.presentation.repository;



import com.m2i.omnicanal.presentation.domain.HistAppel;
import java.time.ZonedDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 *
 * @author cda
 */
public interface HistoriqueAppelRepository  extends EntityRepository<HistAppel,Long> {
@Query("select h from HistAppel h where h.dateRequest <= ?1")
    public List<HistAppel> findAllDateRequestBefore(ZonedDateTime minusDays);
    
}
