package com.gateway.payment.billing.api.dto;



import com.gateway.payment.billing.domain.AmountPolicy;

import com.gateway.payment.billing.domain.JourneyCode;



import java.math.BigDecimal;



public record BillDto(

        String id,

        String billerCode,

        String billerName,

        String productCode,

        String productName,

        String billReference,

        String subscriberReference,

        BigDecimal amountDue,

        String currency,

        String dueDate,

        String status,

        String label,

        String customerName,

        AmountPolicy amountPolicy,

        BigDecimal minAmount,

        BigDecimal maxAmount,

        boolean allowOverpay,

        JourneyCode journeyCode

) {}


