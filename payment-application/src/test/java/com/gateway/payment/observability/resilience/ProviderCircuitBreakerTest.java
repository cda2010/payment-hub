package com.gateway.payment.observability.resilience;

import com.gateway.payment.common.domain.PaymentStatus;
import com.gateway.payment.provider.ProviderExecutionResult;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProviderCircuitBreakerTest {

    private ProviderCircuitBreaker circuitBreaker;

    @BeforeEach
    void setUp() {
        ProviderResilienceProperties props = new ProviderResilienceProperties();
        props.setEnabled(true);
        props.setFailureThreshold(4);
        props.setFailureRateThreshold(50f);
        props.setOpenDurationMs(60_000L);
        props.setHalfOpenPermittedCalls(1);
        circuitBreaker = new ProviderCircuitBreaker(
                CircuitBreakerRegistry.ofDefaults(),
                props,
                (com.gateway.payment.observability.metrics.GatewayMetrics) null
        );
    }

    @Test
    void circuitKey_isPerProviderAndMethod() {
        assertEquals(
                "provider:CORIS_BANK:op:TRANSFERT",
                ProviderCircuitBreaker.circuitKey("CORIS_BANK", "TRANSFERT")
        );
        assertEquals(
                "provider:CORIS_BANK:op:CONSULTATIONSOLDE",
                ProviderCircuitBreaker.circuitKey("CORIS_BANK", "CONSULTATIONSOLDE")
        );
    }

    @Test
    void forceOpen_onOneMethod_doesNotOpenAnother() {
        circuitBreaker.forceOpen("CORIS_BANK", "TRANSFERT");
        assertTrue(circuitBreaker.isOpen("CORIS_BANK", "TRANSFERT"));
        assertFalse(circuitBreaker.isOpen("CORIS_BANK", "CONSULTATIONSOLDE"));
        assertEquals(CircuitBreaker.State.CLOSED.name(), circuitBreaker.state("CORIS_BANK", "CONSULTATIONSOLDE"));
    }

    @Test
    void reset_closesForcedOpenMethod() {
        circuitBreaker.forceOpen("CORIS_MONEY", "TRANSFERT");
        circuitBreaker.reset("CORIS_MONEY", "TRANSFERT");
        assertFalse(circuitBreaker.isOpen("CORIS_MONEY", "TRANSFERT"));
        assertEquals(CircuitBreaker.State.CLOSED.name(), circuitBreaker.state("CORIS_MONEY", "TRANSFERT"));
    }

    @Test
    void infraFailures_openOnlyThatMethodCircuit() {
        CircuitBreaker cb = circuitBreaker.of("TEST_PROVIDER", "TRANSFERT");
        for (int i = 0; i < 4; i++) {
            try {
                cb.executeSupplier(() -> {
                    throw new ProviderInfrastructureException("TEST_PROVIDER", "timeout");
                });
            } catch (ProviderInfrastructureException ignored) {
                // expected
            }
        }
        assertTrue(circuitBreaker.isOpen("TEST_PROVIDER", "TRANSFERT")
                || cb.getState() == CircuitBreaker.State.OPEN);
        assertFalse(circuitBreaker.isOpen("TEST_PROVIDER", "CONSULTATIONSOLDE"));
    }

    @Test
    void isInfrastructureFailure_detectsTimeoutAnd5xx() {
        assertTrue(ProviderCircuitBreaker.isInfrastructureFailure(
                ProviderExecutionResult.builder().success(false).timedOut(true).build()));
        assertTrue(ProviderCircuitBreaker.isInfrastructureFailure(
                ProviderExecutionResult.builder().success(false).httpStatus(502).build()));
        assertFalse(ProviderCircuitBreaker.isInfrastructureFailure(
                ProviderExecutionResult.builder()
                        .success(false)
                        .httpStatus(400)
                        .gatewayStatus(PaymentStatus.FAILED)
                        .build()));
    }
}
