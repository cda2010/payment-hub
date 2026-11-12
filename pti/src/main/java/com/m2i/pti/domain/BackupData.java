/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.domain;

import java.io.Serializable;
import java.time.Instant;
import jakarta.persistence.Column;
import jakarta.persistence.ColumnResult;
import jakarta.persistence.ConstructorResult;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedNativeQueries;
import jakarta.persistence.NamedNativeQuery;
import jakarta.persistence.QueryHint;
import jakarta.persistence.SqlResultSetMapping;
import jakarta.persistence.SqlResultSetMappings;
import jakarta.persistence.Table;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.m2i.pti.service.dto.BackupDataDTO;

/**
 *
 * @author cda
 */
@SqlResultSetMappings(value = {
    @SqlResultSetMapping(name = "Mapping.BackupDataDTO",
            classes = @ConstructorResult(targetClass = BackupDataDTO.class,
                    columns = {
                        @ColumnResult(name = "id", type = Long.class),
                        @ColumnResult(name = "table_source", type = String.class),
                        @ColumnResult(name = "table_dest", type = String.class),
                        @ColumnResult(name = "last_backup", type = Instant.class),
                       
                    }))
})
@NamedNativeQueries({
    @NamedNativeQuery(name = "BackupData.findAllBackupByDate",
            query = "select bkp.id, bkp.table_dest, bkp.table_source,bkp.last_backup FROM backup_data bkp WHERE (" 
            +"CAST(CAST(:dateLastDeb as text) as timestamp) is null or bkp.last_backup BETWEEN CAST(CAST(:dateLastDeb AS text) AS timestamp ) and CAST(CAST(:dateLastFin AS text) AS timestamp ) )" 
            +"order by bkp.id desc",
            resultSetMapping = "Mapping.BackupDataDTO",
            hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")}),  
            })
@Entity
@Table(name = "backup_data")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class BackupData implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "table_source")
    private String tableSource;
    @Column(name = "table_dest")
    private String tableDest;
    @Column(name = "delete_after")
    private int deleteAfter;
    @Column(name = "last_backup")
    Instant lastBackup;
    
    @Column(name = "update_able", columnDefinition = "boolean default false")
    boolean updatable;
    
     @Column(name = "table_source_key")
    private String tableSourceKey;
    @Column(name = "table_dest_key")
    private String tableDestKey;
    
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

    public boolean isUpdatable() {
        return updatable;
    }

    public void setUpdatable(boolean updatable) {
        this.updatable = updatable;
    }

    public String getTableSourceKey() {
        return tableSourceKey;
    }

    public void setTableSourceKey(String tableSourceKey) {
        this.tableSourceKey = tableSourceKey;
    }

    public String getTableDestKey() {
        return tableDestKey;
    }

    public void setTableDestKey(String tableDestKey) {
        this.tableDestKey = tableDestKey;
    }

    public void setLastBackup(Instant lastBackup) {
        this.lastBackup = lastBackup;
    }


}
