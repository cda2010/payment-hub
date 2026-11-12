package com.gateway.payment.billing.connector.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Canonical fixed contract for LOOKUP_BILL / BILL_LIST provider responses.
 * Replaces declarative {@code InterfaceUtilisateur} RESPONSE mapping — connectors normalize
 * into this shape; the payment engine applies it via {@code BillResponseSupport}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record BillResponse(
        String code,
        String status,
        String providerReference,
        String reference,
        Integer count,
        List<Bill> bills
) {
    public BillResponse {
        bills = bills != null ? List.copyOf(bills) : List.of();
    }

    public int billCount() {
        return count != null ? count : bills.size();
    }

    public boolean isSuccess() {
        String c = code != null ? code.trim() : status;
        return c == null || "00".equals(c) || "0".equals(c) || "OK".equalsIgnoreCase(c);
    }
}
