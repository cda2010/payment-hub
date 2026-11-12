package com.gateway.payment.billing.api.dto;

import com.gateway.payment.billing.domain.AmountPolicy;
import com.gateway.payment.billing.domain.JourneyCode;
import com.gateway.payment.billing.domain.LookupMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record BillerProductRequest(
        @NotBlank @Size(max = 50) String code,
        @NotBlank @Size(max = 150) String name,
        String description,
        @NotNull Boolean actif,
        @NotNull LookupMode lookupMode,
        @NotNull AmountPolicy amountPolicy,
        BigDecimal minAmount,
        BigDecimal maxAmount,
        Boolean allowOverpay,
        @NotNull JourneyCode journeyCode,
        UUID paymentFlowId,
        Integer ordreAffichage,
        /** Override optionnel — sinon hérite du facturier. */
        UUID providerId,
        List<ProductFieldDto> lookupFields,
        List<ProductFieldDto> confirmFields,
        List<ProductFieldDto> amountFields
) {}
