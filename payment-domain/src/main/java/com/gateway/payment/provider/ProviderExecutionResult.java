package com.gateway.payment.provider;

import com.gateway.payment.common.domain.PaymentStatus;
import lombok.*;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProviderExecutionResult {

    private boolean success;
    private Integer httpStatus;
    private String providerStatus;
    private String providerMessage;
    private String providerReference;
    private PaymentStatus gatewayStatus;

    @Builder.Default
    private Map<String, Object> responseBody = new HashMap<>();

    private String rawResponse;
    private Long durationMs;
    private boolean timedOut;
}
