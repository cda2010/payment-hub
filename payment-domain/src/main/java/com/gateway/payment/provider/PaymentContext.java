package com.gateway.payment.provider;

import lombok.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentContext {

    private String paymentId;
    private String providerCode;
    private BigDecimal amount;
    private String currency;
    private String phone;
    private String customerReference;
    private String merchantReference;
    private String providerReference;
    private String currentStep;

    @Builder.Default
    private Map<String, Object> references = new HashMap<>();

    @Builder.Default
    private Map<String, Object> context = new HashMap<>();

    @Builder.Default
    private Map<String, Object> attributes = new HashMap<>();
}
