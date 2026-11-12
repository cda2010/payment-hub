/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.importation.domain;

import com.univocity.parsers.annotations.Parsed;
import com.univocity.parsers.annotations.Trim;


import java.io.Serializable;

/**
 * @author O.Soufiane
 */

public class TagClient extends TagIdentiy implements Serializable {
    @Trim
    @Parsed(field = "dev_number")
    private String telephoneClient;
    @Trim
    @Parsed(field = "cli_idt_id")
    private String cli_idt_id;
    @Trim
    @Parsed(field = "cli_id")
    private String cli_id;
    @Trim
    @Parsed(field = "cli_cos_id")
    private String cli_cos_id;
    @Trim
    @Parsed(field = "cli_firstuse")
    private String cli_firstuse;
    @Trim
    @Parsed(field = "cli_job")
    private String cli_job;
    @Trim
    @Parsed(field = "cli_employer")
    private String cli_employer;
    @Trim
    @Parsed(field = "cli_type")
    private String cli_type;
    @Trim
    @Parsed(field = "cli_status")
    private String cli_status;

    public String getTelephoneClient() {
        return telephoneClient;
    }

    public void setTelephoneClient(String telephoneClient) {
        this.telephoneClient = telephoneClient;
    }

    public String getCli_idt_id() {
        return cli_idt_id;
    }

    public void setCli_idt_id(String cli_idt_id) {
        this.cli_idt_id = cli_idt_id;
    }

    public String getCli_id() {
        return cli_id;
    }

    public void setCli_id(String cli_id) {
        this.cli_id = cli_id;
    }

    public String getCli_cos_id() {
        return cli_cos_id;
    }

    public void setCli_cos_id(String cli_cos_id) {
        this.cli_cos_id = cli_cos_id;
    }

    public String getCli_firstuse() {
        return cli_firstuse;
    }

    public void setCli_firstuse(String cli_firstuse) {
        this.cli_firstuse = cli_firstuse;
    }

    public String getCli_job() {
        return cli_job;
    }

    public void setCli_job(String cli_job) {
        this.cli_job = cli_job;
    }

    public String getCli_employer() {
        return cli_employer;
    }

    public void setCli_employer(String cli_employer) {
        this.cli_employer = cli_employer;
    }

    public String getCli_type() {
        return cli_type;
    }

    public void setCli_type(String cli_type) {
        this.cli_type = cli_type;
    }

    public String getCli_status() {
        return cli_status;
    }

    public void setCli_status(String cli_status) {
        this.cli_status = cli_status;
    }
}
