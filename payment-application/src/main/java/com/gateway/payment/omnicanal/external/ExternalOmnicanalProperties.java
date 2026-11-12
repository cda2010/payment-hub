package com.gateway.payment.omnicanal.external;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Connexion HTTP vers l'omnicanal externe (omnic-exp).
 * Indépendant de {@code gateway.catalog.omnicanal}, qui reste le moteur interne.
 */
@ConfigurationProperties(prefix = "gateway.omnicanal.external")
public class ExternalOmnicanalProperties {

    private boolean enabled = true;
    private String baseUrl = "http://localhost:5981";
    private String login = "admin";
    private String password = "";
    private String clientId = "Omnicanalapp";
    private String clientSecret = "";
    private String timeout = "PT30S";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getTimeout() {
        return timeout;
    }

    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }
}
