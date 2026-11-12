package com.m2i.omnicanal.application.security;

/**
 * Constants for Spring Security authorities.
 */
public final class AuthoritiesConstants {

    public static final String ADMIN = "ROLE_ADMIN";

    public static final String USER = "ROLE_USER";

    public static final String ANONYMOUS = "ROLE_ANONYMOUS";

    public static final String CONSULT_TDB = "ROLE_CONSULT_TDB";

    private AuthoritiesConstants() {
    }
}
