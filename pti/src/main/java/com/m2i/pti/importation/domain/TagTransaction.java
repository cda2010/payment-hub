package com.m2i.pti.importation.domain;

import com.univocity.parsers.annotations.Parsed;
import com.univocity.parsers.annotations.Trim;

/**
 * @author O.soufiane
 */
public class TagTransaction {
    @Trim
    @Parsed(field = "trf_id")
    private String trf_id;

    @Trim
    @Parsed(field = "trf_src_cli_id")
    private String trf_src_cli_id;
    @Trim
    @Parsed(field = "trf_dst_cli_id")
    private String trf_dst_cli_id;
    @Trim
    @Parsed(field = "trf_trx_id")
    private String trf_trx_id;
    @Trim
    @Parsed(field = "trf_status")
    private String trf_status;
    @Trim
    @Parsed(field = "trf_type")
    private String trf_type;
    @Trim
    @Parsed(field = "trf_cur_id")
    private String trf_cur_id;
    @Trim
    @Parsed(field = "trf_balance")
    private String trf_balance;
    @Trim
    @Parsed(field = "trf_amount")
    private String trf_amount;
    @Trim
    @Parsed(field = "trf_code")
    private String trf_code;
    @Trim
    @Parsed(field = "trf_label")
    private String trf_label;
    @Trim
    @Parsed(field = "trf_firstuse")
    private String trf_firstuse;
    @Trim
    @Parsed(field = "trf_lastuse")
    private String trf_lastuse;
    @Trim
    @Parsed(field = "trf_countuse")
    private String trf_countuse;
    @Trim
    @Parsed(field = "trf_expiry")
    private String trf_expiry;
    @Trim
    @Parsed(field = "trf_src_client_name")
    private String trf_src_client_name;
    @Trim
    @Parsed(field = "trf_src_client_number")
    private String trf_src_client_number;
    @Trim
    @Parsed(field = "trf_src_client_firstname")
    private String trf_src_client_firstname;
    @Trim
    @Parsed(field = "trf_src_client_cou_id")
    private String trf_src_client_cou_id;
    @Trim
    @Parsed(field = "trf_dst_client_name")
    private String trf_dst_client_name;
    @Trim
    @Parsed(field = "trf_dst_client_firstname")
    private String trf_dst_client_firstname;
    @Trim
    @Parsed(field = "trf_dst_client_number")
    private String trf_dst_client_number;
    @Trim
    @Parsed(field = "trf_dst_client_cou_id")
    private String trf_dst_client_cou_id;
    @Trim
    @Parsed(field = "trf_carrier")
    private String trf_carrier;
    @Trim
    @Parsed(field = "trf_param")
    private String trf_param;
    @Trim
    @Parsed(field = "trf_src_acc_id")
    private String trf_src_acc_id;
    @Trim
    @Parsed(field = "trx_id")
    private String trx_id;
    @Trim
    @Parsed(field = "trx_reversal_id")
    private String trx_reversal_id;
    @Trim
    @Parsed(field = "trx_type")
    private String trx_type;
    @Trim
    @Parsed(field = "trx_datetime")
    private String trx_datetime;
    @Trim
    @Parsed(field = "trx_amount")
    private String trx_amount;
    @Trim
    @Parsed(field = "trx_cur_id")
    private String trx_cur_id;
    @Trim
    @Parsed(field = "trx_label")
    private String trx_label;
    @Trim
    @Parsed(field = "trx_params")
    private String trx_params;
    @Trim
    @Parsed(field = "trx_Stringitude")
    private String trx_Stringitude;
    @Trim
    @Parsed(field = "trx_latitude")
    private String trx_latitude;
    @Trim
    @Parsed(field = "trx_auth_mode")
    private String trx_auth_mode;
    @Trim
    @Parsed(field = "trx_media")
    private String trx_media;
    @Trim
    @Parsed(field = "trx_position_type")
    private String trx_position_type;
    @Trim
    @Parsed(field = "trx_external_ref")
    private String trx_external_ref;

    public String getTrf_id() {
        return trf_id;
    }

    public void setTrf_id(String trf_id) {
        this.trf_id = trf_id;
    }

    public String getTrf_src_cli_id() {
        return trf_src_cli_id;
    }

    public void setTrf_src_cli_id(String trf_src_cli_id) {
        this.trf_src_cli_id = trf_src_cli_id;
    }

    public String getTrf_dst_cli_id() {
        return trf_dst_cli_id;
    }

    public void setTrf_dst_cli_id(String trf_dst_cli_id) {
        this.trf_dst_cli_id = trf_dst_cli_id;
    }

    public String getTrf_trx_id() {
        return trf_trx_id;
    }

    public void setTrf_trx_id(String trf_trx_id) {
        this.trf_trx_id = trf_trx_id;
    }

    public String getTrf_status() {
        return trf_status;
    }

    public void setTrf_status(String trf_status) {
        this.trf_status = trf_status;
    }

    public String getTrf_type() {
        return trf_type;
    }

    public void setTrf_type(String trf_type) {
        this.trf_type = trf_type;
    }

    public String getTrf_cur_id() {
        return trf_cur_id;
    }

    public void setTrf_cur_id(String trf_cur_id) {
        this.trf_cur_id = trf_cur_id;
    }

    public String getTrf_balance() {
        return trf_balance;
    }

    public void setTrf_balance(String trf_balance) {
        this.trf_balance = trf_balance;
    }

    public String getTrf_amount() {
        return trf_amount;
    }

    public void setTrf_amount(String trf_amount) {
        this.trf_amount = trf_amount;
    }

    public String getTrf_code() {
        return trf_code;
    }

    public void setTrf_code(String trf_code) {
        this.trf_code = trf_code;
    }

