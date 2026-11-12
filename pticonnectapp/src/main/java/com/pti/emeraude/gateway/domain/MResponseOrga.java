/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pti.emeraude.gateway.domain;

/**
 *
 * @author cda
 */
public class MResponseOrga extends StandardResponse {

    private String code;
    private String message;
    private MOrganisation mOrga;

    public MOrganisation getmOrga() {
        return mOrga;
    }

    public void setmOrga(MOrganisation mOrga) {
        this.mOrga = mOrga;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
