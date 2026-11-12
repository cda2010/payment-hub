/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.service.dto;

import com.m2i.pti.domain.Facturier;
import com.m2i.pti.domain.GroupeProfilActeur;
import com.m2i.pti.domain.TypeService;
import com.m2i.pti.domain.enumeration.ActionService;
import com.m2i.pti.domain.enumeration.TypeActeurFacturier;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;


/**
 *
 * @author kadsuke
 */

public class FacturierProfilDTO implements Serializable {

    private Long id;
    
    private List<GroupeProfilActeur> groupeProfilActeur = new ArrayList<>(); 

    private FacturierDTO facturier;
 
    private TypeActeurFacturier typeActeur;

    public List<GroupeProfilActeur> getGroupeProfilActeur() {
        return groupeProfilActeur;
    }

    public void setGroupeProfilActeur(List<GroupeProfilActeur> groupeProfilActeur) {
        this.groupeProfilActeur = groupeProfilActeur;
    }

    public FacturierDTO getFacturier() {
        return facturier;
    }

    public void setFacturier(FacturierDTO facturier) {
        this.facturier = facturier;
    }

    public TypeActeurFacturier getTypeActeur() {
        return typeActeur;
    }

    public void setTypeActeur(TypeActeurFacturier typeActeur) {
        this.typeActeur = typeActeur;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }



}
