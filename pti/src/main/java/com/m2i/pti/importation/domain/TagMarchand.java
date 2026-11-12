package com.m2i.pti.importation.domain;

import com.univocity.parsers.annotations.Parsed;
import com.univocity.parsers.annotations.Trim;
/**
 * @author O.soufiane
 */
public class TagMarchand extends TagIdentiy {
    @Trim
    @Parsed(field = "mer_id")
    private String mer_id;


    @Trim
    @Parsed(field = "mer_state")
    private String mer_state;

    @Trim
    @Parsed(field = "mer_idt_id")
    private String mer_idt_id;

    @Trim
    @Parsed(field = "mer_cos_id")
    private String mer_cos_id;

    @Trim
    @Parsed(field = "mer_title")
    private String mer_title;

    @Trim
    @Parsed(field = "mer_firstuse")
    private String mer_firstuse;

    @Trim
    @Parsed(field = "mer_legalid_type")
    private String mer_legalid_type;

    @Trim
    @Parsed(field = "mer_legalid_number")
    private String mer_legalid_number;

    @Trim
    @Parsed(field = "mer_immat")
    private String mer_immat;

    @Trim
    @Parsed(field = "mer_bill_companyname")
    private String mer_bill_companyname;

    @Trim
    @Parsed(field = "mer_settlement_mode")
    private String mer_settlement_mode;

    @Trim
    @Parsed(field = "mer_settlement_notification")
    private String mer_settlement_notification;

    public String getMer_id() {
        return mer_id;
    }

    public void setMer_id(String mer_id) {
        this.mer_id = mer_id;
    }

    public String getMer_idt_id() {
        return mer_idt_id;
    }

    public void setMer_idt_id(String mer_idt_id) {
        this.mer_idt_id = mer_idt_id;
    }

    public String getMer_cos_id() {
        return mer_cos_id;
    }

    public void setMer_cos_id(String mer_cos_id) {
        this.mer_cos_id = mer_cos_id;
    }

    public String getMer_title() {
        return mer_title;
    }

    public void setMer_title(String mer_title) {
        this.mer_title = mer_title;
    }

    public String getMer_firstuse() {
        return mer_firstuse;
    }

    public void setMer_firstuse(String mer_firstuse) {
        this.mer_firstuse = mer_firstuse;
    }

    public String getMer_legalid_type() {
        return mer_legalid_type;
    }

    public void setMer_legalid_type(String mer_legalid_type) {
        this.mer_legalid_type = mer_legalid_type;
    }

    public String getMer_legalid_number() {
        return mer_legalid_number;
    }

    public void setMer_legalid_number(String mer_legalid_number) {
        this.mer_legalid_number = mer_legalid_number;
    }

    public String getMer_bill_companyname() {
        return mer_bill_companyname;
    }

    public void setMer_bill_companyname(String mer_bill_companyname) {
        this.mer_bill_companyname = mer_bill_companyname;
    }

    public String getMer_settlement_mode() {
        return mer_settlement_mode;
    }

    public void setMer_settlement_mode(String mer_settlement_mode) {
        this.mer_settlement_mode = mer_settlement_mode;
    }

    public String getMer_settlement_notification() {
        return mer_settlement_notification;
    }

    public void setMer_settlement_notification(String mer_settlement_notification) {
        this.mer_settlement_notification = mer_settlement_notification;
    }

    public String getMer_state() {
        return mer_state;
    }

    public void setMer_state(String mer_state) {
        this.mer_state = mer_state;
    }

    public String getMer_immat() {
        return mer_immat;
    }

    public void setMer_immat(String mer_immat) {
        this.mer_immat = mer_immat;
    }

}
