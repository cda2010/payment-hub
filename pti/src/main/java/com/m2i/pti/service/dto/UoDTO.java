/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.service.dto;

import java.io.Serializable;

/**
 *
 * @author cda
 */
public class UoDTO  implements Serializable {
    Long id;
    String libelleUniteOrganisation;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLibelleUniteOrganisation() {
        return libelleUniteOrganisation;
    }

    public void setLibelleUniteOrganisation(String libelleUniteOrganisation) {
        this.libelleUniteOrganisation = libelleUniteOrganisation;
    }
    
}
