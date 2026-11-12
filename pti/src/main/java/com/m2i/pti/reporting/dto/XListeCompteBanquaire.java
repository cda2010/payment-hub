package com.m2i.pti.reporting.dto;

import com.m2i.pti.reporting.util.ReportHeader;

import java.util.List;

public class XListeCompteBanquaire extends ReportHeader {

    List<XBanqueReportDTO> listeBanque;

    public List<XBanqueReportDTO> getListeBanque() {
        return listeBanque;
    }

    public void setListeBanque(List<XBanqueReportDTO> listeBanque) {
        this.listeBanque = listeBanque;
    }
}
