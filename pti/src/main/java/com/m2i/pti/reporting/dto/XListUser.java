package com.m2i.pti.reporting.dto;

import com.m2i.pti.reporting.util.ReportHeader;

import java.util.List;

public class XListUser extends ReportHeader {
    private List<XUser> listUser;

    public List<XUser> getListUser() {
        return listUser;
    }

    public void setListUser(List<XUser> listUser) {
        this.listUser = listUser;
    }
}
