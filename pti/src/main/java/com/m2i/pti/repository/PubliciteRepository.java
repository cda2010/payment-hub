/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.repository;

import com.m2i.pti.domain.Canal;
import com.m2i.pti.domain.Publicite;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 *
 * @author cda
 */
@SuppressWarnings("unused")
@Repository
public interface PubliciteRepository extends JpaRepository<Publicite, Long> {

    @Query("select  p from Publicite p where p.pays.codePays=?1")
    List<Publicite> findByCodeIndicatif(String codeIndicatif);
    
    
     @Query("SELECT p FROM Publicite p " +
           "WHERE coalesce(p.actif,0) = 1 " +
           "AND p.dateDebut <= ?1 " +
           "AND p.dateFin >= ?1 and p.pays.codePays=?2")
    List<Publicite> findByCodeIndicatifAndActif( LocalDate now , String codeIndicatif);
}
