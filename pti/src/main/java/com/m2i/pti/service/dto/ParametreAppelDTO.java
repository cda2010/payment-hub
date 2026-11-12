package com.m2i.pti.service.dto;

import java.util.List;

public class ParametreAppelDTO {
    private String fse;
    private String codeMethode;
    private List<ParametreUserDTO> param;
 private String canal;
    public String getFse() {
        return fse;
    }

    public void setFse(String fse) {
        this.fse = fse;
    }

    public String getCodeMethode() {
        return codeMethode;
    }

    public void setCodeMethode(String codeMethode) {
        this.codeMethode = codeMethode;
    }

    public List<ParametreUserDTO> getParam() {
        return param;
    }

    public void setParam(List<ParametreUserDTO> param) {
        this.param = param;
    }

    public String getCanal() {
        return canal;
    }

    public void setCanal(String canal) {
        this.canal = canal;
    }
    
}
