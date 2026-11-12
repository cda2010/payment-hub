package com.gateway.payment.ops;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gateway.ops")
public record GatewayOpsProperties(
        boolean waitTimeoutEnabled,
        long waitTimeoutMinutes,
        boolean reconEnabled,
        long reconAfterMinutes
) {
    public GatewayOpsProperties {
        if (waitTimeoutMinutes <= 0) {
            waitTimeoutMinutes = 60;
        }
        if (reconAfterMinutes <= 0) {
            reconAfterMinutes = 5;
        }
    }
}
