/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.omnicanal.presentation.domain;

import com.m2i.omnicanal.model.FournisseurService;
import com.m2i.omnicanal.model.enumeration.ProprieteApi;
import java.util.List;

/**
 *
 * @author cda
 */
public class ParametreDTO {
    private String canal;
    private String referenceExterne;
    private String codeService;
    private String fse;
    private ProprieteApi proprieteApi;
    private List<ParametreUser> param;
    private String codeUniteOrganisation;

    public String getCodeService() {
        return codeService;
    }

    public void setCodeService(String codeService) {
        this.codeService = codeService;
    }

    public List<ParametreUser> getParam() {
        return param;
    }

    public void setParam(List<ParametreUser> param) {
        this.param = param;
    }

    public ProprieteApi getProprieteApi() {
        return proprieteApi;
    }

    public void setProprieteApi(ProprieteApi proprieteApi) {
        this.proprieteApi = proprieteApi;
    }

    public String getFse() {
        return fse;
    }

    public void setFse(String fse) {
        this.fse = fse;
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
