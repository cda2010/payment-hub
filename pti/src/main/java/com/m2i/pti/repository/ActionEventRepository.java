/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.repository;

import com.m2i.pti.domain.ActionEvent;
import com.m2i.pti.domain.BrancheActivite;
import com.m2i.pti.domain.enumeration.ActionEnum;
import com.m2i.pti.service.dto.ActionEventDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;

/**
 *
 * @author cda
 */
@SuppressWarnings("unused")
@Repository
public interface ActionEventRepository extends JpaRepository<ActionEvent, Long> {

    //@Query(value = "select a from action_event a where a.action=:actionE and a.date_action between CAST(CAST(:dateDebut as text) as timestamp) and CAST(CAST(:dateFin as text) as timestamp)  ",nativeQuery = true)
    @Query(name = "ActionEvent.findAllByActionAndDateAction", nativeQuery = true)
    List<ActionEventDTO> findAllByActionAndDateAction(@Param("actionE") String actionE, @Param("dateDebut") ZonedDateTime dateDebut,@Param("dateFin") ZonedDateTime dateFin);

   }
