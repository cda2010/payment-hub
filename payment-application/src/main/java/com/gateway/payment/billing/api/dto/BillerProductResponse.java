package com.gateway.payment.billing.api.dto;

import com.gateway.payment.billing.domain.AmountPolicy;
import com.gateway.payment.billing.domain.JourneyCode;
import com.gateway.payment.billing.domain.LookupMode;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record BillerProductResponse(
        UUID id,
        UUID billerId,
        String billerCode,
        String code,
        String name,
        String description,
        boolean actif,
        LookupMode lookupMode,
        AmountPolicy amountPolicy,
        BigDecimal minAmount,
        BigDecimal maxAmount,
        boolean allowOverpay,
        JourneyCode journeyCode,
        UUID paymentFlowId,
        Integer ordreAffichage,
        UUID providerId,
        String providerCode,
        String providerLibelle,
        UUID resolvedProviderId,
        String resolvedProviderCode,
        List<ProductFieldDto> lookupFields,
        List<ProductFieldDto> confirmFields,
        List<ProductFieldDto> amountFields
) {}
