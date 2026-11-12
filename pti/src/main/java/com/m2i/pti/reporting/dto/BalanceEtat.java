package com.m2i.pti.reporting.dto;

public class BalanceEtat {
    public String type;
    public Long fse;

    public String format;

    public String typeUO;

    public Long getFse() {
        return fse;
    }

    public void setFse(Long fse) {
        this.fse = fse;
    }



    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getTypeUO() {
        return typeUO;
    }

    public void setTypeUO(String typeUO) {
        this.typeUO = typeUO;
    }
}
