package com.gateway.payment.transfer.api.dto;

import com.gateway.payment.transfer.domain.ParticipantKind;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record TransferParticipantRequest(
        @NotBlank String code,
        @NotBlank String name,
        @NotBlank String countryCode,
        @NotNull Boolean actif,
        Boolean canEmit,
        Boolean canReceive,
        ParticipantKind participantKind,
        String groupCode,
        BigDecimal minAmount,
        BigDecimal maxAmount,
        List<TransferFieldDto> emitterFields,
        List<TransferFieldDto> beneficiaryFields,
        Integer ordreAffichage,
        /** Lien optionnel vers payment_provider.id */
        UUID providerId
) {}
