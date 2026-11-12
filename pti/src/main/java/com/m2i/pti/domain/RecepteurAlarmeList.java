package com.m2i.pti.domain;

import java.io.Serializable;
import java.util.List;

public class RecepteurAlarmeList extends AbstractAuditingEntity implements Serializable {
    private List<String> emails;
    private  List<String>sms;

    public List<String> getEmails() {
        return emails;
    }

    public void setEmails(List<String> emails) {
        this.emails = emails;
    }

    public List<String> getSms() {
        return sms;
    }

    public void setSms(List<String> sms) {
        this.sms = sms;
    }
}
