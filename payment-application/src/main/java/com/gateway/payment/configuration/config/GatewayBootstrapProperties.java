package com.gateway.payment.configuration.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gateway.bootstrap")
public record GatewayBootstrapProperties(
        boolean providers
) {}
