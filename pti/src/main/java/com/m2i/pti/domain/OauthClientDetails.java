package com.m2i.pti.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "oauth_client_details")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class OauthClientDetails implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "client_id")
    private String client_id;

    @Column(name = "resource_ids")
    private String resource_ids;

    @Column(name = "client_secret")
    private String client_secret;

    @Column(name = "scope")
    private String scope;

    @Column(name = "authorized_grant_types")
    private String authorized_grant_types;

    @Column(name = "web_server_redirect_uri")
    private String web_server_redirect_uri;

    @Column(name = "authorities")
    private String authorities;

    @Column(name = "access_token_validity")
    private Integer access_token_validity;

    @Column(name = "refresh_token_validity")
    private Integer refresh_token_validity;

    @Column(name = "additional_information")
    private String additional_information;

    @Column(name = "autoapprove")
    private String autoapprove;

    @Column(name = "adresse_authorise")
    private String adresse_authorise;

    @ManyToOne
    private UniteOrganisation uniteOrganisation;

    @ManyToOne
    private Profil profil;


    public String getResource_ids() {
        return resource_ids;
    }

    public void setResource_ids(String resource_ids) {
        this.resource_ids = resource_ids;
    }

    public String getClient_secret() {
        return client_secret;
    }

    public void setClient_secret(String client_secret) {
        this.client_secret = client_secret;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getAuthorized_grant_types() {
        return authorized_grant_types;
    }

    public void setAuthorized_grant_types(String authorized_grant_types) {
        this.authorized_grant_types = authorized_grant_types;
    }

    public String getWeb_server_redirect_uri() {
        return web_server_redirect_uri;
    }

    public void setWeb_server_redirect_uri(String web_server_redirect_uri) {
        this.web_server_redirect_uri = web_server_redirect_uri;
    }

    public String getAuthorities() {
        return authorities;
    }

    public void setAuthorities(String authorities) {
        this.authorities = authorities;
    }

    public Integer getAccess_token_validity() {
        return access_token_validity;
    }

    public void setAccess_token_validity(Integer access_token_validity) {
        this.access_token_validity = access_token_validity;
    }

    public Integer getRefresh_token_validity() {
        return refresh_token_validity;
    }

    public void setRefresh_token_validity(Integer refresh_token_validity) {
        this.refresh_token_validity = refresh_token_validity;
    }

    public String getAdditional_information() {
        return additional_information;
    }

    public void setAdditional_information(String additional_information) {
        this.additional_information = additional_information;
    }

    public String getAutoapprove() {
        return autoapprove;
    }

    public void setAutoapprove(String autoapprove) {
        this.autoapprove = autoapprove;
    }

    public String getClient_id() {
        return client_id;
    }

    public void setClient_id(String client_id) {
        this.client_id = client_id;
    }

    public String getAdresse_authorise() {
        return adresse_authorise;
    }

    public void setAdresse_authorise(String adresse_authorise) {
        this.adresse_authorise = adresse_authorise;
    }

    @Override
    public String toString() {
        return "OauthClientDetails{" +
                "resource_ids='" + resource_ids + '\'' +
                ", client_secret='" + client_secret + '\'' +
                ", scope='" + scope + '\'' +
                ", authorized_grant_types='" + authorized_grant_types + '\'' +
                ", web_server_redirect_uri='" + web_server_redirect_uri + '\'' +
                ", authorities='" + authorities + '\'' +
                ", access_token_validity=" + access_token_validity +
                ", refresh_token_validity=" + refresh_token_validity +
                ", additional_information='" + additional_information + '\'' +
                ", autoapprove='" + autoapprove + '\'' +
                ", client_id='" + client_id + '\'' +
                '}';
    }

    public UniteOrganisation getUniteOrganisation() {
        return uniteOrganisation;
    }

    public void setUniteOrganisation(UniteOrganisation uniteOrganisation) {
        this.uniteOrganisation = uniteOrganisation;
    }

    public Profil getProfil() {
        return profil;
    }

    public void setProfil(Profil profil) {
        this.profil = profil;
    }
}
