package com.gateway.payment.observability.resilience;

public class ProviderInfrastructureException extends RuntimeException {

    private final String providerCode;

    public ProviderInfrastructureException(String providerCode, String message) {
        super(message);
        this.providerCode = providerCode;
    }

    public String getProviderCode() {
        return providerCode;
    }
}
