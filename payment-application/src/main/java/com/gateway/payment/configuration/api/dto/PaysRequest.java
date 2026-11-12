package com.gateway.payment.configuration.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PaysRequest(
        @NotBlank @Size(max = 8) String code,
        @NotBlank @Size(max = 80) String nom,
        @NotBlank @Size(max = 8) String indicatif,
        @NotBlank @Size(max = 10) String devise,
        boolean actif,
        Integer ordre
) {}
