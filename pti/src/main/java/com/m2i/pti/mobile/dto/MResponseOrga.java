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
public class MResponseOrga {
    MOrganisation mOrga;
    private String code;
    private String message;

    public MResponseOrga( String code, String message,MOrganisation mOrga) {
        
        this.code = code;
        this.message = message;
        this.mOrga = mOrga;
    }


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
    
     public MResponseOrga code(String code) {
        this.code = code;
        return this;
    }

    

    public MResponseOrga message(String message) {
        this.message = message;
        return this;
    }
    public MResponseOrga message(MOrganisation mOrga) {
        this.mOrga = mOrga;
        return this;
    }
    
}
