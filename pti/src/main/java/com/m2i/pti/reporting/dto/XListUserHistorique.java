package com.m2i.pti.reporting.dto;

import com.m2i.pti.reporting.util.ReportHeader;

import java.util.List;

public class XListUserHistorique extends ReportHeader {
    private List<XUserHistorique> listUsers;

    public List<XUserHistorique> getListUsers() {
        return listUsers;
    }

    public void setListUsers(List<XUserHistorique> listUsers) {
        this.listUsers = listUsers;
    }
}
