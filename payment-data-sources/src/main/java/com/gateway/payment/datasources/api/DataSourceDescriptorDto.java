package com.gateway.payment.datasources.api;

/**
 * Descripteur d'une source enregistrée (inventaire designer RapidFrame).
 */
public record DataSourceDescriptorDto(
        String code,
        String label,
        String defaultOrigin,
        boolean localHandler
) {}
