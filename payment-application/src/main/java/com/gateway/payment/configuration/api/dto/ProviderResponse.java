package com.gateway.payment.configuration.api.dto;

import com.gateway.payment.common.domain.ExecutionMode;
import com.gateway.payment.common.domain.PaymentMethodType;
import com.gateway.payment.common.domain.ServiceTypeCode;
import com.gateway.payment.configuration.domain.FseParticipantKind;
import com.gateway.payment.configuration.domain.FseStatut;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ProviderResponse(
        UUID id,
        String code,
        String libelle,
        String description,
        String logoUrl,
        boolean actif,
        Integer ordreAffichage,
        PaymentMethodType paymentMethodType,
        ExecutionMode modeExecution,
        List<ServiceTypeCode> serviceTypes,
        String codeFse,
        String codeExterne,
        String groupCode,
        String countryCode,
        FseParticipantKind participantKind,
        FseStatut fseStatut,
        Instant createdAt,
        Instant updatedAt
) {}
