package com.m2i.pti.config;

/**
 * L'authorization server Spring Security OAuth 2.0 (Boot 1.5) n'existe plus en Boot 3.
 * L'authentification du backoffice passe par le resource server JWT de {@link SecurityConfiguration}.
 */
public final class OAuth2ServerConfiguration {

    private OAuth2ServerConfiguration() {
    }
}
