/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.repository;

import com.m2i.pti.domain.BackupData;

import com.m2i.pti.service.dto.BackupDataDTO;

import java.time.ZonedDateTime;
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
public interface BackupDataRepository extends JpaRepository<BackupData, Long> {
   @Query(name = "BackupData.findAllBackupByDate", nativeQuery = true)
    List<BackupDataDTO> findAllBackupSpeed(@Param("dateLastDeb") ZonedDateTime dateLastDeb,@Param("dateLastFin") ZonedDateTime dateLastFin);
}
