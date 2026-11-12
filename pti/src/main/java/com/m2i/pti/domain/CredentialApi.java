package com.m2i.pti.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A CredentialApi.
 */
@Entity
@Table(name = "credential_api")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class CredentialApi extends AbstractAuditingEntity  implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "resource_ids")
    private String resource_ids;

    @Column(name = "client_secret")
    private String client_secret;

    @Column(name = "jhi_scope")
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

    @Column(name = "client_id")
    private String client_id;

    @ManyToOne
    private UniteOrganisation organisationMarchand;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getResource_ids() {
        return resource_ids;
    }

    public CredentialApi resource_ids(String resource_ids) {
        this.resource_ids = resource_ids;
        return this;
    }

    public void setResource_ids(String resource_ids) {
        this.resource_ids = resource_ids;
    }

    public String getClient_secret() {
        return client_secret;
    }

    public CredentialApi client_secret(String client_secret) {
        this.client_secret = client_secret;
        return this;
    }

    public void setClient_secret(String client_secret) {
        this.client_secret = client_secret;
    }

    public String getScope() {
        return scope;
    }

    public CredentialApi scope(String scope) {
        this.scope = scope;
        return this;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getAuthorized_grant_types() {
        return authorized_grant_types;
    }

    public CredentialApi authorized_grant_types(String authorized_grant_types) {
        this.authorized_grant_types = authorized_grant_types;
        return this;
    }

    public void setAuthorized_grant_types(String authorized_grant_types) {
        this.authorized_grant_types = authorized_grant_types;
    }

    public String getWeb_server_redirect_uri() {
        return web_server_redirect_uri;
    }

    public CredentialApi web_server_redirect_uri(String web_server_redirect_uri) {
        this.web_server_redirect_uri = web_server_redirect_uri;
        return this;
    }

    public void setWeb_server_redirect_uri(String web_server_redirect_uri) {
        this.web_server_redirect_uri = web_server_redirect_uri;
    }

    public String getAuthorities() {
        return authorities;
    }

    public CredentialApi authorities(String authorities) {
        this.authorities = authorities;
        return this;
    }

    public void setAuthorities(String authorities) {
        this.authorities = authorities;
    }

    public Integer getAccess_token_validity() {
        return access_token_validity;
    }

    public CredentialApi access_token_validity(Integer access_token_validity) {
        this.access_token_validity = access_token_validity;
        return this;
    }

    public void setAccess_token_validity(Integer access_token_validity) {
        this.access_token_validity = access_token_validity;
    }

    public Integer getRefresh_token_validity() {
        return refresh_token_validity;
    }

    public CredentialApi refresh_token_validity(Integer refresh_token_validity) {
        this.refresh_token_validity = refresh_token_validity;
        return this;
    }

    public void setRefresh_token_validity(Integer refresh_token_validity) {
        this.refresh_token_validity = refresh_token_validity;
    }

    public String getAdditional_information() {
        return additional_information;
    }

    public CredentialApi additional_information(String additional_information) {
        this.additional_information = additional_information;
        return this;
    }

    public void setAdditional_information(String additional_information) {
        this.additional_information = additional_information;
    }

    public String getAutoapprove() {
        return autoapprove;
    }

    public CredentialApi autoapprove(String autoapprove) {
        this.autoapprove = autoapprove;
        return this;
    }

    public void setAutoapprove(String autoapprove) {
        this.autoapprove = autoapprove;
    }

    public String getClient_id() {
        return client_id;
    }

    public CredentialApi client_id(String client_id) {
        this.client_id = client_id;
        return this;
    }

    public void setClient_id(String client_id) {
        this.client_id = client_id;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove


    public UniteOrganisation getOrganisationMarchand() {
        return organisationMarchand;
    }

    public void setOrganisationMarchand(UniteOrganisation organisationMarchand) {
        this.organisationMarchand = organisationMarchand;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CredentialApi credentialApi = (CredentialApi) o;
        if (credentialApi.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), credentialApi.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "CredentialApi{" +
            "id=" + getId() +
            ", resource_ids='" + getResource_ids() + "'" +
            ", client_secret='" + getClient_secret() + "'" +
            ", scope='" + getScope() + "'" +
            ", authorized_grant_types='" + getAuthorized_grant_types() + "'" +
            ", web_server_redirect_uri='" + getWeb_server_redirect_uri() + "'" +
            ", authorities='" + getAuthorities() + "'" +
            ", access_token_validity='" + getAccess_token_validity() + "'" +
            ", refresh_token_validity='" + getRefresh_token_validity() + "'" +
            ", additional_information='" + getAdditional_information() + "'" +
            ", autoapprove='" + getAutoapprove() + "'" +
            ", client_id='" + getClient_id() + "'" +
            "}";
    }
}
