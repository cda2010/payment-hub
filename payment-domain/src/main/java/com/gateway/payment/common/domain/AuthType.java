package com.gateway.payment.common.domain;

public enum AuthType {
    NONE, BASIC, API_KEY, BEARER_TOKEN, OAUTH2_CLIENT_CREDENTIALS, CUSTOM_HEADER, MTLS
}
