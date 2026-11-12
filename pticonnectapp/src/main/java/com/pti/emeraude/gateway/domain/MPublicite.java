/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pti.emeraude.gateway.domain;


/**
 * DTO mobile pour Publicite.
 * L'image se récupère via GET /api/publicite/image/{id}
 */
public class MPublicite {
    private Long id;
    private Integer frequence;
 private byte[] imageData;

  
    private String contentType;

    public MPublicite(Long id, Integer frequence, byte[] imageData, String contentType) {
        this.id = id;
        this.frequence = frequence;
        this.imageData = imageData;
        this.contentType = contentType;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Long getId() {
        return id;
    }

    public Integer getFrequence() {
        return frequence;
    }

    public void setId(Long id) {
        this.id=id;
    }
    public void setFrequence(Integer frequence) {       
        this.frequence = frequence;
    }
    
}
