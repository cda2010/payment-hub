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
public class CanalCommunicationDTO {
    private Long id;

    private Long facturierId;

    private Long ftpId;

    private Long recepteurEmailId;
private String fse;
    public CanalCommunicationDTO() {
    }

    public CanalCommunicationDTO(Long id, Long facturierId, Long ftpId, Long recepteurEmailId,String fse) {
        this.id = id;
        this.facturierId = facturierId;
        this.ftpId = ftpId;
        this.recepteurEmailId = recepteurEmailId;
        this.fse=fse;
    }

    public String getFse() {
        return fse;
    }

    public void setFse(String fse) {
        this.fse = fse;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFacturierId() {
        return facturierId;
    }

    public void setFacturierId(Long facturierId) {
        this.facturierId = facturierId;
    }

    public Long getFtpId() {
        return ftpId;
    }

    public void setFtpId(Long ftpId) {
        this.ftpId = ftpId;
    }

    public Long getRecepteurEmailId() {
        return recepteurEmailId;
    }

    public void setRecepteurEmailId(Long recepteurEmailId) {
        this.recepteurEmailId = recepteurEmailId;
    }
    
    
}
