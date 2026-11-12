/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.repository;

import com.m2i.pti.domain.ParamBackUpData;
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
public interface ParamBackUpDataRepository extends JpaRepository<ParamBackUpData, Long> {

    @Query("select p from ParamBackUpData p where p.enCours=1 ")
    ParamBackUpData findEnCours();
}
