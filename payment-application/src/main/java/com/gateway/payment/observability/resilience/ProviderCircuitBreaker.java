package com.gateway.payment.observability.resilience;

import com.gateway.payment.observability.metrics.GatewayMetrics;
import com.gateway.payment.provider.ProviderExecutionResult;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Circuit breaker Resilience4j — une instance par couple
 * <strong>fournisseur + méthode</strong> ({@code provider:CORIS_BANK:op:TRANSFERT}).
 * <p>
 * Une méthode indisponible n'ouvre pas le circuit des autres méthodes du même FSE.
 */
@Service
public class ProviderCircuitBreaker {

    private static final Logger log = LoggerFactory.getLogger(ProviderCircuitBreaker.class);

    private final CircuitBreakerRegistry registry;
    private final ProviderResilienceProperties properties;
    private final CircuitBreakerConfig config;
    private final GatewayMetrics gatewayMetrics;

    @Autowired
    public ProviderCircuitBreaker(
            CircuitBreakerRegistry registry,
            ProviderResilienceProperties properties,
            ObjectProvider<GatewayMetrics> gatewayMetrics
    ) {
        this(registry, properties, gatewayMetrics == null ? null : gatewayMetrics.getIfAvailable());
    }

    /** Constructeur package-private pour les tests unitaires. */
    ProviderCircuitBreaker(
            CircuitBreakerRegistry registry,
            ProviderResilienceProperties properties,
            GatewayMetrics gatewayMetrics
    ) {
        this.registry = registry;
        this.properties = properties;
        this.gatewayMetrics = gatewayMetrics;
        int window = Math.max(1, properties.getFailureThreshold());
        float rate = properties.getFailureRateThreshold();
        if (rate <= 0f || rate > 100f) {
            rate = 50f;
        }
        int halfOpen = Math.max(1, properties.getHalfOpenPermittedCalls());
        this.config = CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(window)
                .minimumNumberOfCalls(Math.max(1, Math.min(window, 10)))
                .failureRateThreshold(rate)
                .waitDurationInOpenState(Duration.ofMillis(Math.max(1L, properties.getOpenDurationMs())))
                .permittedNumberOfCallsInHalfOpenState(halfOpen)
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .recordException(ProviderCircuitBreaker::isRecordedFailure)
                .build();
        log.info("Provider circuit breaker (per method): window={} failureRate={}% openDurationMs={} halfOpen={}",
                window, rate, properties.getOpenDurationMs(), halfOpen);
    }

    public boolean isEnabled() {
        return properties.isEnabled();
    }

    public CircuitBreaker of(String providerCode, String operationCode) {
        String name = circuitKey(providerCode, operationCode);
        boolean created = registry.find(name).isEmpty();
        CircuitBreaker cb = registry.circuitBreaker(name, config);
        if (created) {
            cb.getEventPublisher().onStateTransition(event ->
                    log.warn("Circuit {} {} -> {}",
                            name,
                            event.getStateTransition().getFromState(),
                            event.getStateTransition().getToState())
            );
        }
        if (gatewayMetrics != null) {
            gatewayMetrics.bindCircuitBreaker(cb);
        }
        return cb;
    }

    public boolean isOpen(String providerCode, String operationCode) {
        if (!properties.isEnabled() || !StringUtils.hasText(providerCode)) {
            return false;
        }
        CircuitBreaker.State state = of(providerCode, operationCode).getState();
        boolean open = state == CircuitBreaker.State.OPEN || state == CircuitBreaker.State.FORCED_OPEN;
        if (open) {
            log.warn("Circuit OPEN for {} (state={})", circuitKey(providerCode, operationCode), state);
        }
        return open;
    }

    public String state(String providerCode, String operationCode) {
        if (!StringUtils.hasText(providerCode)) {
            return CircuitBreaker.State.DISABLED.name();
        }
        if (!properties.isEnabled()) {
            return "DISABLED";
        }
        return of(providerCode, operationCode).getState().name();
    }

    /** Force l'ouverture (maintenance / break manuel) sur une méthode. */
    public void forceOpen(String providerCode, String operationCode) {
        if (!StringUtils.hasText(providerCode)) {
            return;
        }
        of(providerCode, operationCode).transitionToForcedOpenState();
        log.warn("Circuit FORCE_OPEN for {}", circuitKey(providerCode, operationCode));
    }

    /** Remet le circuit méthode à CLOSED. */
    public void reset(String providerCode, String operationCode) {
        if (!StringUtils.hasText(providerCode)) {
            return;
        }
        of(providerCode, operationCode).reset();
        log.info("Circuit RESET for {}", circuitKey(providerCode, operationCode));
    }

    public void recordSuccess(String providerCode, String operationCode) {
        if (!properties.isEnabled() || !StringUtils.hasText(providerCode)) {
            return;
        }
        of(providerCode, operationCode).onSuccess(0, TimeUnit.NANOSECONDS);
    }

    public void recordFailure(String providerCode, String operationCode, ProviderExecutionResult result) {
        if (!properties.isEnabled() || !StringUtils.hasText(providerCode) || !isInfrastructureFailure(result)) {
            return;
        }
        of(providerCode, operationCode).onError(0, TimeUnit.NANOSECONDS,
                new ProviderInfrastructureException(providerCode, "Provider infra failure on " + operationCode));
    }

    public static boolean isInfrastructureFailure(ProviderExecutionResult result) {
        if (result == null) {
            return true;
        }
        if (result.isTimedOut()) {
            return true;
        }
        Integer http = result.getHttpStatus();
        return http != null && http >= 500;
    }

    static boolean isRecordedFailure(Throwable ex) {
        if (ex instanceof ProviderInfrastructureException) {
            return true;
        }
        Throwable cause = ex;
        while (cause != null) {
            if (cause instanceof SocketTimeoutException
                    || cause instanceof java.net.ConnectException
                    || cause instanceof java.io.IOException) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    public static String circuitKey(String providerCode, String operationCode) {
        String provider = StringUtils.hasText(providerCode) ? providerCode.trim() : "unknown";
        String op = StringUtils.hasText(operationCode) ? operationCode.trim().toUpperCase() : "UNKNOWN";
        return "provider:" + provider + ":op:" + op;
    }
}
