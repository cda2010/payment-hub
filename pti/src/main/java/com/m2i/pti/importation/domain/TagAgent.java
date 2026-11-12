package com.m2i.pti.importation.domain;

import com.univocity.parsers.annotations.Parsed;
import com.univocity.parsers.annotations.Trim;
/**
 * @author O.soufiane
 */
public class TagAgent extends TagIdentiy {
    @Trim
    @Parsed(field = "agt_id")
    private int agt_id;
    @Trim
    @Parsed(field = "agt_idt_id")
    private int agt_idt_id;
    @Trim
    @Parsed(field = "agt_type")
    private String agt_type;
    @Trim
    @Parsed(field = "agt_network_id")
    private String agt_network_id;
    @Trim
    @Parsed(field = "agt_code")
    private long agt_code;
    @Trim
    @Parsed(field = "agt_appli")
    private String agt_appli;
    @Trim
    @Parsed(field = "agt_immat")
    private String agt_immat;
    @Trim
    @Parsed(field = "agt_legalid_type")
    private String agt_legalid_type;
    @Trim
    @Parsed(field = "agt_legalid_number")
    private String agt_legalid_number;
    @Trim
    @Parsed(field = "agt_legalid_check")
    private String agt_legalid_check;
    @Trim
    @Parsed(field = "agt_job")
    private String agt_job;
    @Trim
    @Parsed(field = "agt_employer")
    private String agt_employer;
    @Trim
    @Parsed(field = "agt_tagpay_account")
    private String agt_tagpay_account;
    @Trim
    @Parsed(field = "agt_organization")
    private String agt_organization;
    @Trim
    @Parsed(field = "agt_organization_job")
    private String agt_organization_job;
    @Trim
    @Parsed(field = "agt_contract_datetime")
    private String agt_contract_datetime;
    @Trim
    @Parsed(field = "agt_contract_check")
    private int agt_contract_check;
    @Trim
    @Parsed(field = "agt_group")
    private String agt_group;
    @Trim
    @Parsed(field = "agt_state")
    private String agt_state;
    @Trim
    @Parsed(field = "agt_firstuse")
    private String agt_firstuse;
    @Trim
    @Parsed(field = "agt_lastuse")
    private String agt_lastuse;
    @Trim
    @Parsed(field = "agt_countuse")
    private int agt_countuse;
    @Trim
    @Parsed(field = "agt_cos_id")
    private String agt_cos_id;
    @Trim
    @Parsed(field = "agt_param")
    private String agt_param;
    @Trim
    @Parsed(field = "agt_category")
    private int agt_category;
    @Trim
    @Parsed(field = "agt_settlement_mode")
    private String agt_settlement_mode;
    @Trim
    @Parsed(field = "agt_settlement_notification")
    private String agt_settlement_notification;

    public int getAgt_id() {
        return agt_id;
    }

    public void setAgt_id(int agt_id) {
        this.agt_id = agt_id;
    }

    public int getAgt_idt_id() {
        return agt_idt_id;
    }

    public void setAgt_idt_id(int agt_idt_id) {
        this.agt_idt_id = agt_idt_id;
    }

    public String getAgt_type() {
        return agt_type;
    }

    public void setAgt_type(String agt_type) {
        this.agt_type = agt_type;
    }

    public String getAgt_network_id() {
        return agt_network_id;
    }

    public void setAgt_network_id(String agt_network_id) {
        this.agt_network_id = agt_network_id;
    }

    public long getAgt_code() {
        return agt_code;
    }

    public void setAgt_code(long agt_code) {
        this.agt_code = agt_code;
    }

    public String getAgt_appli() {
        return agt_appli;
    }

    public void setAgt_appli(String agt_appli) {
        this.agt_appli = agt_appli;
    }

    public String getAgt_immat() {
        return agt_immat;
    }

    public void setAgt_immat(String agt_immat) {
        this.agt_immat = agt_immat;
    }

    public String getAgt_legalid_type() {
        return agt_legalid_type;
    }

    public void setAgt_legalid_type(String agt_legalid_type) {
        this.agt_legalid_type = agt_legalid_type;
    }

    public String getAgt_legalid_number() {
        return agt_legalid_number;
    }

    public void setAgt_legalid_number(String agt_legalid_number) {
        this.agt_legalid_number = agt_legalid_number;
    }

    public String getAgt_legalid_check() {
        return agt_legalid_check;
    }

    public void setAgt_legalid_check(String agt_legalid_check) {
        this.agt_legalid_check = agt_legalid_check;
    }

    public String getAgt_job() {
        return agt_job;
    }

    public void setAgt_job(String agt_job) {
        this.agt_job = agt_job;
    }

    public String getAgt_employer() {
        return agt_employer;
    }

    public void setAgt_employer(String agt_employer) {
        this.agt_employer = agt_employer;
    }

    public String getAgt_tagpay_account() {
        return agt_tagpay_account;
    }

    public void setAgt_tagpay_account(String agt_tagpay_account) {
        this.agt_tagpay_account = agt_tagpay_account;
    }

    public String getAgt_organization() {
        return agt_organization;
    }

    public void setAgt_organization(String agt_organization) {
        this.agt_organization = agt_organization;
    }

    public String getAgt_organization_job() {
        return agt_organization_job;
    }

    public void setAgt_organization_job(String agt_organization_job) {
        this.agt_organization_job = agt_organization_job;
    }

    public String getAgt_contract_datetime() {
        return agt_contract_datetime;
    }

    public void setAgt_contract_datetime(String agt_contract_datetime) {
        this.agt_contract_datetime = agt_contract_datetime;
    }

    public int getAgt_contract_check() {
        return agt_contract_check;
    }

    public void setAgt_contract_check(int agt_contract_check) {
        this.agt_contract_check = agt_contract_check;
    }

    public String getAgt_group() {
        return agt_group;
    }

    public void setAgt_group(String agt_group) {
        this.agt_group = agt_group;
    }

    public String getAgt_state() {
        return agt_state;
    }

    public void setAgt_state(String agt_state) {
        this.agt_state = agt_state;
    }

    public String getAgt_firstuse() {
        return agt_firstuse;
    }

    public void setAgt_firstuse(String agt_firstuse) {
        this.agt_firstuse = agt_firstuse;
    }

    public String getAgt_lastuse() {
        return agt_lastuse;
    }

    public void setAgt_lastuse(String agt_lastuse) {
        this.agt_lastuse = agt_lastuse;
    }

    public int getAgt_countuse() {
        return agt_countuse;
    }

    public void setAgt_countuse(int agt_countuse) {
        this.agt_countuse = agt_countuse;
    }

    public String getAgt_cos_id() {
        return agt_cos_id;
    }

    public void setAgt_cos_id(String agt_cos_id) {
        this.agt_cos_id = agt_cos_id;
    }

    public String getAgt_param() {
        return agt_param;
    }

    public void setAgt_param(String agt_param) {
        this.agt_param = agt_param;
    }

    public int getAgt_category() {
        return agt_category;
    }

    public void setAgt_category(int agt_category) {
        this.agt_category = agt_category;
    }

    public String getAgt_settlement_mode() {
        return agt_settlement_mode;
    }

    public void setAgt_settlement_mode(String agt_settlement_mode) {
        this.agt_settlement_mode = agt_settlement_mode;
    }

    public String getAgt_settlement_notification() {
        return agt_settlement_notification;
    }

    public void setAgt_settlement_notification(String agt_settlement_notification) {
        this.agt_settlement_notification = agt_settlement_notification;
    }

}
