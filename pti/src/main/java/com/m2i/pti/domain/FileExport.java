/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.domain;

import java.io.Serializable;
import java.time.LocalDate;
import jakarta.persistence.Column;
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
@Table(name = "file_export")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class FileExport implements Serializable {


    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "file_name")
    String fileName;
    
    @Column(name = "date_generation")
    private LocalDate dateGeneration;

    @ManyToOne
    public ExportTable exportTable;

    @Column(name = "parameter")
    String parameter;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public LocalDate getDateGeneration() {
        return dateGeneration;
    }

    public void setDateGeneration(LocalDate dateGeneration) {
        this.dateGeneration = dateGeneration;
    }

    public ExportTable getExportTable() {
        return exportTable;
    }

    public void setExportTable(ExportTable exportTable) {
        this.exportTable = exportTable;
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }


    @Override
    public String toString() {
        return "{" +
            " id='" + getId() + "'" +
            ", fileName='" + getFileName() + "'" +
            ", dateGeneration='" + getDateGeneration() + "'" +
            ", exportTable='" + getExportTable() + "'" +
            ", parameter='" + getParameter() + "'" +
            "}";
    }
    
}
