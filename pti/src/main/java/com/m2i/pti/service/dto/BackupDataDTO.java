/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.service.dto;

import java.io.Serializable;
import java.time.Instant;

/**
 *
 * @author kadsuke
 */

public class BackupDataDTO implements Serializable {

    private Long id;

    private String tableSource;

    private String tableDest;

    private int deleteAfter;

    Instant lastBackup;
    
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTableSource() {
        return tableSource;
    }

    public void setTableSource(String tableSource) {
        this.tableSource = tableSource;
    }

    public String getTableDest() {
        return tableDest;
    }

    public void setTableDest(String tableDest) {
        this.tableDest = tableDest;
    }

    public int getDeleteAfter() {
        return deleteAfter;
    }

    public void setDeleteAfter(int deleteAfter) {
        this.deleteAfter = deleteAfter;
    }

    public Instant getLastBackup() {
        return lastBackup;
    }

    public void setLastBackup(Instant lastBackup) {
        this.lastBackup = lastBackup;
    }

    public BackupDataDTO(Long id, String tableSource, String tableDest, Instant lastBackup) {
        this.id = id;
        this.tableSource = tableSource;
        this.tableDest = tableDest;
        this.lastBackup = lastBackup;
    }

    public BackupDataDTO(String tableSource, String tableDest, int deleteAfter, Instant lastBackup) {
        this.tableSource = tableSource;
        this.tableDest = tableDest;
        this.deleteAfter = 0;
        this.lastBackup = null;
    }

    public BackupDataDTO(Long id, String tableSource, String tableDest, int deleteAfter, Instant lastBackup) {
        this.id = id;
        this.tableSource = tableSource;
        this.tableDest = tableDest;
        this.deleteAfter = 0;
        this.lastBackup = null;
    }

    public BackupDataDTO() {
    }

    
}
