/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.domain;

import java.io.Serializable;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 *
 * @author cda
 */
@Entity
@Table(name = "backup_data_fields")

public class BackupDataFields implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    String fieldToUpdate;
     String streamForUpdate;
    @ManyToOne
    BackupData backupData;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

   

    public BackupData getBackupData() {
        return backupData;
    }

    public void setBackupData(BackupData backupData) {
        this.backupData = backupData;
    }

    public String getFieldToUpdate() {
        return fieldToUpdate;
    }

    public void setFieldToUpdate(String fieldToUpdate) {
        this.fieldToUpdate = fieldToUpdate;
    }

    public String getStreamForUpdate() {
        return streamForUpdate;
    }

    public void setStreamForUpdate(String streamForUpdate) {
        this.streamForUpdate = streamForUpdate;
    }

}
