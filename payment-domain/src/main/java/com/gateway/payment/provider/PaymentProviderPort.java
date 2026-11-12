package com.gateway.payment.provider;

import java.util.Map;

public interface PaymentProviderPort {

    String getCode();

    ProviderExecutionResult execute(PaymentContext context, String operationCode, Map<String, Object> input);
}
