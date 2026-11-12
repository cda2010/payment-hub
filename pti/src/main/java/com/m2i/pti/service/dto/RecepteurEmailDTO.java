package com.m2i.pti.service.dto;

import java.io.Serializable;

public class RecepteurEmailDTO implements Serializable{

    private Long id;

    private String email;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    
}
