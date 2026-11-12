package com.pti.emeraude.gateway.domain;

public class MAutorisation {
    private String service;

    public MAutorisation() {
       
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public MAutorisation(String service) {
        this.service = service;
    }
}
