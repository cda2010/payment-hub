package com.gateway.payment.transfer.api.dto;

import com.gateway.payment.transfer.domain.RailScope;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record TransferRailResponse(
        UUID id,
        String code,
        String name,
        String description,
        RailScope railScope,
        String currency,
        BigDecimal minAmount,
        BigDecimal maxAmount,
        boolean requiresBank,
        boolean sameCountryOnly,
        boolean sameGroupOnly,
        boolean crossBorderOnly,
        List<TransferFieldDto> extraFields,
        boolean actif,
        Integer ordre,
        String displayLabel,
        String etaLabel,
        String warning
) {}
