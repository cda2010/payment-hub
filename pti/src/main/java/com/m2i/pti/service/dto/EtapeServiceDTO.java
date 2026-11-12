/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.service.dto;

import com.m2i.pti.domain.TypeService;
import com.m2i.pti.domain.enumeration.ActionService;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;


/**
 *
 * @author kadsuke
 */

public class EtapeServiceDTO implements Serializable {

    private Long id;

    private List<String> actionService = new ArrayList<>();

    private TypeServiceDTO typeService;



 
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }                                                                                                                                                              

    
    public EtapeServiceDTO() {
    }


    public List<String> getActionService() {
        return actionService;
    }

    public void setActionService(List<String> actionService) {
        this.actionService = actionService;
    }

    public TypeServiceDTO getTypeService() {
        return typeService;
    }

    public void setTypeService(TypeServiceDTO typeService) {
        this.typeService = typeService;
    }

}
