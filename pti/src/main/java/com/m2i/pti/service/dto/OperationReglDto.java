/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.service.dto;

/**
 *
 * @author cda
 */
public class OperationReglDto {
    String typeService;
    String canal;
    Long uoAcq;
   String login;
    public String getTypeService() {
        return typeService;
    }

    public void setTypeService(String typeService) {
        this.typeService = typeService;
    }

    public String getCanal() {
        return canal;
    }

    public void setCanal(String canal) {
        this.canal = canal;
    }

    public Long getUoAcq() {
        return uoAcq;
    }

    public void setUoAcq(Long uoAcq) {
        this.uoAcq = uoAcq;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }
    
}
