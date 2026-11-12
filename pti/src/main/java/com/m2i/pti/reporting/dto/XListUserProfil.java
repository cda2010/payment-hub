package com.m2i.pti.reporting.dto;

import com.m2i.pti.reporting.util.ReportHeader;

import java.util.List;

public class XListUserProfil extends ReportHeader {
    List<XUserProfil> listProfils;

    public List<XUserProfil> getListProfils() {
        return listProfils;
    }

    public void setListProfils(List<XUserProfil> listProfils) {
        this.listProfils = listProfils;
    }
}
