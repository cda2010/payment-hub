package com.gateway.payment.configuration.api.dto;

import java.util.UUID;

public record PaysResponse(
        UUID id,
        String code,
        String nom,
        String indicatif,
        String devise,
        boolean actif,
        Integer ordre
) {}
