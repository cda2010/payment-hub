/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.repository;

import com.m2i.pti.domain.FileExportParam;
import com.m2i.pti.domain.ParametreExport;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;

import org.springframework.data.domain.Page;


/**
 *
 * @author cda
 */
@SuppressWarnings("unused")
@Repository
public interface FileExportParamRepository extends JpaRepository<FileExportParam, Long>{
@Query("select c from FileExportParam c where c.fileExport.id=?1")
    public List<FileExportParam> findByExpotable(Long exportId);


    @Query(value = "select c from FileExportParam c where c.fileExport.id=?1")
    Page<FileExportParam> findFileExportparamByFileExportId(Long fileExportId, Pageable pageable);
    
}
