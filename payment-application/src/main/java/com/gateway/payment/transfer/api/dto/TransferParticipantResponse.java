package com.gateway.payment.transfer.api.dto;

import com.gateway.payment.transfer.domain.ParticipantKind;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record TransferParticipantResponse(
        UUID id,
        String code,
        String name,
        String countryCode,
        boolean actif,
        boolean canEmit,
        boolean canReceive,
        ParticipantKind participantKind,
        String groupCode,
        BigDecimal minAmount,
        BigDecimal maxAmount,
        List<TransferFieldDto> emitterFields,
        List<TransferFieldDto> beneficiaryFields,
        Integer ordreAffichage,
        UUID providerId
) {}
