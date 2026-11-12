package com.gateway.payment.transfer.api.dto;

import com.gateway.payment.transfer.domain.RailScope;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record TransferRailRequest(
        @NotBlank String code,
        @NotBlank String name,
        String description,
        @NotNull RailScope railScope,
        String currency,
        BigDecimal minAmount,
        BigDecimal maxAmount,
        Boolean requiresBank,
        Boolean sameCountryOnly,
        Boolean sameGroupOnly,
        Boolean crossBorderOnly,
        List<TransferFieldDto> extraFields,
        @NotNull Boolean actif,
        Integer ordre
) {}
