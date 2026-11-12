/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.repository;

import com.m2i.pti.domain.BackupDataFields;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 *
 * @author cda
 */
@SuppressWarnings("unused")
@Repository
public interface BackupDataFieldsRepository extends JpaRepository<BackupDataFields, Long> {

    @Query("select b from BackupDataFields b where b.backupData.id=?1 ")
    public List<BackupDataFields> findByBackupData(Long id);

}
