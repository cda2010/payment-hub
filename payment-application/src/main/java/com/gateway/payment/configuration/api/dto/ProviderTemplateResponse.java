package com.gateway.payment.configuration.api.dto;

import com.gateway.payment.common.domain.ExecutionMode;
import com.gateway.payment.common.domain.PaymentMethodType;

public record ProviderTemplateResponse(
        String code,
        String libelle,
        String description,
        PaymentMethodType paymentMethodType,
        ExecutionMode modeExecution,
        String defaultBaseUrl
) {}
