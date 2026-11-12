/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.omnicanal.application.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.m2i.omnicanal.application.domain.HistAppel;
import com.m2i.omnicanal.application.service.dto.DataChartResponse;

/**
 *
 * @author cda
 */
public interface HistoriqueAppelRepository extends EntityRepository<HistAppel, Long> {

    @Query(value = "select * from historique_appel order by date_request desc,duree desc limit 10", nativeQuery = true)
    public List<HistAppel> findTop10();

    @Query(value = "select * from historique_appel order by duree desc,id desc", nativeQuery = true)
    public List<HistAppel> getAllHistoriqueAppel();

    @Query(value = "select * from historique_appel where date(date_request) = curdate() order by date_request desc,duree desc", nativeQuery = true)
    public List<HistAppel> getTodayHistoriqueAppel();

    @Query(name = "HistAppel.findNombrePerFonction", nativeQuery = true)
    public List<DataChartResponse> findNombrePerFonction();
    
    @Query(name = "HistAppel.findNombrePerCodeError", nativeQuery = true)
    public List<DataChartResponse> findNombrePerCodeError();
//Nombre d'appel par api et en interval de temps
    @Query(name = "HistAppel.findNombrePerCodeError", nativeQuery = true)
    public List<DataChartResponse> findNombreAppelGroupByTimeAndApi();


    
    
}
