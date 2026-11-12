package com.gateway.payment.payment.api.dto;

import com.gateway.payment.common.domain.PaymentStatus;

import java.time.Instant;

public record TransactionSearchCriteria(
        String provider,
        PaymentStatus status,
        String paymentId,
        String customerReference,
        String providerReference,
        String phoneNumber,
        Instant from,
        Instant to
) {}
