package com.m2i.pti.reporting.dto;

import com.m2i.pti.reporting.util.ReportHeader;

import java.util.List;

public class XListAuditEvent extends ReportHeader {
    List<XAuditEvent> listAuditEvent;

    public List<XAuditEvent> getListAuditEvent() {
        return listAuditEvent;
    }

    public void setListAuditEvent(List<XAuditEvent> listAuditEvent) {
        this.listAuditEvent = listAuditEvent;
    }
}
