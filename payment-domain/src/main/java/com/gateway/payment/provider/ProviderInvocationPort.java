package com.gateway.payment.provider;

import com.gateway.payment.payment.domain.PaymentTransaction;

import java.util.Map;

/**
 * Port d'invocation provider — implémenté dans {@code payment-application}
 * pour que {@code payment-orchestration} n'y dépende pas.
 */
public interface ProviderInvocationPort {

    ProviderExecutionResult call(
            PaymentTransaction tx,
            String stepCode,
            String operationCode,
            Map<String, Object> input,
            Map<String, Object> contextAttributes
    );

    ProviderExecutionResult callForProvider(
            PaymentTransaction tx,
            String providerCode,
            String stepCode,
            String operationCode,
            Map<String, Object> input,
            Map<String, Object> contextAttributes
    );
}
