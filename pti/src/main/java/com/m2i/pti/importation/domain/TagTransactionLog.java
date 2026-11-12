package com.m2i.pti.importation.domain;

import com.univocity.parsers.annotations.Parsed;
import com.univocity.parsers.annotations.Trim;

/**
 * @author O.soufiane
 */
public class TagTransactionLog {
    @Trim
    @Parsed(field = "aco_id")
    private String aco_id;
    @Trim
    @Parsed(field = "aco_trx_id")
    private String aco_trx_id;
    @Trim
    @Parsed(field = "aco_acc_id")
    private String aco_acc_id;
    @Trim
    @Parsed(field = "aco_amount")
    private String aco_amount;
    @Trim
    @Parsed(field = "aco_balance")
    private String aco_balance;
    @Trim
    @Parsed(field = "aco_cur_id")
    private String aco_cur_id;
    @Trim
    @Parsed(field = "aco_description")
    private String aco_description;
    @Trim
    @Parsed(field = "aco_ref")
    private String aco_ref;
    @Trim
    @Parsed(field = "aco_type")
    private String aco_type;
    @Trim
    @Parsed(field = "aco_params")
    private String aco_params;
    @Trim
    @Parsed(field = "aco_sla_id")
    private String aco_sla_id;
    @Trim
    @Parsed(field = "aco_datetime")
    private String aco_datetime;

    public String getAco_id() {
        return aco_id;
    }

    public void setAco_id(String aco_id) {
        this.aco_id = aco_id;
    }

    public String getAco_trx_id() {
        return aco_trx_id;
    }

    public void setAco_trx_id(String aco_trx_id) {
        this.aco_trx_id = aco_trx_id;
    }

    public String getAco_acc_id() {
        return aco_acc_id;
    }

    public void setAco_acc_id(String aco_acc_id) {
        this.aco_acc_id = aco_acc_id;
    }

    public String getAco_amount() {
        return aco_amount;
    }

    public void setAco_amount(String aco_amount) {
        this.aco_amount = aco_amount;
    }

    public String getAco_balance() {
        return aco_balance;
    }

    public void setAco_balance(String aco_balance) {
        this.aco_balance = aco_balance;
    }

    public String getAco_cur_id() {
        return aco_cur_id;
    }

    public void setAco_cur_id(String aco_cur_id) {
        this.aco_cur_id = aco_cur_id;
    }

    public String getAco_description() {
        return aco_description;
    }

    public void setAco_description(String aco_description) {
        this.aco_description = aco_description;
    }

    public String getAco_ref() {
        return aco_ref;
    }

    public void setAco_ref(String aco_ref) {
        this.aco_ref = aco_ref;
    }

    public String getAco_type() {
        return aco_type;
    }

    public void setAco_type(String aco_type) {
        this.aco_type = aco_type;
    }

    public String getAco_params() {
        return aco_params;
    }

    public void setAco_params(String aco_params) {
        this.aco_params = aco_params;
    }

    public String getAco_sla_id() {
        return aco_sla_id;
    }

    public void setAco_sla_id(String aco_sla_id) {
        this.aco_sla_id = aco_sla_id;
    }

    public String getAco_datetime() {
        return aco_datetime;
    }

    public void setAco_datetime(String aco_datetime) {
        this.aco_datetime = aco_datetime;
    }
}
