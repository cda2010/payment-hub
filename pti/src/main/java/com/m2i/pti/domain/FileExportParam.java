/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.domain;

import com.m2i.pti.domain.enumeration.Operateur;
import com.m2i.pti.domain.enumeration.OperateurLogique;
import com.m2i.pti.domain.enumeration.TypeChamps;
import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "file_export_param")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class FileExportParam implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "operateur")
    private Operateur operateur;
    @Enumerated(EnumType.STRING)
    @Column(name = "operateur_logique")
    private OperateurLogique operateurLogique;
 
    @Column(name = "valeur")
    private String valeur;
    @ManyToOne
    public FileExport fileExport;
    @ManyToOne
    ParametreExport parametreExport;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Operateur getOperateur() {
        return operateur;
    }

    public void setOperateur(Operateur operateur) {
        this.operateur = operateur;
    }

    public OperateurLogique getOperateurLogique() {
        return operateurLogique;
    }

    public void setOperateurLogique(OperateurLogique operateurLogique) {
        this.operateurLogique = operateurLogique;
    }

 

    public String getValeur() {
        return valeur;
    }

    public void setValeur(String valeur) {
        this.valeur = valeur;
    }

    public FileExport getFileExport() {
        return fileExport;
    }

    public void setFileExport(FileExport fileExport) {
        this.fileExport = fileExport;
    }

    public ParametreExport getParametreExport() {
        return parametreExport;
    }

    public void setParametreExport(ParametreExport parametreExport) {
        this.parametreExport = parametreExport;
    }

    @Override
    public String toString() {
        return "{" +
            " id='" + getId() + "'" +
            ", operateur='" + getOperateur() + "'" +
            ", operateurLogique='" + getOperateurLogique() + "'" +
            ", valeur='" + getValeur() + "'" +
            ", fileExport='" + getFileExport() + "'" +
            ", parametreExport='" + getParametreExport() + "'" +
            "}";
    }
}

  
