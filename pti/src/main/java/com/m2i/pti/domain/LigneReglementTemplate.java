/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.domain;

import   com.m2i.pti.service.dto.UoDTO;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author cda
 */
public class LigneReglementTemplate implements Serializable  {
 
    private List<UoDTO> organisation = new ArrayList<>();
    public List<UoDTO> getOrganisation() {
        return organisation;
    }
    public void setOrganisation(List<UoDTO> organisation) {
        this.organisation = organisation;
    }

  

}
