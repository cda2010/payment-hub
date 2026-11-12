package com.m2i.pti.service.dto;

import java.util.List;
import java.util.Set;

public class CredentialApiDTO {
    private Long id;
    private String resource_ids;
    private String client_secret;
    private String scope;
    private String authorized_grant_types;
    private String web_server_redirect_uri;
    private String authorities;
    private Integer access_token_validity;
    private Integer refresh_token_validity;
    private String additional_information;
    private String autoapprove;
    private String client_id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getResourceIds() {
        return resource_ids;
    }

    public void setResourceIds(String resource_ids) {
        this.resource_ids = resource_ids;
    }

    public String getClientSecret() {
        return client_secret;
    }

    public void setClientSecret(String client_secret) {
        this.client_secret = client_secret;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getAuthorizedGrantTypes() {
        return authorized_grant_types;
    }

    public void setAuthorizedGrantTypes(String authorized_grant_types) {
        this.authorized_grant_types = authorized_grant_types;
    }

    public String getWebServerRedirect_uri() {
        return web_server_redirect_uri;
    }

    public void setWebServerRedirectUri(String web_server_redirect_uri) {
        this.web_server_redirect_uri = web_server_redirect_uri;
    }

    public String getAuthorities() {
        return authorities;
    }

    public void setAuthorities(String authorities) {
        this.authorities = authorities;
    }

    public Integer getAccessTokenValidity() {
        return access_token_validity;
    }

    public void setAccessTokenValidity(Integer access_token_validity) {
        this.access_token_validity = access_token_validity;
    }

    public Integer getRefreshTtokenValidity() {
        return refresh_token_validity;
    }

    public void setRefreshTokenValidity(Integer refresh_token_validity) {
        this.refresh_token_validity = refresh_token_validity;
    }

    public String getAdditionalInformation() {
        return additional_information;
    }

    public void setAdditionalInformation(String additional_information) {
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
}
