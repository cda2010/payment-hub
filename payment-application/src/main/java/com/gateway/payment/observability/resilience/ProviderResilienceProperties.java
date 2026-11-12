package com.gateway.payment.observability.resilience;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gateway.provider-resilience")
public class ProviderResilienceProperties {

    private boolean enabled = true;
    private int failureThreshold = 20;
    private float failureRateThreshold = 50f;
    private long openDurationMs = 10_000L;
    private int halfOpenPermittedCalls = 3;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getFailureThreshold() {
        return failureThreshold;
    }

    public void setFailureThreshold(int failureThreshold) {
        this.failureThreshold = failureThreshold;
    }

    public float getFailureRateThreshold() {
        return failureRateThreshold;
    }

    public void setFailureRateThreshold(float failureRateThreshold) {
        this.failureRateThreshold = failureRateThreshold;
    }

    public long getOpenDurationMs() {
        return openDurationMs;
    }

    public void setOpenDurationMs(long openDurationMs) {
        this.openDurationMs = openDurationMs;
    }

    public int getHalfOpenPermittedCalls() {
        return halfOpenPermittedCalls;
    }

    public void setHalfOpenPermittedCalls(int halfOpenPermittedCalls) {
        this.halfOpenPermittedCalls = halfOpenPermittedCalls;
    }
}
