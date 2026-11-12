package com.gateway.payment.billing.api.dto;

import com.gateway.payment.billing.domain.ProductFieldRole;

import java.util.List;

public record ProductFieldDto(
        String name,
        String label,
        String type,
        boolean required,
        String placeholder,
        String pattern,
        ProductFieldRole role,
        List<String> options,
        String defaultValue
) {}