    public String getTrf_label() {
        return trf_label;
    }

    public void setTrf_label(String trf_label) {
        this.trf_label = trf_label;
    }

    public String getTrf_firstuse() {
        return trf_firstuse;
    }

    public void setTrf_firstuse(String trf_firstuse) {
        this.trf_firstuse = trf_firstuse;
    }

    public String getTrf_lastuse() {
        return trf_lastuse;
    }

    public void setTrf_lastuse(String trf_lastuse) {
        this.trf_lastuse = trf_lastuse;
    }

    public String getTrf_countuse() {
        return trf_countuse;
    }

    public void setTrf_countuse(String trf_countuse) {
        this.trf_countuse = trf_countuse;
    }

    public String getTrf_expiry() {
        return trf_expiry;
    }

    public void setTrf_expiry(String trf_expiry) {
        this.trf_expiry = trf_expiry;
    }

    public String getTrf_src_client_name() {
        return trf_src_client_name;
    }

    public void setTrf_src_client_name(String trf_src_client_name) {
        this.trf_src_client_name = trf_src_client_name;
    }

    public String getTrf_src_client_number() {
        return trf_src_client_number;
    }

    public void setTrf_src_client_number(String trf_src_client_number) {
        this.trf_src_client_number = trf_src_client_number;
    }

    public String getTrf_src_client_firstname() {
        return trf_src_client_firstname;
    }

    public void setTrf_src_client_firstname(String trf_src_client_firstname) {
        this.trf_src_client_firstname = trf_src_client_firstname;
    }

    public String getTrf_src_client_cou_id() {
        return trf_src_client_cou_id;
    }

    public void setTrf_src_client_cou_id(String trf_src_client_cou_id) {
        this.trf_src_client_cou_id = trf_src_client_cou_id;
    }

    public String getTrf_dst_client_name() {
        return trf_dst_client_name;
    }

    public void setTrf_dst_client_name(String trf_dst_client_name) {
        this.trf_dst_client_name = trf_dst_client_name;
    }

    public String getTrf_dst_client_firstname() {
        return trf_dst_client_firstname;
    }

    public void setTrf_dst_client_firstname(String trf_dst_client_firstname) {
        this.trf_dst_client_firstname = trf_dst_client_firstname;
    }

    public String getTrf_dst_client_number() {
        return trf_dst_client_number;
    }

    public void setTrf_dst_client_number(String trf_dst_client_number) {
        this.trf_dst_client_number = trf_dst_client_number;
    }

    public String getTrf_dst_client_cou_id() {
        return trf_dst_client_cou_id;
    }

    public void setTrf_dst_client_cou_id(String trf_dst_client_cou_id) {
        this.trf_dst_client_cou_id = trf_dst_client_cou_id;
    }

    public String getTrf_carrier() {
        return trf_carrier;
    }

    public void setTrf_carrier(String trf_carrier) {
        this.trf_carrier = trf_carrier;
    }

    public String getTrf_param() {
        return trf_param;
    }

    public void setTrf_param(String trf_param) {
        this.trf_param = trf_param;
    }

    public String getTrf_src_acc_id() {
        return trf_src_acc_id;
    }

    public void setTrf_src_acc_id(String trf_src_acc_id) {
        this.trf_src_acc_id = trf_src_acc_id;
    }

    public String getTrx_id() {
        return trx_id;
    }

    public void setTrx_id(String trx_id) {
        this.trx_id = trx_id;
    }

    public String getTrx_reversal_id() {
        return trx_reversal_id;
    }

    public void setTrx_reversal_id(String trx_reversal_id) {
        this.trx_reversal_id = trx_reversal_id;
    }

    public String getTrx_type() {
        return trx_type;
    }

    public void setTrx_type(String trx_type) {
        this.trx_type = trx_type;
    }

    public String getTrx_datetime() {
        return trx_datetime;
    }

    public void setTrx_datetime(String trx_datetime) {
        this.trx_datetime = trx_datetime;
    }

    public String getTrx_amount() {
        return trx_amount;
    }

    public void setTrx_amount(String trx_amount) {
        this.trx_amount = trx_amount;
    }

    public String getTrx_cur_id() {
        return trx_cur_id;
    }

    public void setTrx_cur_id(String trx_cur_id) {
        this.trx_cur_id = trx_cur_id;
    }

    public String getTrx_label() {
        return trx_label;
    }

    public void setTrx_label(String trx_label) {
        this.trx_label = trx_label;
    }

    public String getTrx_params() {
        return trx_params;
    }

    public void setTrx_params(String trx_params) {
        this.trx_params = trx_params;
    }

    public String getTrx_Stringitude() {
        return trx_Stringitude;
    }

    public void setTrx_Stringitude(String trx_Stringitude) {
        this.trx_Stringitude = trx_Stringitude;
    }

    public String getTrx_latitude() {
        return trx_latitude;
    }

    public void setTrx_latitude(String trx_latitude) {
        this.trx_latitude = trx_latitude;
    }

    public String getTrx_auth_mode() {
        return trx_auth_mode;
    }

    public void setTrx_auth_mode(String trx_auth_mode) {
        this.trx_auth_mode = trx_auth_mode;
    }

    public String getTrx_media() {
        return trx_media;
    }

    public void setTrx_media(String trx_media) {
        this.trx_media = trx_media;
    }

    public String getTrx_position_type() {
        return trx_position_type;
    }

    public void setTrx_position_type(String trx_position_type) {
        this.trx_position_type = trx_position_type;
    }

    public String getTrx_external_ref() {
        return trx_external_ref;
    }

    public void setTrx_external_ref(String trx_external_ref) {
        this.trx_external_ref = trx_external_ref;
    }
}
