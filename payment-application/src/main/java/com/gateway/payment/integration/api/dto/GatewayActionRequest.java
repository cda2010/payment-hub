package com.gateway.payment.integration.api.dto;

import java.util.Map;

/**
 * Corps d'action mid-flow : {@code SUBMIT_FORM}, {@code SUBMIT_OTP}, {@code APPROVE}, {@code REJECT}, …
 */
public record GatewayActionRequest(
        String action,
        Map<String, Object> data
) {
}
