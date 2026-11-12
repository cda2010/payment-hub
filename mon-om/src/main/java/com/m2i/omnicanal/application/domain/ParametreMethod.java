/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.omnicanal.application.domain;

import java.io.Serializable;

import com.m2i.omnicanal.model.Methode;
import com.m2i.omnicanal.model.ParametreMethode;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

/**
 *
 * @author cda
 */
@Entity
public class ParametreMethod extends ParametreMethode implements Serializable {
    @ManyToOne
    private EtiquetteMethode etiquetteMethode;

    public EtiquetteMethode getEtiquetteMethode() {
        return etiquetteMethode;
    }

    public void setEtiquetteMethode(EtiquetteMethode etiquetteMethode) {
        this.etiquetteMethode = etiquetteMethode;
    }
}
