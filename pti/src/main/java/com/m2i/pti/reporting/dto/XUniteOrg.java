package com.m2i.pti.reporting.dto;

import java.util.List;

public class XUniteOrg {
   public String title;
   public String logo;
   public String fmt;
   public String date;
    public List<ListeUniteOrg> uniteOrgList;

    public String getTitle() {
        return title;
    }

    public void setTitle(String titre) {
        this.title = titre;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getFmt() {
        return fmt;
    }

    public void setFmt(String fmt) {
        this.fmt = fmt;
    }

    public List<ListeUniteOrg> getUniteOrgList() {
        return uniteOrgList;
    }

    public void setUniteOrgList(List<ListeUniteOrg> uniteOrgList) {
        this.uniteOrgList = uniteOrgList;
    }


    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
