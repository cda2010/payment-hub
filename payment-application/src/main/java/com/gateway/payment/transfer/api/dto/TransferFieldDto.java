package com.gateway.payment.transfer.api.dto;

public record TransferFieldDto(
        String name,
        String label,
        String type,
        boolean required,
        String placeholder,
        String pattern,
        java.util.List<String> options
) {}

