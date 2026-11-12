/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.mobile.dto;

/**
 *
 * @author cda
 */
public class MTerminal {

    private String codeIndicatifPays;
    private String uuid;
    private String platform;
    private String model;
    private String manufacturer;
    private String telephone;
    private String codeUo;
    private String typeTerminal;
    private String login;
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getCodeUo() {
        return codeUo;
    }

    public void setCodeUo(String codeUo) {
        this.codeUo = codeUo;
    }

    public String getTypeTerminal() {
        return typeTerminal;
    }

    public void setTypeTerminal(String typeTerminal) {
        this.typeTerminal = typeTerminal;
    }

    public String getCodeIndicatifPays() {
        return codeIndicatifPays;
    }

    public void setCodeIndicatifPays(String codeIndicatifPays) {
        this.codeIndicatifPays = codeIndicatifPays;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

}
