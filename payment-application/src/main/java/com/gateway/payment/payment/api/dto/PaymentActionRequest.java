package com.gateway.payment.payment.api.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public record PaymentActionRequest(
        @NotBlank String action,
        Map<String, Object> data
) {}
