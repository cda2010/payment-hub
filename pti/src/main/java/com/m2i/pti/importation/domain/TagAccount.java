package com.m2i.pti.importation.domain;

import com.univocity.parsers.annotations.Parsed;
import com.univocity.parsers.annotations.Trim;

/**
 * @author O.soufiane
 */

public class TagAccount {
    @Trim
    @Parsed(field = "acc_id")
    private String acc_id;
    /**
     * Libelle du compte.
     */
    @Trim
    @Parsed(field = "acc_label")
    private String acc_label;
    /**
     * Statut du compte.
     */
    @Trim
    @Parsed(field = "acc_status")
    private String acc_status;
    /**
     * Identifiant du titulaire du compte.
     */
    @Trim
    @Parsed(field = "acc_idt_id")
    private String acc_idt_id;
    @Trim
    @Parsed(field = "acc_mer_id")
    private String acc_mer_id;
    @Trim
    @Parsed(field = "acc_cli_id")
    private String acc_cli_id;
    @Trim
    @Parsed(field = "acc_agt_id")
    private String acc_agt_id;
    @Trim
    @Parsed(field = "acc_cur_id")
    private String acc_cur_id;
    /**
     * Solde du compte.
     */
    @Trim
    @Parsed(field = "acc_balance")
    private String acc_balance;
    @Trim
    @Parsed(field = "acc_firstuse")
    private String acc_firstuse;
    @Trim
    @Parsed(field = "acc_lastuse")
    private String acc_lastuse;
    @Trim
    @Parsed(field = "acc_countuse")
    private String acc_countuse;
    @Trim
    @Parsed(field = "acc_bank_account")
    private String acc_bank_account;
    /***
     * Numero du compte.
     */
    @Trim
    @Parsed(field = "acc_number")
    private long acc_number;
    @Trim
    @Parsed(field = "acc_pro_id")
    private String acc_pro_id;

    @Trim
    @Parsed(field = "pro_type")
    private String acc_pro_type;

    @Trim
    @Parsed(field = "acc_settlementdatetime")
    private String acc_settlementdatetime;
    @Trim
    @Parsed(field = "acc_settlement_acc_id")
    private String acc_settlement_acc_id;

    public String getAcc_id() {
        return acc_id;
    }

    public void setAcc_id(String acc_id) {
        this.acc_id = acc_id;
    }

    public String getAcc_label() {
        return acc_label;
    }

    public void setAcc_label(String acc_label) {
        this.acc_label = acc_label;
    }

    public String getAcc_status() {
        return acc_status;
    }

    public void setAcc_status(String acc_status) {
        this.acc_status = acc_status;
    }

    public String getAcc_idt_id() {
        return acc_idt_id;
    }

    public void setAcc_idt_id(String acc_idt_id) {
        this.acc_idt_id = acc_idt_id;
    }

    public String getAcc_mer_id() {
        return acc_mer_id;
    }

    public void setAcc_mer_id(String acc_mer_id) {
        this.acc_mer_id = acc_mer_id;
    }

    public String getAcc_cli_id() {
        return acc_cli_id;
    }

    public void setAcc_cli_id(String acc_cli_id) {
        this.acc_cli_id = acc_cli_id;
    }

    public String getAcc_agt_id() {
        return acc_agt_id;
    }

    public void setAcc_agt_id(String acc_agt_id) {
        this.acc_agt_id = acc_agt_id;
    }

    public String getAcc_cur_id() {
        return acc_cur_id;
    }

    public void setAcc_cur_id(String acc_cur_id) {
        this.acc_cur_id = acc_cur_id;
    }

    public String getAcc_balance() {
        return acc_balance;
    }

    public void setAcc_balance(String acc_balance) {
        this.acc_balance = acc_balance;
    }

    public String getAcc_firstuse() {
        return acc_firstuse;
    }

    public void setAcc_firstuse(String acc_firstuse) {
        this.acc_firstuse = acc_firstuse;
    }

    public String getAcc_lastuse() {
        return acc_lastuse;
    }

    public void setAcc_lastuse(String acc_lastuse) {
        this.acc_lastuse = acc_lastuse;
    }

    public String getAcc_countuse() {
        return acc_countuse;
    }

    public void setAcc_countuse(String acc_countuse) {
        this.acc_countuse = acc_countuse;
    }

    public String getAcc_bank_account() {
        return acc_bank_account;
    }

    public void setAcc_bank_account(String acc_bank_account) {
        this.acc_bank_account = acc_bank_account;
    }

    public long getAcc_number() {
        return acc_number;
    }

    public void setAcc_number(long acc_number) {
        this.acc_number = acc_number;
    }

    public String getAcc_pro_id() {
        return acc_pro_id;
    }

    public void setAcc_pro_id(String acc_pro_id) {
        this.acc_pro_id = acc_pro_id;
    }

    public String getAcc_settlementdatetime() {
        return acc_settlementdatetime;
    }

    public void setAcc_settlementdatetime(String acc_settlementdatetime) {
        this.acc_settlementdatetime = acc_settlementdatetime;
    }

    public String getAcc_settlement_acc_id() {
        return acc_settlement_acc_id;
    }

    public void setAcc_settlement_acc_id(String acc_settlement_acc_id) {
        this.acc_settlement_acc_id = acc_settlement_acc_id;
    }

    public String getAcc_pro_type() {
        return acc_pro_type;
    }

    public void setAcc_pro_type(String acc_pro_type) {
        this.acc_pro_type = acc_pro_type;
    }
}
