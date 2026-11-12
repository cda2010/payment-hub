package com.gateway.payment.configuration.api.dto;

import com.gateway.payment.common.domain.ExecutionMode;
import com.gateway.payment.common.domain.PaymentMethodType;
import com.gateway.payment.common.domain.ServiceTypeCode;
import com.gateway.payment.configuration.domain.FseParticipantKind;
import com.gateway.payment.configuration.domain.FseStatut;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ProviderRequest(
        @NotBlank @Size(max = 50) String code,
        @NotBlank @Size(max = 150) String libelle,
        String description,
        String logoUrl,
        boolean actif,
        Integer ordreAffichage,
        @NotNull PaymentMethodType paymentMethodType,
        @NotNull ExecutionMode modeExecution,
        /** Types de service supportés (vide = aucun). */
        List<ServiceTypeCode> serviceTypes,
        @Size(max = 20) String codeFse,
        @Size(max = 50) String codeExterne,
        @Size(max = 50) String groupCode,
        /** Nom de pays omnicanal (ex. Burkina Faso) ou code court pour une fiche locale. */
        @Size(max = 30) String countryCode,
        FseParticipantKind participantKind,
        FseStatut fseStatut
) {}
