/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.domain;

import java.io.Serializable;
import java.time.Instant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 *
 * @author cda
 */
@Entity
@Table(name = "param_backup_data")
public class ParamBackUpData implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "last_backup")
    Instant lastBackup;
    @Column(name = "en_cours")
    int enCours;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getLastBackup() {
        return lastBackup;
    }

    public void setLastBackup(Instant lastBackup) {
        this.lastBackup = lastBackup;
    }

    public int getEnCours() {
        return enCours;
    }

    public void setEnCours(int enCours) {
        this.enCours = enCours;
    }

}
