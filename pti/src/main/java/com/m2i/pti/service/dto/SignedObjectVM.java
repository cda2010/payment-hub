/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.service.dto;

import jakarta.validation.constraints.NotNull;
import java.time.ZonedDateTime;
import java.util.Base64;

/**
 * @author yabolank
 */
public class SignedObjectVM {

    private String message;
    private String format;
    private String algorith;
    private String CN;
    private String hostId;
    private String hostName;
    private String currentUser;
    private String login;
    private ZonedDateTime hostDate;

    /**
     * Décode le contenu d'un objet signé en base64
     *
     * @param signedObjectVM : L'objet contenant des champs encodé
     * @return
     */
    public static SignedObjectVM decode(@NotNull SignedObjectVM signedObjectVM) {
        signedObjectVM.hostId = new String(Base64.getDecoder().decode(signedObjectVM.hostId.trim()));
        signedObjectVM.hostName = new String(Base64.getDecoder().decode(signedObjectVM.hostName.trim()));
        signedObjectVM.currentUser = new String(Base64.getDecoder().decode(signedObjectVM.currentUser.trim()));
        return signedObjectVM;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getAlgorith() {
        return algorith;
    }

    public void setAlgorith(String algorith) {
        this.algorith = algorith;
    }

    public String getCN() {
        return CN;
    }

    public void setCN(String CN) {
        this.CN = CN;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getHostId() {
        return hostId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(String currentUser) {
        this.currentUser = currentUser;
    }

    @Override
    public String toString() {
        return "SignedObject{" +
            "\nmessage='" + message + '\'' +
            "\n, format='" + format + '\'' +
            "\n, algorith='" + algorith + '\'' +
            "\n, CN='" + CN + '\'' +
            "\n, hostId='" + hostId + '\'' +
            "\n, hostName='" + hostName + '\'' +
            "\n, login='" + login + '\'' +
            "\n, currentUser='" + currentUser + '\'' +
            '}';
    }

    public ZonedDateTime getHostDate() {
        return hostDate;
    }

    public void setHostDate(ZonedDateTime hostDate) {
        this.hostDate = hostDate;
    }
}
