package com.gateway.payment.configuration.api.dto;

public record ConnectivityTestResponse(
        boolean success,
        String message,
        Long latencyMs
) {}
