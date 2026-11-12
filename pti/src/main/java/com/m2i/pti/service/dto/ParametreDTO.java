package com.m2i.pti.service.dto;

import com.m2i.pti.domain.enumeration.ProprieteApi;

import java.util.List;

public class ParametreDTO {
 private String canal;
    private String referenceExterne;
    private String codeService;
    private String fse;
    private ProprieteApi proprieteApi;
    private List<ParametreUserDTO> param;
    private String codeUniteOrganisation;

    public String getCodeService() {
        return codeService;
    }

    public void setCodeService(String codeService) {
        this.codeService = codeService;
    }

    public String getFse() {
        return fse;
    }

    public void setFse(String fse) {
        this.fse = fse;
    }

    public ProprieteApi getProprieteApi() {
        return proprieteApi;
    }

    public void setProprieteApi(ProprieteApi parametreApi) {
        this.proprieteApi = parametreApi;
    }

    public List<ParametreUserDTO> getParam() {
        return param;
    }

    public void setParam(List<ParametreUserDTO> param) {
        this.param = param;
    }

    public String getCodeUniteOrganisation() {
        return codeUniteOrganisation;
    }

    public void setCodeUniteOrganisation(String codeUniteOrganisation) {
        this.codeUniteOrganisation = codeUniteOrganisation;
    }

    public String getReferenceExterne() {
        return referenceExterne;
    }

    public void setReferenceExterne(String referenceExterne) {
        this.referenceExterne = referenceExterne;
    }

    public String getCanal() {
        return canal;
    }

    public void setCanal(String canal) {
        this.canal = canal;
    }
    
}
