package com.m2i.omnicanal.presentation.resilience;

import com.m2i.omnicanal.model.Api;
import com.m2i.omnicanal.outils.restclient.constants.ErreurConstants;
import com.m2i.omnicanal.outils.restclient.model.ResultMessage;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.micrometer.tagged.TaggedCircuitBreakerMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Un circuit Resilience4j par {@code api.code_api}.
 * Seules les pannes d'infrastructure (connexion, timeout, HTTP 5xx) l'ouvrent.
 */
@Service
public class ApiCircuitBreaker {

    private static final Logger log = LoggerFactory.getLogger(ApiCircuitBreaker.class);

    private final boolean enabled;
    private final CircuitBreakerRegistry registry;
    private final CircuitBreakerConfig config;

    @Autowired
    public ApiCircuitBreaker(ApiResilienceProperties properties, ObjectProvider<MeterRegistry> meters) {
        this(properties, meters.getIfAvailable());
    }

    ApiCircuitBreaker(ApiResilienceProperties properties, MeterRegistry meterRegistry) {
        this.enabled = properties.isEnabled();
        int window = Math.max(1, properties.getFailureThreshold());
        float rate = properties.getFailureRateThreshold();
        if (rate <= 0f || rate > 100f) {
            rate = 50f;
        }
        int halfOpen = Math.max(1, properties.getHalfOpenPermittedCalls());
        this.config = CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(window)
                .minimumNumberOfCalls(window)
                .failureRateThreshold(rate)
                .waitDurationInOpenState(Duration.ofMillis(Math.max(1L, properties.getOpenDurationMs())))
                .permittedNumberOfCallsInHalfOpenState(halfOpen)
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .recordException(ApiCircuitBreaker::isInfrastructure)
                .build();
        this.registry = CircuitBreakerRegistry.of(config);
        if (meterRegistry != null) {
            TaggedCircuitBreakerMetrics.ofCircuitBreakerRegistry(registry).bindTo(meterRegistry);
        }
        log.info("Circuit API: enabled={} window={} failureRate={}% openDurationMs={} halfOpen={}",
                enabled, window, rate, properties.getOpenDurationMs(), halfOpen);
    }

    public Guard guard(Api api) {
        if (!enabled || api == null || api.getCodeApi() == null || api.getCodeApi().isBlank()) {
            return Guard.passthrough();
        }
        String name = "api:" + api.getCodeApi().trim();
        boolean created = registry.find(name).isEmpty();
        CircuitBreaker circuit = registry.circuitBreaker(name, config);
        if (created) {
            circuit.getEventPublisher().onStateTransition(event ->
                    log.warn("Circuit {} {} -> {}",
                            name,
                            event.getStateTransition().getFromState(),
                            event.getStateTransition().getToState()));
        }
        if (!circuit.tryAcquirePermission()) {
            log.warn("Circuit OPEN for {}", name);
            return Guard.blocked();
        }
        return Guard.acquired(circuit);
    }

    public ResultMessage unavailable(String responseType, String message) {
        ResultMessage result = new ResultMessage();
        result.setErrCode(ErreurConstants.RESSOURCE_ACCESS_ERROR);
        result.setMsg(message);
        result.setNumCouche("1");
        result.setResponseType(responseType);
        return result;
    }

    static boolean isInfrastructure(ResultMessage result) {
        if (result == null) {
            return false;
        }
        int code = result.getErrCode();
        return code == ErreurConstants.RESSOURCE_ACCESS_ERROR
                || code == ErreurConstants.HTTP_CODE_STATUS_ERROR
                || code == ErreurConstants.UNKNOWN_ERROR;
    }

    static boolean isInfrastructure(Throwable ex) {
        Throwable current = ex;
        while (current != null) {
            if (current instanceof ApiInfrastructureException
                    || current instanceof SocketTimeoutException
                    || current instanceof ConnectException
                    || current instanceof IOException
                    || current instanceof org.springframework.web.client.ResourceAccessException
                    || current instanceof org.springframework.web.client.HttpServerErrorException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    public static final class Guard {
        private final CircuitBreaker circuit;
        private final boolean rejected;
        private final long startedNanos;
        private boolean settled;

        private Guard(CircuitBreaker circuit, boolean rejected, long startedNanos) {
            this.circuit = circuit;
            this.rejected = rejected;
            this.startedNanos = startedNanos;
        }

        public static Guard passthrough() {
            return new Guard(null, false, 0L);
        }

        static Guard blocked() {
            return new Guard(null, true, 0L);
        }

        static Guard acquired(CircuitBreaker circuit) {
            return new Guard(circuit, false, System.nanoTime());
        }

        public boolean rejected() {
            return rejected;
        }

        public void record(ResultMessage result) {
            if (circuit == null || settled) {
                return;
            }
            settled = true;
            long elapsed = Math.max(0L, System.nanoTime() - startedNanos);
            if (result != null && result.getErrCode() == ErreurConstants.FONCTION_NON_IMPLEMENTE) {
                circuit.releasePermission();
                return;
            }
            if (result == null) {
                circuit.releasePermission();
                return;
            }
            if (isInfrastructure(result)) {
                circuit.onError(elapsed, TimeUnit.NANOSECONDS, new ApiInfrastructureException(result.getErrCode()));
                return;
            }
            circuit.onSuccess(elapsed, TimeUnit.NANOSECONDS);
        }

        public void record(Exception ex) {
            if (circuit == null || settled) {
                return;
            }
            settled = true;
            long elapsed = Math.max(0L, System.nanoTime() - startedNanos);
            if (isInfrastructure(ex)) {
                circuit.onError(elapsed, TimeUnit.NANOSECONDS, ex);
                return;
            }
            circuit.releasePermission();
        }

        public void ignore() {
            if (circuit == null || settled) {
                return;
            }
            settled = true;
            circuit.releasePermission();
        }
    }

    static final class ApiInfrastructureException extends RuntimeException {
        ApiInfrastructureException(int errCode) {
            super("Infrastructure failure " + errCode);
        }
    }
}
